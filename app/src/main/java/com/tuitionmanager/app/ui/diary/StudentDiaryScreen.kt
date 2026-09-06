package com.tuitionmanager.app.ui.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tuitionmanager.app.R
import com.tuitionmanager.app.ui.components.EmptyStateView
import com.tuitionmanager.app.ui.components.LoadingStateView
import com.tuitionmanager.app.ui.student.DiaryItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDiaryScreen(
    viewModel: StudentDiaryViewModel,
    onNavigateBack: () -> Unit,
    onAddDiaryClick: (String?) -> Unit,
    onDiaryClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.student?.let { "${it.name}'s Diary" }
                                ?: stringResource(R.string.title_student_diary),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!uiState.isLoading) {
                            Text(
                                text = "${uiState.entries.size} recorded lessons",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddDiaryClick(uiState.studentId) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.title_new_diary_entry)
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
                        stringResource(R.string.search_diary_hint),
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

            // Subject Filter Chips (if subjects exist)
            if (uiState.subjects.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedSubjectId == null,
                            onClick = { viewModel.onSubjectSelected(null) },
                            label = { Text(stringResource(R.string.filter_all_subjects)) }
                        )
                    }
                    items(uiState.subjects, key = { it.id }) { subject ->
                        FilterChip(
                            selected = uiState.selectedSubjectId == subject.id,
                            onClick = { viewModel.onSubjectSelected(subject.id) },
                            label = { Text(subject.subjectName) }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                LoadingStateView(message = "Loading diary entries…")
            } else if (uiState.entries.isEmpty()) {
                if (uiState.searchQuery.isNotBlank()) {
                    EmptyStateView(
                        icon = Icons.Outlined.SearchOff,
                        title = stringResource(R.string.empty_diary_title),
                        description = stringResource(R.string.empty_diary_search_desc)
                    )
                } else {
                    EmptyStateView(
                        icon = Icons.Outlined.HistoryEdu,
                        title = stringResource(R.string.empty_diary_title),
                        description = stringResource(R.string.empty_diary_desc),
                        actionButtonText = stringResource(R.string.title_new_diary_entry),
                        onActionClick = { onAddDiaryClick(uiState.studentId) }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.entries, key = { it.id }) { diary ->
                        DiaryItemCard(
                            diary = diary,
                            onClick = { onDiaryClick(diary.id) }
                        )
                    }
                }
            }
        }
    }
}
