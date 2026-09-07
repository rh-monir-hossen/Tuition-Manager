package com.tuitionmanager.app.ui.student

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProfileTab {
    OVERVIEW,
    SCHEDULE,
    CLASSES,
    DIARY,
    EXAMS,
    PAYMENTS,
    PROGRESS,
    NOTES
}

private data class StudentDataGroup(
    val student: Student?,
    val stats: StudentStats,
    val subjects: List<StudentSubject>,
    val diaries: List<StudentDiary>
)

private data class ActivityDataGroup(
    val schedules: List<ScheduleWithDetails>,
    val sessions: List<ClassSessionWithDetails>,
    val exams: List<ExamWithDetails>,
    val tab: ProfileTab
)

data class StudentProfileUiState(
    val isLoading: Boolean = true,
    val student: Student? = null,
    val stats: StudentStats = StudentStats(),
    val subjects: List<StudentSubject> = emptyList(),
    val recentDiaries: List<StudentDiary> = emptyList(),
    val schedules: List<ScheduleWithDetails> = emptyList(),
    val sessions: List<ClassSessionWithDetails> = emptyList(),
    val exams: List<ExamWithDetails> = emptyList(),
    val selectedTab: ProfileTab = ProfileTab.OVERVIEW,
    val error: String? = null
)

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    private val observeStudentByIdUseCase: ObserveStudentByIdUseCase,
    private val getStudentStatsUseCase: GetStudentStatsUseCase,
    private val observeSubjectsForStudentUseCase: ObserveSubjectsForStudentUseCase,
    private val observeDiaryHistoryUseCase: ObserveDiaryHistoryUseCase,
    private val observeSchedulesForStudentUseCase: ObserveSchedulesForStudentUseCase,
    private val observeSessionsForStudentUseCase: ObserveSessionsForStudentUseCase,
    private val observeExamsForStudentUseCase: ObserveExamsForStudentUseCase,
    private val toggleStudentActiveUseCase: ToggleStudentActiveUseCase,
    private val deleteStudentUseCase: DeleteStudentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studentId: String = checkNotNull(savedStateHandle["studentId"])
    private val _selectedTab = MutableStateFlow(ProfileTab.OVERVIEW)

    val uiState: StateFlow<StudentProfileUiState> = combine(
        combine(
            observeStudentByIdUseCase(studentId),
            getStudentStatsUseCase(studentId),
            observeSubjectsForStudentUseCase(studentId),
            observeDiaryHistoryUseCase(studentId)
        ) { student, stats, subjects, diaries ->
            StudentDataGroup(student, stats, subjects, diaries)
        },
        combine(
            observeSchedulesForStudentUseCase(studentId),
            observeSessionsForStudentUseCase(studentId),
            observeExamsForStudentUseCase(studentId),
            _selectedTab
        ) { schedules, sessions, exams, tab ->
            ActivityDataGroup(schedules, sessions, exams, tab)
        }
    ) { (student, stats, subjects, diaries), (schedules, sessions, exams, tab) ->
        StudentProfileUiState(
            isLoading = false,
            student = student,
            stats = stats,
            subjects = subjects,
            recentDiaries = diaries.take(10),
            schedules = schedules,
            sessions = sessions,
            exams = exams,
            selectedTab = tab,
            error = if (student == null) "Student not found" else null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudentProfileUiState(isLoading = true)
    )

    fun onTabSelected(tab: ProfileTab) {
        _selectedTab.value = tab
    }

    fun toggleActive() {
        val student = uiState.value.student ?: return
        viewModelScope.launch {
            toggleStudentActiveUseCase(student.id, !student.isActive)
        }
    }

    fun deleteStudent(onComplete: () -> Unit) {
        viewModelScope.launch {
            deleteStudentUseCase(studentId)
            onComplete()
        }
    }
}
