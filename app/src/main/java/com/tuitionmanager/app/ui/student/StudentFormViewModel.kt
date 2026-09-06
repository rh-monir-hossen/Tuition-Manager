package com.tuitionmanager.app.ui.student

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.usecase.*
import com.tuitionmanager.app.domain.validation.StudentValidator
import com.tuitionmanager.app.domain.validation.ValidationResult
import com.tuitionmanager.app.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class StudentFormUiState(
    val isEditMode: Boolean = false,
    val studentId: String = "",
    val name: String = "",
    val nameError: String? = null,
    val institution: String = "",
    val classGrade: String = "",
    val phone: String = "",
    val guardianName: String = "",
    val guardianPhone: String = "",
    val address: String = "",
    val monthlyFee: String = "",
    val billingCycleDay: String = "1",
    val initialSubjects: String = "",
    val isActive: Boolean = true,
    val guardianChannel: GuardianPreferredChannel = GuardianPreferredChannel.WHATSAPP,
    val isGuardianProgressSharingEnabled: Boolean = true,
    val guardianReportLanguage: GuardianReportLanguage = GuardianReportLanguage.EN,
    val guardianReportFormat: GuardianReportFormat = GuardianReportFormat.TEXT_SUMMARY,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val generalError: String? = null,
    val isSaveSuccess: Boolean = false
) {
    val isFormValid: Boolean
        get() = name.trim().isNotBlank()
}

@HiltViewModel
class StudentFormViewModel @Inject constructor(
    private val addStudentUseCase: AddStudentUseCase,
    private val updateStudentUseCase: UpdateStudentUseCase,
    private val getStudentByIdUseCase: GetStudentByIdUseCase,
    private val observeSubjectsForStudentUseCase: ObserveSubjectsForStudentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studentId: String? = savedStateHandle["studentId"]

    private val _uiState = MutableStateFlow(StudentFormUiState(
        isEditMode = !studentId.isNullOrBlank(),
        studentId = studentId ?: UUID.randomUUID().toString()
    ))
    val uiState: StateFlow<StudentFormUiState> = _uiState.asStateFlow()

    init {
        if (!studentId.isNullOrBlank()) {
            loadExistingStudent(studentId)
        }
    }

    private fun loadExistingStudent(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val student = getStudentByIdUseCase(id)
            if (student != null) {
                // Also load subjects
                val subjectsFlow = observeSubjectsForStudentUseCase(id)
                val existingSubjects = subjectsFlow.firstOrNull() ?: emptyList()
                val subjectString = existingSubjects.joinToString(", ") { it.subjectName }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = student.name,
                        institution = student.institution ?: "",
                        classGrade = student.classGrade ?: "",
                        phone = student.phone ?: "",
                        guardianName = student.guardianName ?: "",
                        guardianPhone = student.guardianPhone ?: "",
                        address = student.address ?: "",
                        monthlyFee = if (student.monthlyFeeAmount > 0) student.monthlyFeeAmount.toInt().toString() else "",
                        billingCycleDay = student.billingCycleDay.toString(),
                        initialSubjects = subjectString,
                        isActive = student.isActive,
                        guardianChannel = student.guardianPreferredChannel,
                        isGuardianProgressSharingEnabled = student.isGuardianProgressSharingEnabled,
                        guardianReportLanguage = student.guardianReportLanguage,
                        guardianReportFormat = student.guardianReportFormat
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, generalError = "Student not found") }
            }
        }
    }

    fun onNameChanged(value: String) {
        val validation = StudentValidator.validate(value)
        val error = if (!validation.isValid && value.isNotBlank()) {
            (validation as ValidationResult.Invalid).errorMessage
        } else null

        _uiState.update { it.copy(name = value, nameError = error) }
    }

    fun onInstitutionChanged(value: String) = _uiState.update { it.copy(institution = value) }
    fun onClassGradeChanged(value: String) = _uiState.update { it.copy(classGrade = value) }
    fun onPhoneChanged(value: String) = _uiState.update { it.copy(phone = value) }
    fun onGuardianNameChanged(value: String) = _uiState.update { it.copy(guardianName = value) }
    fun onGuardianPhoneChanged(value: String) = _uiState.update { it.copy(guardianPhone = value) }
    fun onAddressChanged(value: String) = _uiState.update { it.copy(address = value) }
    fun onMonthlyFeeChanged(value: String) = _uiState.update { it.copy(monthlyFee = value) }
    fun onBillingCycleDayChanged(value: String) = _uiState.update { it.copy(billingCycleDay = value) }
    fun onInitialSubjectsChanged(value: String) = _uiState.update { it.copy(initialSubjects = value) }
    fun onActiveStatusChanged(value: Boolean) = _uiState.update { it.copy(isActive = value) }
    fun onGuardianChannelChanged(value: GuardianPreferredChannel) = _uiState.update { it.copy(guardianChannel = value) }
    fun onProgressSharingChanged(value: Boolean) = _uiState.update { it.copy(isGuardianProgressSharingEnabled = value) }
    fun onReportLanguageChanged(value: GuardianReportLanguage) = _uiState.update { it.copy(guardianReportLanguage = value) }

    fun saveStudent() {
        val state = _uiState.value
        if (!state.isFormValid) {
            _uiState.update { it.copy(nameError = "Student full name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, generalError = null) }
            val now = System.currentTimeMillis()
            val feeAmount = state.monthlyFee.toDoubleOrNull() ?: 0.0
            val billingDay = state.billingCycleDay.toIntOrNull()?.coerceIn(1, 31) ?: 1

            val student = Student(
                id = state.studentId,
                name = state.name.trim(),
                institution = state.institution.trim().ifBlank { null },
                classGrade = state.classGrade.trim().ifBlank { null },
                phone = state.phone.trim().ifBlank { null },
                guardianName = state.guardianName.trim().ifBlank { null },
                guardianPhone = state.guardianPhone.trim().ifBlank { null },
                address = state.address.trim().ifBlank { null },
                monthlyFeeAmount = feeAmount,
                billingCycleDay = billingDay,
                isActive = state.isActive,
                joinedDate = now,
                guardianPreferredChannel = state.guardianChannel,
                isGuardianProgressSharingEnabled = state.isGuardianProgressSharingEnabled,
                guardianReportLanguage = state.guardianReportLanguage,
                guardianReportFormat = state.guardianReportFormat,
                createdAt = now,
                updatedAt = now
            )

            val result = if (state.isEditMode) {
                updateStudentUseCase(student)
            } else {
                val subjectsList = state.initialSubjects.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                addStudentUseCase(student, subjectsList)
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
