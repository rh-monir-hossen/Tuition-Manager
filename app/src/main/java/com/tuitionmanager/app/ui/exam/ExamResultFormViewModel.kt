package com.tuitionmanager.app.ui.exam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.engine.ExamEvaluationEngine
import com.tuitionmanager.app.domain.model.Exam
import com.tuitionmanager.app.domain.model.ExamResult
import com.tuitionmanager.app.domain.model.ExamWithDetails
import com.tuitionmanager.app.domain.usecase.GetExamResultUseCase
import com.tuitionmanager.app.domain.usecase.ObserveExamDetailsUseCase
import com.tuitionmanager.app.domain.usecase.RecordExamResultUseCase
import com.tuitionmanager.app.domain.usecase.UpdateExamResultUseCase
import com.tuitionmanager.app.utils.AppResult
import com.tuitionmanager.app.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ExamResultFormUiState(
    val isLoading: Boolean = true,
    val examDetails: ExamWithDetails? = null,
    val existingResultId: String? = null,
    val isEditMode: Boolean = false,
    val actualExamDate: Long = TimeUtils.getTodayStartOfDayEpochMs(),
    val marksObtainedText: String = "",
    val studentStrengths: String = "",
    val studentWeaknesses: String = "",
    val recommendations: String = "",
    val teacherRemarks: String = "",
    val gradedAt: Long = TimeUtils.getTodayStartOfDayEpochMs(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSavedSuccess: Boolean = false,
    // Live derived evaluation
    val livePercentage: Double? = null,
    val liveIsPassed: Boolean? = null,
    val liveGradeLevel: String? = null
)

@HiltViewModel
class ExamResultFormViewModel @Inject constructor(
    private val observeExamDetailsUseCase: ObserveExamDetailsUseCase,
    private val getExamResultUseCase: GetExamResultUseCase,
    private val recordExamResultUseCase: RecordExamResultUseCase,
    private val updateExamResultUseCase: UpdateExamResultUseCase,
    private val evaluationEngine: ExamEvaluationEngine,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val examId: String = checkNotNull(savedStateHandle["examId"])

    private val _uiState = MutableStateFlow(ExamResultFormUiState())
    val uiState: StateFlow<ExamResultFormUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            observeExamDetailsUseCase(examId).collect { details ->
                if (details != null) {
                    val existingResult = details.result ?: getExamResultUseCase(examId)
                    _uiState.update { current ->
                        if (existingResult != null && !current.isEditMode) {
                            val marks = existingResult.marksObtained
                            val pct = if (details.exam.totalMarks > 0) (marks / details.exam.totalMarks) * 100.0 else 0.0
                            val passed = existingResult.isPassed
                            val grade = evaluationEngine.determineGradeLevel(pct)
                            current.copy(
                                isLoading = false,
                                examDetails = details,
                                existingResultId = existingResult.id,
                                isEditMode = true,
                                actualExamDate = existingResult.actualExamDate,
                                marksObtainedText = if (marks % 1.0 == 0.0) marks.toInt().toString() else marks.toString(),
                                studentStrengths = existingResult.studentStrengths ?: "",
                                studentWeaknesses = existingResult.studentWeaknesses ?: "",
                                recommendations = existingResult.recommendations ?: "",
                                teacherRemarks = existingResult.teacherRemarks ?: "",
                                gradedAt = existingResult.gradedAt,
                                livePercentage = pct,
                                liveIsPassed = passed,
                                liveGradeLevel = grade
                            )
                        } else {
                            current.copy(
                                isLoading = false,
                                examDetails = details,
                                actualExamDate = if (current.actualExamDate <= 0L) details.exam.plannedDate else current.actualExamDate
                            )
                        }
                    }
                }
            }
        }
    }

    fun onMarksObtainedChanged(text: String) {
        _uiState.update { current ->
            val totalMarks = current.examDetails?.exam?.totalMarks ?: 100.0
            val passingMarks = current.examDetails?.exam?.passingMarks
            val marks = text.toDoubleOrNull()
            val (pct, passed, grade) = if (marks != null && marks >= 0.0 && totalMarks > 0.0) {
                val percentage = (marks / totalMarks) * 100.0
                val isPassed = evaluationEngine.determineIsPassed(marks, totalMarks, passingMarks)
                val gradeLevel = evaluationEngine.determineGradeLevel(percentage)
                Triple(percentage, isPassed, gradeLevel)
            } else {
                Triple(null, null, null)
            }

            current.copy(
                marksObtainedText = text,
                livePercentage = pct,
                liveIsPassed = passed,
                liveGradeLevel = grade
            )
        }
    }

    fun onActualExamDateChanged(epochMs: Long) {
        _uiState.update { it.copy(actualExamDate = epochMs) }
    }

    fun onGradedAtChanged(epochMs: Long) {
        _uiState.update { it.copy(gradedAt = epochMs) }
    }

    fun onStudentStrengthsChanged(text: String) {
        _uiState.update { it.copy(studentStrengths = text) }
    }

    fun onStudentWeaknessesChanged(text: String) {
        _uiState.update { it.copy(studentWeaknesses = text) }
    }

    fun onRecommendationsChanged(text: String) {
        _uiState.update { it.copy(recommendations = text) }
    }

    fun onTeacherRemarksChanged(text: String) {
        _uiState.update { it.copy(teacherRemarks = text) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun saveResult(onSuccess: () -> Unit) {
        val state = _uiState.value
        val exam = state.examDetails?.exam
        if (exam == null) {
            _uiState.update { it.copy(errorMessage = "Exam reference not loaded") }
            return
        }

        val marks = state.marksObtainedText.toDoubleOrNull()
        if (marks == null || marks < 0.0) {
            _uiState.update { it.copy(errorMessage = "Valid marks obtained is required (0 or greater)") }
            return
        }
        if (marks > exam.totalMarks) {
            _uiState.update { it.copy(errorMessage = "Marks obtained ($marks) cannot exceed total marks (${exam.totalMarks})") }
            return
        }
        if (state.actualExamDate <= 0L) {
            _uiState.update { it.copy(errorMessage = "Valid exam date is required") }
            return
        }

        val isPassed = evaluationEngine.determineIsPassed(
            marksObtained = marks,
            totalMarks = exam.totalMarks,
            passingMarks = exam.passingMarks
        )

        val now = System.currentTimeMillis()
        val result = ExamResult(
            id = state.existingResultId ?: UUID.randomUUID().toString(),
            examId = exam.id,
            studentId = exam.studentId,
            actualExamDate = state.actualExamDate,
            marksObtained = marks,
            isPassed = isPassed,
            studentStrengths = state.studentStrengths.trim().ifEmpty { null },
            studentWeaknesses = state.studentWeaknesses.trim().ifEmpty { null },
            recommendations = state.recommendations.trim().ifEmpty { null },
            teacherRemarks = state.teacherRemarks.trim().ifEmpty { null },
            gradedAt = state.gradedAt,
            createdAt = now,
            updatedAt = now
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val saveResult = if (state.isEditMode) {
                updateExamResultUseCase(result)
            } else {
                recordExamResultUseCase(result)
            }

            when (saveResult) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
                    onSuccess()
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = saveResult.message) }
                }
            }
        }
    }
}
