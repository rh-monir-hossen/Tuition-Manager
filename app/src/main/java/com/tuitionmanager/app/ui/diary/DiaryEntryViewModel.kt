package com.tuitionmanager.app.ui.diary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.repository.StudentSubjectRepository
import com.tuitionmanager.app.domain.usecase.*
import com.tuitionmanager.app.domain.validation.DiaryValidator
import com.tuitionmanager.app.domain.validation.ValidationResult
import com.tuitionmanager.app.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DiaryEntryUiState(
    val isEditMode: Boolean = false,
    val diaryId: String = "",
    val studentId: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val classSessionId: String? = null,
    val dateEpochMs: Long = System.currentTimeMillis(),
    val topicTitle: String = "",
    val topicTitleError: String? = null,
    val whatWasTaught: String = "",
    val whatWasTaughtError: String? = null,
    val homeworkAssigned: String = "",
    val homeworkStatus: HomeworkStatus = HomeworkStatus.ASSIGNED,
    val practiceGiven: String = "",
    val studentUnderstanding: StudentUnderstanding = StudentUnderstanding.GOOD,
    val teacherRemarks: String = "",
    val nextClassPlan: String = "",
    val availableStudents: List<Student> = emptyList(),
    val availableSubjects: List<StudentSubject> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val generalError: String? = null,
    val isSaveSuccess: Boolean = false
) {
    val isFormValid: Boolean
        get() = studentId.isNotBlank() &&
                subjectId.isNotBlank() &&
                topicTitle.trim().isNotBlank() &&
                whatWasTaught.trim().isNotBlank()
}

@HiltViewModel
class DiaryEntryViewModel @Inject constructor(
    private val addDiaryEntryUseCase: AddDiaryEntryUseCase,
    private val updateDiaryEntryUseCase: UpdateDiaryEntryUseCase,
    private val getDiaryByIdUseCase: GetDiaryByIdUseCase,
    private val observeStudentsUseCase: ObserveStudentsUseCase,
    private val observeSubjectsForStudentUseCase: ObserveSubjectsForStudentUseCase,
    private val addSubjectUseCase: AddSubjectUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val diaryId: String? = savedStateHandle["diaryId"]
    private val initialStudentId: String? = savedStateHandle["studentId"]
    private val initialSessionId: String? = savedStateHandle["classSessionId"]

    private val _uiState = MutableStateFlow(DiaryEntryUiState(
        isEditMode = !diaryId.isNullOrBlank(),
        diaryId = diaryId ?: UUID.randomUUID().toString(),
        studentId = initialStudentId ?: "",
        classSessionId = initialSessionId?.takeIf { it.isNotBlank() }
    ))
    val uiState: StateFlow<DiaryEntryUiState> = _uiState.asStateFlow()

    init {
        loadStudents()

        if (!diaryId.isNullOrBlank()) {
            loadExistingDiary(diaryId)
        } else if (!initialStudentId.isNullOrBlank()) {
            loadSubjectsForStudent(initialStudentId)
        }
    }

    private fun loadStudents() {
        viewModelScope.launch {
            observeStudentsUseCase(includeInactive = false).collect { list ->
                _uiState.update { it.copy(availableStudents = list) }
            }
        }
    }

    private fun loadSubjectsForStudent(sId: String) {
        viewModelScope.launch {
            observeSubjectsForStudentUseCase(sId).collect { subjects ->
                _uiState.update { state ->
                    val defaultSubject = if (state.subjectId.isBlank() && subjects.isNotEmpty()) {
                        subjects.first()
                    } else null

                    state.copy(
                        availableSubjects = subjects,
                        subjectId = defaultSubject?.id ?: state.subjectId,
                        subjectName = defaultSubject?.subjectName ?: state.subjectName
                    )
                }
            }
        }
    }

    private fun loadExistingDiary(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val diary = getDiaryByIdUseCase(id)
            if (diary != null) {
                loadSubjectsForStudent(diary.studentId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        studentId = diary.studentId,
                        subjectId = diary.subjectId,
                        classSessionId = diary.classSessionId,
                        dateEpochMs = diary.date,
                        topicTitle = diary.topicTitle,
                        whatWasTaught = diary.whatWasTaught,
                        homeworkAssigned = diary.homeworkAssigned ?: "",
                        homeworkStatus = diary.homeworkStatus,
                        practiceGiven = diary.practiceGiven ?: "",
                        studentUnderstanding = diary.studentUnderstanding,
                        teacherRemarks = diary.teacherRemarks ?: "",
                        nextClassPlan = diary.nextClassPlan ?: ""
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, generalError = "Diary entry not found") }
            }
        }
    }

    fun onStudentSelected(sId: String) {
        _uiState.update { it.copy(studentId = sId, subjectId = "", subjectName = "") }
        loadSubjectsForStudent(sId)
    }

    fun onSubjectSelected(subject: StudentSubject) {
        _uiState.update { it.copy(subjectId = subject.id, subjectName = subject.subjectName) }
    }

    fun onAddNewSubject(subjectName: String) {
        val sId = _uiState.value.studentId
        if (sId.isBlank() || subjectName.isBlank()) return
        viewModelScope.launch {
            addSubjectUseCase(sId, subjectName)
        }
    }

    fun onDateChanged(epochMs: Long) = _uiState.update { it.copy(dateEpochMs = epochMs) }

    fun onTopicTitleChanged(value: String) {
        val error = if (value.trim().isEmpty()) "Topic title is required" else null
        _uiState.update { it.copy(topicTitle = value, topicTitleError = error) }
    }

    fun onWhatWasTaughtChanged(value: String) {
        val error = if (value.trim().isEmpty()) "Lesson content is required" else null
        _uiState.update { it.copy(whatWasTaught = value, whatWasTaughtError = error) }
    }

    fun onHomeworkAssignedChanged(value: String) = _uiState.update { it.copy(homeworkAssigned = value) }
    fun onHomeworkStatusChanged(status: HomeworkStatus) = _uiState.update { it.copy(homeworkStatus = status) }
    fun onPracticeGivenChanged(value: String) = _uiState.update { it.copy(practiceGiven = value) }
    fun onUnderstandingChanged(value: StudentUnderstanding) = _uiState.update { it.copy(studentUnderstanding = value) }
    fun onTeacherRemarksChanged(value: String) = _uiState.update { it.copy(teacherRemarks = value) }
    fun onNextClassPlanChanged(value: String) = _uiState.update { it.copy(nextClassPlan = value) }

    fun saveDiaryEntry() {
        val state = _uiState.value
        val validation = DiaryValidator.validate(
            studentId = state.studentId,
            subjectId = state.subjectId,
            topicTitle = state.topicTitle,
            whatWasTaught = state.whatWasTaught
        )
        if (!validation.isValid) {
            _uiState.update { it.copy(generalError = (validation as ValidationResult.Invalid).errorMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, generalError = null) }
            val now = System.currentTimeMillis()
            val diary = StudentDiary(
                id = state.diaryId,
                studentId = state.studentId,
                subjectId = state.subjectId,
                classSessionId = state.classSessionId,
                date = state.dateEpochMs,
                topicTitle = state.topicTitle.trim(),
                whatWasTaught = state.whatWasTaught.trim(),
                homeworkAssigned = state.homeworkAssigned.trim().ifBlank { null },
                homeworkStatus = state.homeworkStatus,
                practiceGiven = state.practiceGiven.trim().ifBlank { null },
                studentUnderstanding = state.studentUnderstanding,
                teacherRemarks = state.teacherRemarks.trim().ifBlank { null },
                nextClassPlan = state.nextClassPlan.trim().ifBlank { null },
                createdAt = now,
                updatedAt = now
            )

            val result = if (state.isEditMode) {
                updateDiaryEntryUseCase(diary)
            } else {
                addDiaryEntryUseCase(diary)
            }

            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, isSaveSuccess = true) }
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, generalError = result.message) }
                }
            }
        }
    }
}
