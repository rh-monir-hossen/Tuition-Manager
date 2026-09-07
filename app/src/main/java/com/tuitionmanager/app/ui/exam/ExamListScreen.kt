package com.tuitionmanager.app.ui.exam

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tuitionmanager.app.R
import com.tuitionmanager.app.domain.model.ExamStatus
import com.tuitionmanager.app.domain.model.ExamType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamListScreen(
    onNavigateBack: () -> Unit,
    onExamClick: (String) -> Unit,
    onAddExamClick: (String?) -> Unit,
    onEnterResultClick: (String) -> Unit,
    viewModel: ExamListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showStudentDropdown by remember { mutableStateOf(false) }
    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    val selectedStudent = uiState.students.firstOrNull { it.id == uiState.filters.studentId }
    val selectedSubject = uiState.subjects.firstOrNull { it.id == uiState.filters.subjectId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.title_exams),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedStudent?.let { "Student: ${it.name}" } ?: stringResource(R.string.subtitle_exams),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddExamClick(uiState.filters.studentId) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add_exam))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = uiState.filters.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_exams_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.filters.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true
            )

            // Horizontal Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Student Filter
                Box {
                    FilterChip(
                        selected = uiState.filters.studentId != null,
                        onClick = { showStudentDropdown = true },
                        label = {
                            Text(selectedStudent?.name ?: stringResource(R.string.all_students))
                        },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    DropdownMenu(
                        expanded = showStudentDropdown,
                        onDismissRequest = { showStudentDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.all_students)) },
                            onClick = {
                                viewModel.onStudentSelected(null)
                                showStudentDropdown = false
                            }
                        )
                        uiState.students.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.name) },
                                onClick = {
                                    viewModel.onStudentSelected(s.id)
                                    showStudentDropdown = false
                                }
                            )
                        }
                    }
                }

                // Subject Filter
                if (uiState.subjects.isNotEmpty()) {
                    Box {
                        FilterChip(
                            selected = uiState.filters.subjectId != null,
                            onClick = { showSubjectDropdown = true },
                            label = {
                                Text(selectedSubject?.subjectName ?: stringResource(R.string.all_subjects))
                            },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                        DropdownMenu(
                            expanded = showSubjectDropdown,
                            onDismissRequest = { showSubjectDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.all_subjects)) },
                                onClick = {
                                    viewModel.onSubjectSelected(null)
                                    showSubjectDropdown = false
                                }
                            )
                            uiState.subjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj.subjectName) },
                                    onClick = {
                                        viewModel.onSubjectSelected(subj.id)
                                        showSubjectDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Exam Type Filter
                Box {
                    FilterChip(
                        selected = uiState.filters.examType != null,
                        onClick = { showTypeDropdown = true },
                        label = {
                            Text(
                                uiState.filters.examType?.let {
                                    when (it) {
                                        ExamType.WEEKLY_QUIZ -> stringResource(R.string.exam_type_weekly_quiz)
                                        ExamType.CHAPTER_TEST -> stringResource(R.string.exam_type_chapter_test)
                                        ExamType.MONTHLY_ASSESSMENT -> stringResource(R.string.exam_type_monthly_assessment)
                                        ExamType.MID_TERM -> stringResource(R.string.exam_type_mid_term)
                                        ExamType.FINAL_MODEL_TEST -> stringResource(R.string.exam_type_final_model_test)
                                        ExamType.SURPRISE_TEST -> stringResource(R.string.exam_type_surprise_test)
                                    }
                                } ?: stringResource(R.string.all_types)
                            )
                        },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    DropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.all_types)) },
                            onClick = {
                                viewModel.onExamTypeSelected(null)
                                showTypeDropdown = false
                            }
                        )
                        ExamType.values().forEach { t ->
                            val textRes = when (t) {
                                ExamType.WEEKLY_QUIZ -> R.string.exam_type_weekly_quiz
                                ExamType.CHAPTER_TEST -> R.string.exam_type_chapter_test
                                ExamType.MONTHLY_ASSESSMENT -> R.string.exam_type_monthly_assessment
                                ExamType.MID_TERM -> R.string.exam_type_mid_term
                                ExamType.FINAL_MODEL_TEST -> R.string.exam_type_final_model_test
                                ExamType.SURPRISE_TEST -> R.string.exam_type_surprise_test
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(textRes)) },
                                onClick = {
                                    viewModel.onExamTypeSelected(t)
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                // Status Filter
                Box {
                    FilterChip(
                        selected = uiState.filters.status != null,
                        onClick = { showStatusDropdown = true },
                        label = {
                            Text(
                                uiState.filters.status?.let {
                                    when (it) {
                                        ExamStatus.PLANNED -> stringResource(R.string.exam_status_planned)
                                        ExamStatus.COMPLETED -> stringResource(R.string.exam_status_completed)
                                        ExamStatus.MISSED -> stringResource(R.string.exam_status_missed)
                                        ExamStatus.CANCELLED -> stringResource(R.string.exam_status_cancelled)
                                        ExamStatus.RESCHEDULED -> stringResource(R.string.exam_status_rescheduled)
                                    }
                                } ?: stringResource(R.string.all_status)
                            )
                        },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.all_status)) },
                            onClick = {
                                viewModel.onStatusSelected(null)
                                showStatusDropdown = false
                            }
                        )
                        ExamStatus.values().forEach { s ->
                            val textRes = when (s) {
                                ExamStatus.PLANNED -> R.string.exam_status_planned
                                ExamStatus.COMPLETED -> R.string.exam_status_completed
                                ExamStatus.MISSED -> R.string.exam_status_missed
                                ExamStatus.CANCELLED -> R.string.exam_status_cancelled
                                ExamStatus.RESCHEDULED -> R.string.exam_status_rescheduled
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(textRes)) },
                                onClick = {
                                    viewModel.onStatusSelected(s)
                                    showStatusDropdown = false
                                }
                            )
                        }
                    }
                }

                // Clear Filters Chip
                val hasActiveFilters = uiState.filters.studentId != null ||
                        uiState.filters.subjectId != null ||
                        uiState.filters.examType != null ||
                        uiState.filters.status != null ||
                        uiState.filters.searchQuery.isNotEmpty()

                if (hasActiveFilters) {
                    AssistChip(
                        onClick = { viewModel.clearFilters() },
                        label = { Text("Clear") },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }

            // Exam List Content
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.exams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = stringResource(R.string.empty_exams_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.empty_exams_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { onAddExamClick(uiState.filters.studentId) }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_add_exam))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.exams,
                        key = { it.exam.id }
                    ) { examDetails ->
                        ExamCard(
                            examDetails = examDetails,
                            onClick = { onExamClick(examDetails.exam.id) },
                            onEnterResultClick = if (examDetails.result == null) {
                                { onEnterResultClick(examDetails.exam.id) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}
