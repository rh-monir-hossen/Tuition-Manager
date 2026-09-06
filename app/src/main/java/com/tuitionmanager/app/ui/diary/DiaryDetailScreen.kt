package com.tuitionmanager.app.ui.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tuitionmanager.app.R
import com.tuitionmanager.app.domain.model.HomeworkStatus
import com.tuitionmanager.app.domain.model.StudentUnderstanding
import com.tuitionmanager.app.ui.components.ConfirmActionDialog
import com.tuitionmanager.app.ui.components.ErrorStateView
import com.tuitionmanager.app.ui.components.LoadingStateView
import com.tuitionmanager.app.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(
    viewModel: DiaryDetailViewModel,
    onNavigateBack: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_diary_details),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    uiState.diary?.let { diary ->
                        IconButton(onClick = { onEditClick(diary.id) }) {
                            Icon(imageVector = Icons.Outlined.Edit, contentDescription = stringResource(R.string.action_edit))
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingStateView(message = "Loading lesson record…")
        } else if (uiState.diary == null) {
            ErrorStateView(
                message = uiState.error ?: "Record not found",
                onRetry = onNavigateBack
            )
        } else {
            val diary = uiState.diary!!
            val student = uiState.student
            val subject = uiState.subject

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Soft-deleted notice banner
                if (diary.isDeleted) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "This record has been deleted.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = { viewModel.restoreDiary() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(stringResource(R.string.action_restore))
                            }
                        }
                    }
                }

                // Header Card with Student, Date & Badges
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = student?.name ?: "Student Lesson",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = TimeUtils.formatLocalDate(diary.date),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        subject?.let {
                            Text(
                                text = "Subject: ${it.subjectName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Badges Row: Understanding + Homework Status
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            val (uColor, uText) = when (diary.studentUnderstanding) {
                                StudentUnderstanding.EXCELLENT -> Color(0xFF2E7D32) to "Comprehension: Excellent"
                                StudentUnderstanding.GOOD -> Color(0xFF0288D1) to "Comprehension: Good"
                                StudentUnderstanding.AVERAGE -> Color(0xFFF57C00) to "Comprehension: Average"
                                StudentUnderstanding.NEEDS_ATTENTION -> MaterialTheme.colorScheme.error to "Needs Attention"
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = uColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = uText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = uColor,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            val hwLabel = when (diary.homeworkStatus) {
                                HomeworkStatus.ASSIGNED -> "HW: Assigned"
                                HomeworkStatus.SUBMITTED_COMPLETE -> "HW: Complete"
                                HomeworkStatus.SUBMITTED_PARTIAL -> "HW: Partial"
                                HomeworkStatus.NOT_SUBMITTED -> "HW: Not Submitted"
                                HomeworkStatus.NONE -> "HW: None"
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = hwLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Section 1: Topic Covered
                DetailBlock(
                    icon = Icons.Outlined.Bookmark,
                    title = "Topic / Chapter",
                    content = diary.topicTitle,
                    isHighlight = true
                )

                // Section 2: What Was Taught
                DetailBlock(
                    icon = Icons.Outlined.School,
                    title = "What Was Taught in Class",
                    content = diary.whatWasTaught
                )

                // Section 3: Homework Assigned
                if (!diary.homeworkAssigned.isNullOrBlank()) {
                    DetailBlock(
                        icon = Icons.Outlined.Assignment,
                        title = "Homework Assigned",
                        content = diary.homeworkAssigned
                    )
                }

                // Section 4: Practice / In-Class Exercises
                if (!diary.practiceGiven.isNullOrBlank()) {
                    DetailBlock(
                        icon = Icons.Outlined.Edit,
                        title = "Practice & In-Class Exercises",
                        content = diary.practiceGiven
                    )
                }

                // Section 5: Teacher Remarks
                if (!diary.teacherRemarks.isNullOrBlank()) {
                    DetailBlock(
                        icon = Icons.Outlined.Notes,
                        title = "Teacher Remarks & Observations",
                        content = diary.teacherRemarks
                    )
                }

                // Section 6: Next Class Plan
                if (!diary.nextClassPlan.isNullOrBlank()) {
                    DetailBlock(
                        icon = Icons.Outlined.Schedule,
                        title = "Next Class Plan",
                        content = diary.nextClassPlan
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_delete_diary_title),
            message = stringResource(R.string.dialog_delete_diary_message),
            confirmText = stringResource(R.string.action_delete),
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteDiary(onComplete = onNavigateBack)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun DetailBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String,
    isHighlight: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlight) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = content,
                style = if (isHighlight) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                fontWeight = if (isHighlight) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
