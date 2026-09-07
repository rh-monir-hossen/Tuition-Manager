package com.tuitionmanager.app.ui.exam

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.tuitionmanager.app.R
import com.tuitionmanager.app.domain.model.ExamStatus
import com.tuitionmanager.app.ui.components.ConfirmActionDialog
import com.tuitionmanager.app.ui.components.LoadingStateView
import com.tuitionmanager.app.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDetailScreen(
    onNavigateBack: () -> Unit,
    onEditExamClick: (String) -> Unit,
    onEnterResultClick: (String) -> Unit,
    onEditResultClick: (String) -> Unit,
    onStudentClick: (String) -> Unit,
    viewModel: ExamDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showDeleteExamDialog by remember { mutableStateOf(false) }
    var showDeleteResultDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val details = uiState.examDetails
    val exam = details?.exam
    val result = details?.result

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_exam_details), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (exam != null) {
                        IconButton(onClick = { onEditExamClick(exam.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.title_edit_exam))
                        }
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                if (exam.status != ExamStatus.CANCELLED) {
                                    DropdownMenuItem(
                                        text = { Text("Cancel Exam") },
                                        leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null) },
                                        onClick = {
                                            viewModel.cancelExam()
                                            showMoreMenu = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showMoreMenu = false
                                        showDeleteExamDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading || details == null || exam == null) {
            LoadingStateView(modifier = Modifier.padding(paddingValues))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ExamTypeChip(type = exam.examType)
                            ExamStatusChip(status = exam.status)
                        }

                        Text(
                            text = exam.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (exam.syllabusTopic.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = stringResource(R.string.label_syllabus_topic),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = exam.syllabusTopic,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Student and Subject Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.all_students),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = details.studentName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = stringResource(R.string.all_subjects),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = details.subjectName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Schedule & Marks Info Grid
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Assessment Logistics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Planned Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(details.dateFormatted, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Time & Duration", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${details.timeFormatted} (${exam.durationMinutes} min)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Marks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val tot = if (exam.totalMarks % 1.0 == 0.0) exam.totalMarks.toInt().toString() else exam.totalMarks.toString()
                                Text(tot, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Passing Marks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val pass = exam.passingMarks?.let {
                                    if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
                                } ?: "None specified"
                                Text(pass, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (!exam.notes.isNullOrBlank()) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Preparation Notes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(exam.notes, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Result Section
                if (result != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Exam Outcome",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                PassFailBadge(isPassed = result.isPassed)
                            }

                            // Score Banner
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (result.isPassed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val obtained = if (result.marksObtained % 1.0 == 0.0) result.marksObtained.toInt().toString() else result.marksObtained.toString()
                                    val total = if (exam.totalMarks % 1.0 == 0.0) exam.totalMarks.toInt().toString() else exam.totalMarks.toString()
                                    val pct = details.percentage?.let { String.format("%.1f", it) } ?: "0.0"

                                    Text(
                                        text = "$obtained / $total",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (result.isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                    Text(
                                        text = "$pct% • ${details.gradeLevel ?: ""}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Exam dates
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Conducted Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(TimeUtils.formatLocalDate(result.actualExamDate, "dd MMM yyyy"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Graded Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(TimeUtils.formatLocalDate(result.gradedAt, "dd MMM yyyy"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                            }

                            // Strengths
                            if (!result.studentStrengths.isNullOrBlank()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = stringResource(R.string.label_student_strengths),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(text = result.studentStrengths, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            // Weaknesses
                            if (!result.studentWeaknesses.isNullOrBlank()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = stringResource(R.string.label_student_weaknesses),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828)
                                    )
                                    Text(text = result.studentWeaknesses, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            // Recommendations
                            if (!result.recommendations.isNullOrBlank()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = stringResource(R.string.label_recommendations),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(text = result.recommendations, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            // Remarks
                            if (!result.teacherRemarks.isNullOrBlank()) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = stringResource(R.string.label_result_teacher_remarks),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(text = result.teacherRemarks, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onEditResultClick(exam.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.action_edit_result))
                                }

                                OutlinedButton(
                                    onClick = { showDeleteResultDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.action_delete_result))
                                }
                            }
                        }
                    }
                } else if (exam.status != ExamStatus.CANCELLED) {
                    // Prompt to enter result
                    Button(
                        onClick = { onEnterResultClick(exam.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.action_record_result), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDeleteExamDialog) {
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_delete_exam_title),
            message = stringResource(R.string.dialog_delete_exam_message),
            confirmText = stringResource(R.string.delete),
            onConfirm = {
                showDeleteExamDialog = false
                viewModel.deleteExam(onSuccess = onNavigateBack)
            },
            onDismiss = { showDeleteExamDialog = false }
        )
    }

    if (showDeleteResultDialog) {
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_delete_result_title),
            message = stringResource(R.string.dialog_delete_result_message),
            confirmText = stringResource(R.string.delete),
            onConfirm = {
                showDeleteResultDialog = false
                viewModel.deleteResult()
            },
            onDismiss = { showDeleteResultDialog = false }
        )
    }
}
