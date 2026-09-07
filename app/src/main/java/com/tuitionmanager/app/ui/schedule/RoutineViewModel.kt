package com.tuitionmanager.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.RoutineDay
import com.tuitionmanager.app.domain.model.ScheduleWithDetails
import com.tuitionmanager.app.domain.usecase.DeleteScheduleUseCase
import com.tuitionmanager.app.domain.usecase.ObserveWeeklyRoutineUseCase
import com.tuitionmanager.app.domain.usecase.ToggleScheduleActiveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineUiState(
    val isLoading: Boolean = true,
    val selectedDayOfWeek: Int? = null, // null means "All Week"
    val allSchedules: List<ScheduleWithDetails> = emptyList(),
    val displayedSchedules: List<ScheduleWithDetails> = emptyList(),
    val totalSlotsCount: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val observeWeeklyRoutineUseCase: ObserveWeeklyRoutineUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val toggleScheduleActiveUseCase: ToggleScheduleActiveUseCase
) : ViewModel() {

    private val _selectedDay = MutableStateFlow<Int?>(null) // null = All Week
    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeWeeklyRoutineUseCase(),
                _selectedDay
            ) { schedules, selectedDay ->
                val sorted = schedules.sortedWith(
                    compareBy<ScheduleWithDetails> {
                        // Order days starting from Saturday (6, 7, 1, 2, 3, 4, 5)
                        when (it.dayOfWeek) {
                            6 -> 0
                            7 -> 1
                            else -> it.dayOfWeek + 1
                        }
                    }.thenBy { it.schedule.startTimeMinutes }
                )

                val filtered = if (selectedDay == null) {
                    sorted
                } else {
                    sorted.filter { it.dayOfWeek == selectedDay }
                }

                RoutineUiState(
                    isLoading = false,
                    selectedDayOfWeek = selectedDay,
                    allSchedules = sorted,
                    displayedSchedules = filtered,
                    totalSlotsCount = sorted.size
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onDaySelected(dayOfWeek: Int?) {
        _selectedDay.value = dayOfWeek
    }

    fun toggleActive(scheduleId: String, currentActive: Boolean) {
        viewModelScope.launch {
            toggleScheduleActiveUseCase(scheduleId, !currentActive)
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            deleteScheduleUseCase(scheduleId)
        }
    }
}
