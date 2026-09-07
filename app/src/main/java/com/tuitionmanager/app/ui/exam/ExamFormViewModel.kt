package com.tuitionmanager.app.ui.exam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.usecase.*
import com.tuitionmanager.app.utils.AppResult
import com.tuitionmanager.app.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ExamFormUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val examId: String? = null,
    val studentId: String = "",
    val subjectId: String = "",
    val title: String = "",
    val syllabusTopic: String = "",
    val plannedDate: Long = TimeUtils.getTodayStartOfDayEpochMs(),
    val plannedStartTimeMinutes: Int = 600, // 10:00 AM
    val durationMinutes: Int = 60,
    val totalMarksText: String = "50",
    val passingMarksText: String = "20",
    val examType: ExamType = ExamType.CHAPTER_TEST,
    val status: ExamStatus = ExamStatus.PLANNED,
    val notes: String = "",
    val students: List<Student> = emptyList(),
    val subjects: List<StudentSubject> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSavedSuccess: Boolean = false
)

@HiltViewModel
class ExamFormViewModel @Inject constructor(
    private val planExamUseCase: PlanExamUseCase,
    private val updateExamUseCase: UpdateExamUseCase,
    private val getExamByIdUseCase: GetExamByIdUseCase,
    private val observeAllActiveStudentsUseCase: ObserveAllActiveStudentsUseCase,
    private val observeSubjectsForStudentUseCase: ObserveSubjectsForStudentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navExamId: String? = savedStateHandle.get<String>("examId")?.takeIf { it.isNotBlank() }
    private val navStudentId: String? = savedStateHandle.get<String>("studentId")?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(
        ExamFormUiState(
            isEditMode = navExamId != null,
            examId = navExamId,
            studentId = navStudentId ?: ""
        )
    )
    val uiState: StateFlow<ExamFormUiState> = _uiState.asStateFlow()

    init {
        loadStudents()
        if (navExamId != null) {
            loadExistingExam(navExamId)
        } else if (!navStudentId.isNullOrBlank()) {
            loadSubjectsForStudent(navStudentId)
        }
    }

    private fun loadStudents() {
        viewModelScope.launch {
            observeAllActiveStudentsUseCase().collect { studentList ->
                _uiState.update { state ->
                    val chosenStudentId = when {
                        state.studentId.isNotBlank() -> state.studentId
                        studentList.isNotEmpty() -> studentList.first().id
                        else -> ""
                    }
                    state.copy(students = studentList, studentId = chosenStudentId)
                }
                if (_uiState.value.studentId.isNotBlank()) {
                    loadSubjectsForStudent(_uiState.value.studentId)
                }
            }
        }
    }

    private fun loadSubjectsForStudent(studentId: String) {
        viewModelScope.launch {
            observeSubjectsForStudentUseCase(studentId).collect { subjectList ->
                _uiState.update { state ->
                    val chosenSubjectId = when {
                        state.subjectId.isNotBlank() && subjectList.any { it.id == state.subjectId } -> state.subjectId
                        subjectList.isNotEmpty() -> subjectList.first().id
                        else -> ""
                    }
                    state.copy(subjects = subjectList, subjectId = chosenSubjectId)
                }
            }
        }
    }

    private fun loadExistingExam(examId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val existing = getExamByIdUseCase(examId)
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        studentId = existing.studentId,
                        subjectId = existing.subjectId,
                        title = existing.title,
                        syllabusTopic = existing.syllabusTopic,
                        plannedDate = existing.plannedDate,
                        plannedStartTimeMinutes = existing.plannedStartTimeMinutes,
                        durationMinutes = existing.durationMinutes,
                        totalMarksText = if (existing.totalMarks % 1.0 == 0.0) existing.totalMarks.toInt().toString() else existing.totalMarks.toString(),
                        passingMarksText = existing.passingMarks?.let { pm ->
                            if (pm % 1.0 == 0.0) pm.toInt().toString() else pm.toString()
                        } ?: "",
                        examType = existing.examType,
                        status = existing.status,
                        notes = existing.notes ?: ""
                    )
                }
                loadSubjectsForStudent(existing.studentId)
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Exam record not found") }
            }
        }
    }

    fun onStudentChanged(studentId: String) {
        _uiState.update { it.copy(studentId = studentId, subjectId = "") }
        loadSubjectsForStudent(studentId)
    }

    fun onSubjectChanged(subjectId: String) {
        _uiState.update { it.copy(subjectId = subjectId) }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onSyllabusTopicChanged(topic: String) {
        _uiState.update { it.copy(syllabusTopic = topic) }
    }

    fun onPlannedDateChanged(epochMs: Long) {
        _uiState.update { it.copy(plannedDate = epochMs) }
    }

    fun onStartTimeChanged(minutes: Int) {
        _uiState.update { it.copy(plannedStartTimeMinutes = minutes) }
    }

    fun onDurationChanged(minutes: Int) {
        _uiState.update { it.copy(durationMinutes = minutes) }
    }

    fun onTotalMarksChanged(text: String) {
        _uiState.update { it.copy(totalMarksText = text) }
    }

    fun onPassingMarksChanged(text: String) {
        _uiState.update { it.copy(passingMarksText = text) }
    }

    fun onExamTypeChanged(type: ExamType) {
        _uiState.update { it.copy(examType = type) }
    }

    fun onStatusChanged(status: ExamStatus) {
        _uiState.update { it.copy(status = status) }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun saveExam(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.studentId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please select a student") }
            return
        }
        if (state.subjectId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please select a subject") }
            return
        }
        if (state.title.trim().isBlank()) {
            _uiState.update { it.copy(errorMessage = "Exam title cannot be empty") }
            return
        }
        val totalMarks = state.totalMarksText.toDoubleOrNull()
        if (totalMarks == null || totalMarks <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Total marks must be greater than 0") }
            return
        }
        val passingMarks = if (state.passingMarksText.isNotBlank()) {
            val pm = state.passingMarksText.toDoubleOrNull()
            if (pm == null || pm < 0.0 || pm > totalMarks) {
                _uiState.update { it.copy(errorMessage = "Passing marks must be between 0 and total marks ($totalMarks)") }
                return
            }
            pm
        } else {
            null
        }

        if (state.durationMinutes <= 0) {
            _uiState.update { it.copy(errorMessage = "Duration must be greater than 0 minutes") }
            return
        }

        val now = System.currentTimeMillis()
        val exam = Exam(
            id = state.examId ?: UUID.randomUUID().toString(),
            studentId = state.studentId,
            subjectId = state.subjectId,
            title = state.title.trim(),
            syllabusTopic = state.syllabusTopic.trim(),
            plannedDate = state.plannedDate,
            plannedStartTimeMinutes = state.plannedStartTimeMinutes,
            durationMinutes = state.durationMinutes,
            totalMarks = totalMarks,
            passingMarks = passingMarks,
            examType = state.examType,
            status = state.status,
            notes = state.notes.trim().ifEmpty { null },
            createdAt = now,
            updatedAt = now
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val result = if (state.isEditMode) {
                updateExamUseCase(exam)
            } else {
                planExamUseCase(exam)
            }

            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
                    onSuccess()
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }
}
