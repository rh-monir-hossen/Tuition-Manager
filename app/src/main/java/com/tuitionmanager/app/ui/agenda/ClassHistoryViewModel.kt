package com.tuitionmanager.app.ui.agenda

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassHistoryUiState(
    val isLoading: Boolean = true,
    val sessions: List<ClassSessionWithDetails> = emptyList(),
    val availableStudents: List<Student> = emptyList(),
    val availableSubjects: List<StudentSubject> = emptyList(),
    val selectedStudentId: String? = null,
    val selectedSubjectId: String? = null,
    val selectedStatus: SessionStatus? = null,
    val searchQuery: String = "",
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val missedCount: Int = 0,
    val scheduledCount: Int = 0,
    val cancelledCount: Int = 0
)

@HiltViewModel
class ClassHistoryViewModel @Inject constructor(
    private val observeAllSessionsUseCase: ObserveAllSessionsUseCase,
    private val observeSessionsForStudentUseCase: ObserveSessionsForStudentUseCase,
    private val observeStudentsUseCase: ObserveStudentsUseCase,
    private val observeSubjectsForStudentUseCase: ObserveSubjectsForStudentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialStudentId: String? = savedStateHandle["studentId"]

    private val _selectedStudentId = MutableStateFlow(initialStudentId?.takeIf { it.isNotBlank() })
    private val _selectedSubjectId = MutableStateFlow<String?>(null)
    private val _selectedStatus = MutableStateFlow<SessionStatus?>(null)
    private val _searchQuery = MutableStateFlow("")

    private val _availableStudents = MutableStateFlow<List<Student>>(emptyList())
    private val _availableSubjects = MutableStateFlow<List<StudentSubject>>(emptyList())

    val uiState: StateFlow<ClassHistoryUiState> = combine(
        _selectedStudentId,
        _selectedSubjectId,
        _selectedStatus,
        _searchQuery,
        _availableStudents,
        _availableSubjects
    ) { studentId, subjectId, status, query, students, subjects ->
        Params(studentId, subjectId, status, query, students, subjects)
    }.flatMapLatest { params ->
        val sessionsFlow = if (params.studentId != null) {
            observeSessionsForStudentUseCase(params.studentId)
        } else {
            observeAllSessionsUseCase()
        }

        sessionsFlow.map { allSessions ->
            val sorted = allSessions.sortedWith(
                compareByDescending<ClassSessionWithDetails> { it.session.sessionDate }
                    .thenByDescending { it.session.scheduledStartTime }
            )

            val filtered = sorted.filter { item ->
                val matchesSubject = params.subjectId == null || item.session.subjectId == params.subjectId
                val matchesStatus = params.status == null || item.session.status == params.status
                val matchesQuery = params.query.isBlank() ||
                        item.studentName.contains(params.query, ignoreCase = true) ||
                        item.subjectName.contains(params.query, ignoreCase = true) ||
                        (item.session.topicCovered?.contains(params.query, ignoreCase = true) == true) ||
                        (item.session.remarks?.contains(params.query, ignoreCase = true) == true)

                matchesSubject && matchesStatus && matchesQuery
            }

            ClassHistoryUiState(
                isLoading = false,
                sessions = filtered,
                availableStudents = params.students,
                availableSubjects = params.subjects,
                selectedStudentId = params.studentId,
                selectedSubjectId = params.subjectId,
                selectedStatus = params.status,
                searchQuery = params.query,
                totalCount = filtered.size,
                completedCount = filtered.count { it.session.status == SessionStatus.COMPLETED },
                missedCount = filtered.count { it.session.status == SessionStatus.MISSED },
                scheduledCount = filtered.count { it.session.status == SessionStatus.SCHEDULED },
                cancelledCount = filtered.count { it.session.status == SessionStatus.CANCELLED }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClassHistoryUiState()
    )

    init {
        loadStudents()
        if (initialStudentId != null && initialStudentId.isNotBlank()) {
            loadSubjectsForStudent(initialStudentId)
        }
    }

    private fun loadStudents() {
        viewModelScope.launch {
            observeStudentsUseCase(includeInactive = true).collect { students ->
                _availableStudents.value = students
            }
        }
    }

    private fun loadSubjectsForStudent(studentId: String) {
        viewModelScope.launch {
            observeSubjectsForStudentUseCase(studentId).collect { subjects ->
                _availableSubjects.value = subjects
            }
        }
    }

    fun onStudentSelected(studentId: String?) {
        _selectedStudentId.value = studentId
        _selectedSubjectId.value = null
        if (studentId != null) {
            loadSubjectsForStudent(studentId)
        } else {
            _availableSubjects.value = emptyList()
        }
    }

    fun onSubjectSelected(subjectId: String?) {
        _selectedSubjectId.value = subjectId
    }

    fun onStatusSelected(status: SessionStatus?) {
        _selectedStatus.value = status
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private data class Params(
        val studentId: String?,
        val subjectId: String?,
        val status: SessionStatus?,
        val query: String,
        val students: List<Student>,
        val subjects: List<StudentSubject>
    )
}
