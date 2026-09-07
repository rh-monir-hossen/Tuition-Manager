package com.tuitionmanager.app.ui.exam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamFilterState(
    val studentId: String? = null,
    val subjectId: String? = null,
    val examType: ExamType? = null,
    val status: ExamStatus? = null,
    val startDateEpochMs: Long? = null,
    val endDateEpochMs: Long? = null,
    val searchQuery: String = ""
)

data class ExamListUiState(
    val isLoading: Boolean = true,
    val exams: List<ExamWithDetails> = emptyList(),
    val students: List<Student> = emptyList(),
    val subjects: List<StudentSubject> = emptyList(),
    val filters: ExamFilterState = ExamFilterState(),
    val userMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExamListViewModel @Inject constructor(
    private val filterExamsUseCase: FilterExamsUseCase,
    private val observeAllActiveStudentsUseCase: ObserveAllActiveStudentsUseCase,
    private val observeSubjectsForStudentUseCase: ObserveSubjectsForStudentUseCase,
    private val deleteExamUseCase: DeleteExamUseCase,
    private val restoreExamUseCase: RestoreExamUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialStudentId: String? = savedStateHandle.get<String>("studentId")?.takeIf { it.isNotBlank() }
    private val _filters = MutableStateFlow(ExamFilterState(studentId = initialStudentId))
    private val _userMessage = MutableStateFlow<String?>(null)

    val students: StateFlow<List<Student>> = observeAllActiveStudentsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<StudentSubject>> = _filters
        .flatMapLatest { filters ->
            val sId = filters.studentId
            if (sId != null) {
                observeSubjectsForStudentUseCase(sId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<ExamListUiState> = combine(
        _filters.flatMapLatest { filters ->
            filterExamsUseCase(
                studentId = filters.studentId,
                subjectId = filters.subjectId,
                examType = filters.examType,
                status = filters.status,
                startDateEpochMs = filters.startDateEpochMs,
                endDateEpochMs = filters.endDateEpochMs,
                query = filters.searchQuery
            )
        },
        students,
        subjects,
        _filters,
        _userMessage
    ) { exams, studentsList, subjectsList, filterState, message ->
        ExamListUiState(
            isLoading = false,
            exams = exams,
            students = studentsList,
            subjects = subjectsList,
            filters = filterState,
            userMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExamListUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _filters.update { it.copy(searchQuery = query) }
    }

    fun onStudentSelected(studentId: String?) {
        _filters.update { it.copy(studentId = studentId, subjectId = null) }
    }

    fun onSubjectSelected(subjectId: String?) {
        _filters.update { it.copy(subjectId = subjectId) }
    }

    fun onExamTypeSelected(type: ExamType?) {
        _filters.update { it.copy(examType = type) }
    }

    fun onStatusSelected(status: ExamStatus?) {
        _filters.update { it.copy(status = status) }
    }

    fun onDateRangeSelected(start: Long?, end: Long?) {
        _filters.update { it.copy(startDateEpochMs = start, endDateEpochMs = end) }
    }

    fun clearFilters() {
        _filters.value = ExamFilterState()
    }

    fun deleteExam(examId: String) {
        viewModelScope.launch {
            deleteExamUseCase(examId)
            _userMessage.value = "Exam deleted"
        }
    }

    fun restoreExam(examId: String) {
        viewModelScope.launch {
            restoreExamUseCase(examId)
            _userMessage.value = "Exam restored"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
