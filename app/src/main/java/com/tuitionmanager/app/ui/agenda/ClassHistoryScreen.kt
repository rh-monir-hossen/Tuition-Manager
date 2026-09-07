package com.tuitionmanager.app.ui.agenda

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
import com.tuitionmanager.app.domain.model.ClassSessionWithDetails
import com.tuitionmanager.app.domain.model.SessionStatus
import com.tuitionmanager.app.ui.schedule.SessionStatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassHistoryScreen(
    onNavigateBack: () -> Unit,
    onSessionClick: (String) -> Unit,
    onNavigateToDiaryEntry: (studentId: String, subjectId: String, classSessionId: String) -> Unit,
    onNavigateToDiaryDetail: (diaryId: String) -> Unit,
    viewModel: ClassHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showStudentDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.class_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true
            )

            // Filter Row: Student & Subject
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Student selector chip
                Box {
                    FilterChip(
                        selected = uiState.selectedStudentId != null,
                        onClick = { showStudentDropdown = true },
                        label = {
                            val studentName = uiState.availableStudents.firstOrNull { it.id == uiState.selectedStudentId }?.name
                            Text(studentName ?: stringResource(R.string.all_students))
                        },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
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
                        uiState.availableStudents.forEach { student ->
                            DropdownMenuItem(
                                text = { Text(student.name) },
                                onClick = {
                                    viewModel.onStudentSelected(student.id)
                                    showStudentDropdown = false
                                }
                            )
                        }
                    }
                }

                // Subject filter chips (when student is selected)
                if (uiState.availableSubjects.isNotEmpty()) {
                    FilterChip(
                        selected = uiState.selectedSubjectId == null,
                        onClick = { viewModel.onSubjectSelected(null) },
                        label = { Text(stringResource(R.string.all_subjects)) }
                    )
                    uiState.availableSubjects.forEach { subject ->
                        FilterChip(
                            selected = uiState.selectedSubjectId == subject.id,
                            onClick = { viewModel.onSubjectSelected(subject.id) },
                            label = { Text(subject.subjectName) }
                        )
                    }
                }
            }

            // Status Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedStatus == null,
                    onClick = { viewModel.onStatusSelected(null) },
                    label = { Text(stringResource(R.string.all_status)) }
                )
                SessionStatus.values().forEach { status ->
                    FilterChip(
                        selected = uiState.selectedStatus == status,
                        onClick = { viewModel.onStatusSelected(status) },
                        label = { Text(status.displayName) }
                    )
                }
            }

            // Stats summary strip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MetricItem(label = stringResource(R.string.total), count = uiState.totalCount)
                    MetricItem(label = stringResource(R.string.status_completed), count = uiState.completedCount)
                    MetricItem(label = stringResource(R.string.status_missed), count = uiState.missedCount)
                    MetricItem(label = stringResource(R.string.status_scheduled), count = uiState.scheduledCount)
                }
            }

            // Session List
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_class_history_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.sessions, key = { it.session.id }) { item ->
                        ClassHistorySessionCard(
                            item = item,
                            onSessionClick = { onSessionClick(item.session.id) },
                            onDiaryClick = {
                                if (item.hasDiaryEntry && item.diaryEntryId != null) {
                                    onNavigateToDiaryDetail(item.diaryEntryId)
                                } else {
                                    onNavigateToDiaryEntry(
                                        item.session.studentId,
                                        item.session.subjectId,
                                        item.session.id
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ClassHistorySessionCard(
    item: ClassSessionWithDetails,
    onSessionClick: () -> Unit,
    onDiaryClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSessionClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.subjectName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                SessionStatusChip(status = item.session.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.dateFormatted} (${item.dayFormatted})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.timeFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!item.session.topicCovered.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.topic_label, item.session.topicCovered ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.hasDiaryEntry) {
                    FilledTonalButton(
                        onClick = onDiaryClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.view_diary), style = MaterialTheme.typography.labelSmall)
                    }
                } else if (item.session.status == SessionStatus.COMPLETED) {
                    OutlinedButton(
                        onClick = onDiaryClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add_diary_entry), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
