package com.tuitionmanager.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tuitionmanager.app.R
import com.tuitionmanager.app.domain.model.Student
import com.tuitionmanager.app.ui.components.ConfirmActionDialog
import com.tuitionmanager.app.ui.components.EmptyStateView
import com.tuitionmanager.app.ui.components.LoadingStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    viewModel: StudentListViewModel,
    onStudentClick: (String) -> Unit,
    onAddStudentClick: () -> Unit,
    onAddDiaryClick: (String) -> Unit,
    onViewDiaryHistoryClick: (String) -> Unit,
    onRoutineClick: () -> Unit,
    onAgendaClick: () -> Unit,
    onClassHistoryClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var studentToDelete by remember { mutableStateOf<Student?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.title_students),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (!uiState.isLoading) {
                            Text(
                                text = "${uiState.activeCount} active · ${uiState.totalCount} total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onAgendaClick) {
                        Icon(
                            imageVector = Icons.Outlined.Today,
                            contentDescription = stringResource(R.string.title_daily_agenda),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onRoutineClick) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = stringResource(R.string.title_weekly_routine),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onClassHistoryClick) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = stringResource(R.string.class_history_title),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Sort,
                                contentDescription = "Sort"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_name_asc)) },
                                onClick = {
                                    viewModel.onSortChanged(StudentSort.NAME_ASC)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (uiState.sort == StudentSort.NAME_ASC) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_recent)) },
                                onClick = {
                                    viewModel.onSortChanged(StudentSort.RECENT)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (uiState.sort == StudentSort.RECENT) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddStudentClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.title_add_student)
                )
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
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = {
                    Text(
                        stringResource(R.string.search_students_hint),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filter == StudentFilter.ALL,
                    onClick = { viewModel.onFilterChanged(StudentFilter.ALL) },
                    label = { Text(stringResource(R.string.filter_all)) }
                )
                FilterChip(
                    selected = uiState.filter == StudentFilter.ACTIVE,
                    onClick = { viewModel.onFilterChanged(StudentFilter.ACTIVE) },
                    label = { Text(stringResource(R.string.filter_active)) }
                )
                FilterChip(
                    selected = uiState.filter == StudentFilter.INACTIVE,
                    onClick = { viewModel.onFilterChanged(StudentFilter.INACTIVE) },
                    label = { Text(stringResource(R.string.filter_inactive)) }
                )
            }

            if (uiState.isLoading) {
                LoadingStateView(message = "Loading students…")
            } else if (uiState.students.isEmpty()) {
                if (uiState.searchQuery.isNotBlank()) {
                    EmptyStateView(
                        icon = Icons.Outlined.SearchOff,
                        title = stringResource(R.string.empty_students_title),
                        description = stringResource(R.string.empty_students_search_desc)
                    )
                } else {
                    EmptyStateView(
                        icon = Icons.Outlined.PersonAdd,
                        title = stringResource(R.string.empty_students_title),
                        description = stringResource(R.string.empty_students_desc),
                        actionButtonText = stringResource(R.string.title_add_student),
                        onActionClick = onAddStudentClick
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.students, key = { it.id }) { student ->
                        StudentCard(
                            student = student,
                            onClick = { onStudentClick(student.id) },
                            onAddDiary = { onAddDiaryClick(student.id) },
                            onViewDiaryHistory = { onViewDiaryHistoryClick(student.id) },
                            onToggleActive = { viewModel.toggleStudentActive(student.id, student.isActive) },
                            onDelete = { studentToDelete = student }
                        )
                    }
                }
            }
        }
    }

    studentToDelete?.let { student ->
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_delete_student_title),
            message = stringResource(R.string.dialog_delete_student_message),
            confirmText = stringResource(R.string.action_delete),
            onConfirm = {
                viewModel.deleteStudent(student.id)
                studentToDelete = null
            },
            onDismiss = { studentToDelete = null }
        )
    }
}

@Composable
private fun StudentCard(
    student: Student,
    onClick: () -> Unit,
    onAddDiary: () -> Unit,
    onViewDiaryHistory: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Initial Circle Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (student.isActive) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (student.isActive) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!student.isActive) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Inactive",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    val detailLine = listOfNotNull(
                        student.classGrade?.takeIf { it.isNotBlank() },
                        student.institution?.takeIf { it.isNotBlank() }
                    ).joinToString(" • ")

                    if (detailLine.isNotBlank()) {
                        Text(
                            text = detailLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add Diary Entry") },
                            onClick = {
                                showMenu = false
                                onAddDiary()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.EditNote, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Diary History") },
                            onClick = {
                                showMenu = false
                                onViewDiaryHistory()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.HistoryEdu, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(if (student.isActive) "Mark Inactive" else "Mark Active")
                            },
                            onClick = {
                                showMenu = false
                                onToggleActive()
                            },
                            leadingIcon = {
                                Icon(
                                    if (student.isActive) Icons.Outlined.PauseCircle
                                    else Icons.Outlined.PlayCircle,
                                    contentDescription = null
                                )
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete Student",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            // Quick Info Badges: Fee, Phone, Diary Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (student.monthlyFeeAmount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "৳ ${student.monthlyFeeAmount.toInt()} / mo",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (!student.phone.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = student.phone,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Quick Add Diary Chip button
                OutlinedButton(
                    onClick = onAddDiary,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ Diary",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
