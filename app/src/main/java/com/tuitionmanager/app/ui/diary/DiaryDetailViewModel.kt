package com.tuitionmanager.app.ui.diary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.Student
import com.tuitionmanager.app.domain.model.StudentDiary
import com.tuitionmanager.app.domain.model.StudentSubject
import com.tuitionmanager.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiaryDetailUiState(
    val isLoading: Boolean = true,
    val diary: StudentDiary? = null,
    val student: Student? = null,
    val subject: StudentSubject? = null,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    private val observeDiaryByIdUseCase: ObserveDiaryByIdUseCase,
    private val getStudentByIdUseCase: GetStudentByIdUseCase,
    private val observeSubjectsForStudentUseCase: ObserveSubjectsForStudentUseCase,
    private val deleteDiaryEntryUseCase: DeleteDiaryEntryUseCase,
    private val restoreDiaryEntryUseCase: RestoreDiaryEntryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val diaryId: String = checkNotNull(savedStateHandle["diaryId"])

    private val _isDeletedAction = MutableStateFlow(false)

    val uiState: StateFlow<DiaryDetailUiState> = observeDiaryByIdUseCase(diaryId)
        .flatMapLatest { diary ->
            if (diary == null) {
                flowOf(DiaryDetailUiState(isLoading = false, error = "Diary record not found or was deleted"))
            } else {
                combine(
                    flow { emit(getStudentByIdUseCase(diary.studentId)) },
                    observeSubjectsForStudentUseCase(diary.studentId),
                    _isDeletedAction
                ) { student, subjects, deleted ->
                    val subject = subjects.find { it.id == diary.subjectId }
                    DiaryDetailUiState(
                        isLoading = false,
                        diary = diary,
                        student = student,
                        subject = subject,
                        isDeleted = deleted || diary.isDeleted
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DiaryDetailUiState(isLoading = true)
        )

    fun deleteDiary(onComplete: () -> Unit) {
        viewModelScope.launch {
            deleteDiaryEntryUseCase(diaryId)
            _isDeletedAction.value = true
            onComplete()
        }
    }

    fun restoreDiary() {
        viewModelScope.launch {
            restoreDiaryEntryUseCase(diaryId)
            _isDeletedAction.value = false
        }
    }
}
