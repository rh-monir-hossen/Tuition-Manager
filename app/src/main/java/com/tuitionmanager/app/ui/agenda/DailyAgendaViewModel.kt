package com.tuitionmanager.app.ui.agenda

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.ClassSessionWithDetails
import com.tuitionmanager.app.domain.model.SessionStatus
import com.tuitionmanager.app.domain.usecase.*
import com.tuitionmanager.app.utils.AppResult
import com.tuitionmanager.app.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyAgendaUiState(
    val isLoading: Boolean = true,
    val selectedDateEpochMs: Long = TimeUtils.getTodayStartOfDayEpochMs(),
    val sessions: List<ClassSessionWithDetails> = emptyList(),
    val completedCount: Int = 0,
    val scheduledCount: Int = 0,
    val missedCount: Int = 0,
    val isGenerating: Boolean = false,
    val actionMessage: String? = null,
    val errorMessage: String? = null
) {
    val isToday: Boolean
        get() = selectedDateEpochMs == TimeUtils.getTodayStartOfDayEpochMs()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DailyAgendaViewModel @Inject constructor(
    private val generateExpectedSessionsForDateUseCase: GenerateExpectedSessionsForDateUseCase,
    private val observeSessionsForDateUseCase: ObserveSessionsForDateUseCase,
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase,
    private val rescheduleSessionUseCase: RescheduleSessionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialDateParam: String? = savedStateHandle["dateEpochMs"]
    private val initialDate = initialDateParam?.toLongOrNull() ?: TimeUtils.getTodayStartOfDayEpochMs()

    private val _selectedDate = MutableStateFlow(TimeUtils.getStartOfDayEpochMs(initialDate))
    private val _actionMessage = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isGenerating = MutableStateFlow(false)

    val uiState: StateFlow<DailyAgendaUiState> = _selectedDate
        .flatMapLatest { dateEpochMs ->
            // Auto-trigger generation for this date on first display
            viewModelScope.launch {
                generateExpectedSessionsForDateUseCase(dateEpochMs)
            }

            combine(
                observeSessionsForDateUseCase(dateEpochMs),
                _actionMessage,
                _errorMessage,
                _isGenerating
            ) { sessions, actionMsg, errorMsg, isGen ->
                val completed = sessions.count { it.session.status == SessionStatus.COMPLETED }
                val scheduled = sessions.count { it.session.status == SessionStatus.SCHEDULED }
                val missed = sessions.count { it.session.status == SessionStatus.MISSED }

                DailyAgendaUiState(
                    isLoading = false,
                    selectedDateEpochMs = dateEpochMs,
                    sessions = sessions,
                    completedCount = completed,
                    scheduledCount = scheduled,
                    missedCount = missed,
                    isGenerating = isGen,
                    actionMessage = actionMsg,
                    errorMessage = errorMsg
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailyAgendaUiState()
        )

    fun onPreviousDay() {
        val current = _selectedDate.value
        _selectedDate.value = current - 86400000L
    }

    fun onNextDay() {
        val current = _selectedDate.value
        _selectedDate.value = current + 86400000L
    }

    fun onToday() {
        _selectedDate.value = TimeUtils.getTodayStartOfDayEpochMs()
    }

    fun onDateSelected(epochMs: Long) {
        _selectedDate.value = TimeUtils.getStartOfDayEpochMs(epochMs)
    }

    fun generateSessionsFromRoutine() {
        viewModelScope.launch {
            _isGenerating.value = true
            val result = generateExpectedSessionsForDateUseCase(_selectedDate.value)
            _isGenerating.value = false
            when (result) {
                is AppResult.Success -> {
                    _actionMessage.value = "Sessions generated: ${result.data.size} class(es)"
                }
                is AppResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun markSessionCompleted(
        sessionId: String,
        actualStart: Int,
        actualEnd: Int,
        topicCovered: String,
        remarks: String,
        onDiaryRequested: ((studentId: String, classSessionId: String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val session = uiState.value.sessions.firstOrNull { it.session.id == sessionId }
            val result = updateSessionStatusUseCase(
                sessionId = sessionId,
                status = SessionStatus.COMPLETED,
                remarks = remarks.ifBlank { null },
                actualStart = actualStart,
                actualEnd = actualEnd,
                topicCovered = topicCovered.ifBlank { null }
            )

            when (result) {
                is AppResult.Success -> {
                    _actionMessage.value = "Class marked as Completed"
                    if (session != null && onDiaryRequested != null) {
                        onDiaryRequested(session.session.studentId, sessionId)
                    }
                }
                is AppResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun markSessionMissed(sessionId: String, remarks: String) {
        viewModelScope.launch {
            val result = updateSessionStatusUseCase(
                sessionId = sessionId,
                status = SessionStatus.MISSED,
                remarks = remarks.ifBlank { "Marked missed" }
            )
            when (result) {
                is AppResult.Success -> _actionMessage.value = "Class marked as Missed"
                is AppResult.Error -> _errorMessage.value = result.message
            }
        }
    }

    fun markSessionCancelled(sessionId: String, reason: String) {
        viewModelScope.launch {
            val result = updateSessionStatusUseCase(
                sessionId = sessionId,
                status = SessionStatus.CANCELLED,
                remarks = reason.ifBlank { "Cancelled" }
            )
            when (result) {
                is AppResult.Success -> _actionMessage.value = "Class cancelled"
                is AppResult.Error -> _errorMessage.value = result.message
            }
        }
    }

    fun rescheduleSession(
        sessionId: String,
        targetDateEpochMs: Long,
        newStartMinutes: Int,
        newEndMinutes: Int,
        reason: String
    ) {
        viewModelScope.launch {
            val result = rescheduleSessionUseCase(
                originalSessionId = sessionId,
                targetDateEpochMs = targetDateEpochMs,
                newStartTimeMinutes = newStartMinutes,
                newEndTimeMinutes = newEndMinutes,
                reason = reason.ifBlank { null }
            )
            when (result) {
                is AppResult.Success -> {
                    _actionMessage.value = "Class rescheduled successfully"
                }
                is AppResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun clearMessages() {
        _actionMessage.value = null
        _errorMessage.value = null
    }
}
