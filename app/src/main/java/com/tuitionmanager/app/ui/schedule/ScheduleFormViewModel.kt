package com.tuitionmanager.app.ui.schedule

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.usecase.*
import com.tuitionmanager.app.domain.validation.ScheduleValidator
import com.tuitionmanager.app.domain.validation.ValidationResult
import com.tuitionmanager.app.utils.AppResult
import com.tuitionmanager.app.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ScheduleFormUiState(
    val isEditMode: Boolean = false,
    val scheduleId: String = "",
    val studentId: String = "",
    val subjectId: String = "",
    val dayOfWeek: Int = 6, // Default Saturday
    val startTimeMinutes: Int = 600, // 10:00 AM
    val endTimeMinutes: Int = 660,   // 11:00 AM
    val location: String = "",
    val notes: String = "",
    val isActive: Boolean = true,
    val availableStudents: List<Student> = emptyList(),
    val availableSubjects: List<StudentSubject> = emptyList(),
    val hasConflict: Boolean = false,
    val conflictingSchedules: List<ScheduleConflictDetails> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
) {
    val durationMinutes: Int
        get() = (endTimeMinutes - startTimeMinutes).coerceAtLeast(0)

    val isFormValid: Boolean
        get() = studentId.isNotBlank() &&
                subjectId.isNotBlank() &&
                dayOfWeek in 1..7 &&
                startTimeMinutes in 0..1439 &&
                endTimeMinutes in 0..1439 &&
                endTimeMinutes > startTimeMinutes
}

@HiltViewModel
class ScheduleFormViewModel @Inject constructor(
    private val addScheduleUseCase: AddScheduleUseCase,
    private val updateScheduleUseCase: UpdateScheduleUseCase,
    private val getScheduleByIdUseCase: GetScheduleByIdUseCase,
    private val checkScheduleConflictUseCase: CheckScheduleConflictUseCase,
    private val observeStudentsUseCase: ObserveStudentsUseCase,
    private val observeSubjectsForStudentUseCase: ObserveSubjectsForStudentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editScheduleId: String? = savedStateHandle["scheduleId"]
    private val preselectedStudentId: String? = savedStateHandle["studentId"]

    private val _uiState = MutableStateFlow(
        ScheduleFormUiState(
            isEditMode = !editScheduleId.isNullOrBlank(),
            scheduleId = editScheduleId ?: UUID.randomUUID().toString(),
            studentId = preselectedStudentId ?: ""
        )
    )
    val uiState: StateFlow<ScheduleFormUiState> = _uiState.asStateFlow()

    init {
        loadStudents()

        if (!editScheduleId.isNullOrBlank()) {
            loadExistingSchedule(editScheduleId)
        } else if (!preselectedStudentId.isNullOrBlank()) {
            loadSubjectsForStudent(preselectedStudentId)
        }
    }

    private fun loadStudents() {
        viewModelScope.launch {
            observeStudentsUseCase(includeInactive = false).collect { students ->
                _uiState.update { state ->
                    val updatedStudentId = if (state.studentId.isBlank() && students.isNotEmpty()) {
                        students.first().id
                    } else state.studentId

                    state.copy(
                        availableStudents = students,
                        studentId = updatedStudentId
                    )
                }
                if (_uiState.value.studentId.isNotBlank() && _uiState.value.availableSubjects.isEmpty()) {
                    loadSubjectsForStudent(_uiState.value.studentId)
                }
            }
        }
    }

    private fun loadSubjectsForStudent(sId: String) {
        viewModelScope.launch {
            observeSubjectsForStudentUseCase(sId).collect { subjects ->
                _uiState.update { state ->
                    val defaultSubject = if (subjects.isNotEmpty() && (state.subjectId.isBlank() || subjects.none { it.id == state.subjectId })) {
                        subjects.first()
                    } else null

                    state.copy(
                        availableSubjects = subjects,
                        subjectId = defaultSubject?.id ?: state.subjectId
                    )
                }
                triggerConflictCheck()
            }
        }
    }

    private fun loadExistingSchedule(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val schedule = getScheduleByIdUseCase(id)
            if (schedule != null) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        scheduleId = schedule.id,
                        studentId = schedule.studentId,
                        subjectId = schedule.subjectId,
                        dayOfWeek = schedule.dayOfWeek,
                        startTimeMinutes = schedule.startTimeMinutes,
                        endTimeMinutes = schedule.endTimeMinutes,
                        location = schedule.location ?: "",
                        notes = schedule.notes ?: "",
                        isActive = schedule.isActive
                    )
                }
                loadSubjectsForStudent(schedule.studentId)
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Schedule not found") }
            }
        }
    }

    fun onStudentSelected(sId: String) {
        _uiState.update { it.copy(studentId = sId, subjectId = "") }
        loadSubjectsForStudent(sId)
    }

    fun onSubjectSelected(subjId: String) {
        _uiState.update { it.copy(subjectId = subjId) }
    }

    fun onDayOfWeekSelected(day: Int) {
        _uiState.update { it.copy(dayOfWeek = day) }
        triggerConflictCheck()
    }

    fun onStartTimeSelected(minutes: Int) {
        _uiState.update { state ->
            val updatedEnd = if (state.endTimeMinutes <= minutes) {
                (minutes + 60).coerceAtMost(1439)
            } else {
                state.endTimeMinutes
            }
            state.copy(startTimeMinutes = minutes, endTimeMinutes = updatedEnd)
        }
        triggerConflictCheck()
    }

    fun onEndTimeSelected(minutes: Int) {
        _uiState.update { it.copy(endTimeMinutes = minutes) }
        triggerConflictCheck()
    }

    fun onLocationChanged(loc: String) {
        _uiState.update { it.copy(location = loc) }
    }

    fun onNotesChanged(notesText: String) {
        _uiState.update { it.copy(notes = notesText) }
    }

    fun onActiveToggled(active: Boolean) {
        _uiState.update { it.copy(isActive = active) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun triggerConflictCheck() {
        val state = _uiState.value
        if (state.startTimeMinutes >= state.endTimeMinutes || state.dayOfWeek !in 1..7) return

        viewModelScope.launch {
            val result = checkScheduleConflictUseCase(
                dayOfWeek = state.dayOfWeek,
                startTimeMinutes = state.startTimeMinutes,
                endTimeMinutes = state.endTimeMinutes,
                ignoreScheduleId = if (state.isEditMode) state.scheduleId else null
            )
            _uiState.update {
                it.copy(
                    hasConflict = result.hasConflict,
                    conflictingSchedules = result.conflicts
                )
            }
        }
    }

    fun saveSchedule(onSuccess: () -> Unit) {
        val state = _uiState.value
        val validation = ScheduleValidator.validate(
            studentId = state.studentId,
            subjectId = state.subjectId,
            dayOfWeek = state.dayOfWeek,
            startTimeMinutes = state.startTimeMinutes,
            endTimeMinutes = state.endTimeMinutes
        )

        if (!validation.isValid) {
            _uiState.update { it.copy(errorMessage = (validation as ValidationResult.Invalid).errorMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val now = System.currentTimeMillis()
            val schedule = Schedule(
                id = state.scheduleId,
                studentId = state.studentId,
                subjectId = state.subjectId,
                dayOfWeek = state.dayOfWeek,
                startTimeMinutes = state.startTimeMinutes,
                endTimeMinutes = state.endTimeMinutes,
                location = state.location.trim().ifBlank { null },
                notes = state.notes.trim().ifBlank { null },
                isActive = state.isActive,
                createdAt = now,
                updatedAt = now
            )

            val result = if (state.isEditMode) {
                updateScheduleUseCase(schedule)
            } else {
                addScheduleUseCase(schedule)
            }

            when (result) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                    onSuccess()
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
                }
            }
        }
    }
}
