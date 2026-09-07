package com.tuitionmanager.app.ui.exam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.ExamStatus
import com.tuitionmanager.app.domain.model.ExamWithDetails
import com.tuitionmanager.app.domain.usecase.*
import com.tuitionmanager.app.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamDetailUiState(
    val isLoading: Boolean = true,
    val examDetails: ExamWithDetails? = null,
    val userMessage: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class ExamDetailViewModel @Inject constructor(
    private val observeExamDetailsUseCase: ObserveExamDetailsUseCase,
    private val deleteExamUseCase: DeleteExamUseCase,
    private val cancelExamUseCase: CancelExamUseCase,
    private val deleteExamResultUseCase: DeleteExamResultUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val examId: String = checkNotNull(savedStateHandle["examId"])
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _isDeleted = MutableStateFlow(false)

    val uiState: StateFlow<ExamDetailUiState> = combine(
        observeExamDetailsUseCase(examId),
        _userMessage,
        _isDeleted
    ) { details, message, deleted ->
        ExamDetailUiState(
            isLoading = false,
            examDetails = details,
            userMessage = message,
            isDeleted = deleted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExamDetailUiState(isLoading = true)
    )

    fun deleteExam(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = deleteExamUseCase(examId)
            if (result is AppResult.Success) {
                _isDeleted.value = true
                onSuccess()
            } else if (result is AppResult.Error) {
                _userMessage.value = result.message
            }
        }
    }

    fun cancelExam() {
        viewModelScope.launch {
            val result = cancelExamUseCase(examId)
            if (result is AppResult.Success) {
                _userMessage.value = "Exam marked as cancelled"
            } else if (result is AppResult.Error) {
                _userMessage.value = result.message
            }
        }
    }

    fun deleteResult() {
        val resultId = uiState.value.examDetails?.result?.id ?: return
        viewModelScope.launch {
            val res = deleteExamResultUseCase(resultId)
            if (res is AppResult.Success) {
                _userMessage.value = "Exam result deleted"
            } else if (res is AppResult.Error) {
                _userMessage.value = res.message
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
