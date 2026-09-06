package com.tuitionmanager.app.ui.diary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.Student
import com.tuitionmanager.app.domain.model.StudentDiary
import com.tuitionmanager.app.domain.model.StudentSubject
import com.tuitionmanager.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentDiaryUiState(
    val isLoading: Boolean = true,
    val studentId: String? = null,
    val student: Student? = null,
    val entries: List<StudentDiary> = emptyList(),
    val subjects: List<StudentSubject> = emptyList(),
    val selectedSubjectId: String? = null,
    val searchQuery: String = "",
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentDiaryViewModel @Inject constructor(
    private val observeDiaryHistoryUseCase: ObserveDiaryHistoryUseCase,
    private val searchDiaryUseCase: SearchDiaryUseCase,
    private val filterDiaryHistoryUseCase: FilterDiaryHistoryUseCase,
    private val getStudentByIdUseCase: GetStudentByIdUseCase,
    private val observeSubjectsForStudentUseCase: ObserveSubjectsForStudentUseCase,
    private val deleteDiaryEntryUseCase: DeleteDiaryEntryUseCase,
    private val restoreDiaryEntryUseCase: RestoreDiaryEntryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val studentId: String? = savedStateHandle["studentId"]

    private val _searchQuery = MutableStateFlow("")
    private val _selectedSubjectId = MutableStateFlow<String?>(null)
    private val _student = MutableStateFlow<Student?>(null)
    private val _subjects = MutableStateFlow<List<StudentSubject>>(emptyList())

    init {
        studentId?.let { sId ->
            viewModelScope.launch {
                _student.value = getStudentByIdUseCase(sId)
                observeSubjectsForStudentUseCase(sId).collect {
                    _subjects.value = it
                }
            }
        }
    }

    val uiState: StateFlow<StudentDiaryUiState> = combine(
        _searchQuery,
        _selectedSubjectId,
        _student,
        _subjects
    ) { query, subjectId, student, subjects ->
        QueryFilter(query, subjectId, student, subjects)
    }.flatMapLatest { qf ->
        val baseFlow = if (qf.query.isNotBlank()) {
            searchDiaryUseCase(studentId, qf.query)
        } else if (qf.subjectId != null) {
            filterDiaryHistoryUseCase(studentId, qf.subjectId, 0L, Long.MAX_VALUE)
        } else {
            observeDiaryHistoryUseCase(studentId)
        }

        baseFlow.map { entries ->
            StudentDiaryUiState(
                isLoading = false,
                studentId = studentId,
                student = qf.student,
                entries = entries,
                subjects = qf.subjects,
                selectedSubjectId = qf.subjectId,
                searchQuery = qf.query
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudentDiaryUiState(isLoading = true, studentId = studentId)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSubjectSelected(subjectId: String?) {
        _selectedSubjectId.value = subjectId
    }

    fun deleteEntry(diaryId: String) {
        viewModelScope.launch {
            deleteDiaryEntryUseCase(diaryId)
        }
    }

    fun restoreEntry(diaryId: String) {
        viewModelScope.launch {
            restoreDiaryEntryUseCase(diaryId)
        }
    }

    private data class QueryFilter(
        val query: String,
        val subjectId: String?,
        val student: Student?,
        val subjects: List<StudentSubject>
    )
}
