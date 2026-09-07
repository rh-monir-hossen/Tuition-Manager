package com.tuitionmanager.app.ui.agenda

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.ClassSessionWithDetails
import com.tuitionmanager.app.domain.model.SessionStatus
import com.tuitionmanager.app.domain.usecase.*
import com.tuitionmanager.app.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassSessionDetailUiState(
    val isLoading: Boolean = true,
    val sessionDetails: ClassSessionWithDetails? = null,
    val actionMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ClassSessionDetailViewModel @Inject constructor(
    private val getSessionByIdUseCase: GetSessionByIdUseCase,
    private val updateSessionStatusUseCase: UpdateSessionStatusUseCase,
    private val rescheduleSessionUseCase: RescheduleSessionUseCase,
    private val updateSessionTopicUseCase: UpdateSessionTopicUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    private val _uiState = MutableStateFlow(ClassSessionDetailUiState())
    val uiState: StateFlow<ClassSessionDetailUiState> = _uiState.asStateFlow()

    init {
        loadSession()
    }

    fun loadSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val details = getSessionByIdUseCase(sessionId)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    sessionDetails = details,
                    errorMessage = if (details == null) "Session not found" else null
                )
            }
        }
    }

    fun markSessionCompleted(
        actualStart: Int,
        actualEnd: Int,
        topicCovered: String,
        remarks: String
    ) {
        viewModelScope.launch {
            val result = updateSessionStatusUseCase(
                sessionId = sessionId,
                status = SessionStatus.COMPLETED,
                remarks = remarks.ifBlank { null },
                actualStart = actualStart,
                actualEnd = actualEnd,
                topicCovered = topicCovered.ifBlank { null }
            )
            handleResult(result, "Class marked as Completed")
        }
    }

    fun markSessionMissed(remarks: String) {
        viewModelScope.launch {
            val result = updateSessionStatusUseCase(
                sessionId = sessionId,
                status = SessionStatus.MISSED,
                remarks = remarks.ifBlank { "Marked missed" }
            )
            handleResult(result, "Class marked as Missed")
        }
    }

    fun markSessionCancelled(reason: String) {
        viewModelScope.launch {
            val result = updateSessionStatusUseCase(
                sessionId = sessionId,
                status = SessionStatus.CANCELLED,
                remarks = reason.ifBlank { "Cancelled" }
            )
            handleResult(result, "Class cancelled")
        }
    }

    fun rescheduleSession(
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
                    _uiState.update { it.copy(actionMessage = "Class rescheduled successfully") }
                    loadSession()
                }
                is AppResult.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
        }
    }

    private fun handleResult(result: AppResult<Unit>, successMessage: String) {
        when (result) {
            is AppResult.Success -> {
                _uiState.update { it.copy(actionMessage = successMessage) }
                loadSession()
            }
            is AppResult.Error -> {
                _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(actionMessage = null, errorMessage = null) }
    }
}
