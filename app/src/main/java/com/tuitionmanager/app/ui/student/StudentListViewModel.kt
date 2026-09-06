package com.tuitionmanager.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuitionmanager.app.domain.model.Student
import com.tuitionmanager.app.domain.usecase.DeleteStudentUseCase
import com.tuitionmanager.app.domain.usecase.ObserveStudentsUseCase
import com.tuitionmanager.app.domain.usecase.RestoreStudentUseCase
import com.tuitionmanager.app.domain.usecase.ToggleStudentActiveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StudentFilter {
    ALL,
    ACTIVE,
    INACTIVE
}

enum class StudentSort {
    NAME_ASC,
    RECENT
}

data class StudentListUiState(
    val isLoading: Boolean = true,
    val students: List<Student> = emptyList(),
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val inactiveCount: Int = 0,
    val searchQuery: String = "",
    val filter: StudentFilter = StudentFilter.ALL,
    val sort: StudentSort = StudentSort.NAME_ASC,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val observeStudentsUseCase: ObserveStudentsUseCase,
    private val deleteStudentUseCase: DeleteStudentUseCase,
    private val restoreStudentUseCase: RestoreStudentUseCase,
    private val toggleStudentActiveUseCase: ToggleStudentActiveUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(StudentFilter.ALL)
    private val _sort = MutableStateFlow(StudentSort.NAME_ASC)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<StudentListUiState> = combine(
        _searchQuery,
        _filter,
        _sort,
        _error
    ) { query, filter, sort, error ->
        FilterSortQuery(query, filter, sort, error)
    }.flatMapLatest { fsq ->
        val includeInactive = fsq.filter != StudentFilter.ACTIVE
        val sortByRecent = fsq.sort == StudentSort.RECENT

        observeStudentsUseCase(
            includeInactive = includeInactive,
            sortByRecent = sortByRecent,
            query = fsq.query
        ).map { list ->
            val finalFiltered = when (fsq.filter) {
                StudentFilter.ALL -> list
                StudentFilter.ACTIVE -> list.filter { it.isActive && !it.isDeleted }
                StudentFilter.INACTIVE -> list.filter { !it.isActive && !it.isDeleted }
            }
            StudentListUiState(
                isLoading = false,
                students = finalFiltered,
                totalCount = list.size,
                activeCount = list.count { it.isActive && !it.isDeleted },
                inactiveCount = list.count { !it.isActive && !it.isDeleted },
                searchQuery = fsq.query,
                filter = fsq.filter,
                sort = fsq.sort,
                error = fsq.error
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudentListUiState(isLoading = true)
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(filter: StudentFilter) {
        _filter.value = filter
    }

    fun onSortChanged(sort: StudentSort) {
        _sort.value = sort
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            deleteStudentUseCase(studentId)
        }
    }

    fun restoreStudent(studentId: String) {
        viewModelScope.launch {
            restoreStudentUseCase(studentId)
        }
    }

    fun toggleStudentActive(studentId: String, currentActive: Boolean) {
        viewModelScope.launch {
            toggleStudentActiveUseCase(studentId, !currentActive)
        }
    }

    private data class FilterSortQuery(
        val query: String,
        val filter: StudentFilter,
        val sort: StudentSort,
        val error: String?
    )
}
