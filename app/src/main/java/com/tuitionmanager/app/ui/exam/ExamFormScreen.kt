package com.tuitionmanager.app.ui.exam

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tuitionmanager.app.R
import com.tuitionmanager.app.domain.model.ExamStatus
import com.tuitionmanager.app.domain.model.ExamType
import com.tuitionmanager.app.ui.components.AppTimePickerDialog
import com.tuitionmanager.app.ui.components.TimeSelectionField
import com.tuitionmanager.app.utils.TimeUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExamFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showTimePicker by remember { mutableStateOf(false) }
    var showStudentMenu by remember { mutableStateOf(false) }
    var showSubjectMenu by remember { mutableStateOf(false) }

    val selectedStudent = uiState.students.firstOrNull { it.id == uiState.studentId }
    val selectedSubject = uiState.subjects.firstOrNull { it.id == uiState.subjectId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) {
                            stringResource(R.string.title_edit_exam)
                        } else {
                            stringResource(R.string.title_add_exam)
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveExam(onSuccess = onNavigateBack) },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = stringResource(R.string.action_save),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error Message Banner
            uiState.errorMessage?.let { errorMsg ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Student & Subject Section
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Student & Subject",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Student Dropdown
                    ExposedDropdownMenuBox(
                        expanded = showStudentMenu,
                        onExpandedChange = { showStudentMenu = it }
                    ) {
                        OutlinedTextField(
                            value = selectedStudent?.name ?: "Select Student",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.label_select_student)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStudentMenu) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showStudentMenu,
                            onDismissRequest = { showStudentMenu = false }
                        ) {
                            uiState.students.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        viewModel.onStudentChanged(s.id)
                                        showStudentMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Subject Dropdown
                    ExposedDropdownMenuBox(
                        expanded = showSubjectMenu,
                        onExpandedChange = { showSubjectMenu = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject?.subjectName ?: if (uiState.subjects.isEmpty()) "No subjects found" else "Select Subject",
                            onValueChange = {},
                            readOnly = true,
                            enabled = uiState.subjects.isNotEmpty(),
                            label = { Text(stringResource(R.string.label_select_subject)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSubjectMenu) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showSubjectMenu,
                            onDismissRequest = { showSubjectMenu = false }
                        ) {
                            uiState.subjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj.subjectName) },
                                    onClick = {
                                        viewModel.onSubjectChanged(subj.id)
                                        showSubjectMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Exam Info Section
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Exam Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.onTitleChanged(it) },
                        label = { Text(stringResource(R.string.label_exam_title)) },
                        placeholder = { Text(stringResource(R.string.placeholder_exam_title)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.syllabusTopic,
                        onValueChange = { viewModel.onSyllabusTopicChanged(it) },
                        label = { Text(stringResource(R.string.label_syllabus_topic)) },
                        placeholder = { Text(stringResource(R.string.placeholder_syllabus_topic)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Exam Type Selector Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.label_exam_type),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExamType.values().forEach { type ->
                                val textRes = when (type) {
                                    ExamType.WEEKLY_QUIZ -> R.string.exam_type_weekly_quiz
                                    ExamType.CHAPTER_TEST -> R.string.exam_type_chapter_test
                                    ExamType.MONTHLY_ASSESSMENT -> R.string.exam_type_monthly_assessment
                                    ExamType.MID_TERM -> R.string.exam_type_mid_term
                                    ExamType.FINAL_MODEL_TEST -> R.string.exam_type_final_model_test
                                    ExamType.SURPRISE_TEST -> R.string.exam_type_surprise_test
                                }
                                FilterChip(
                                    selected = uiState.examType == type,
                                    onClick = { viewModel.onExamTypeChanged(type) },
                                    label = { Text(stringResource(textRes)) }
                                )
                            }
                        }
                    }

                    // Exam Status (if edit mode)
                    if (uiState.isEditMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.label_exam_status),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExamStatus.values().forEach { status ->
                                    val textRes = when (status) {
                                        ExamStatus.PLANNED -> R.string.exam_status_planned
                                        ExamStatus.COMPLETED -> R.string.exam_status_completed
                                        ExamStatus.MISSED -> R.string.exam_status_missed
                                        ExamStatus.CANCELLED -> R.string.exam_status_cancelled
                                        ExamStatus.RESCHEDULED -> R.string.exam_status_rescheduled
                                    }
                                    FilterChip(
                                        selected = uiState.status == status,
                                        onClick = { viewModel.onStatusChanged(status) },
                                        label = { Text(stringResource(textRes)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Schedule & Marks Section
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Schedule & Marks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Date Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.label_planned_date),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = TimeUtils.formatLocalDate(uiState.plannedDate, "EEEE, dd MMMM yyyy"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = uiState.plannedDate }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val newCal = Calendar.getInstance().apply {
                                            set(y, m, d, 12, 0, 0)
                                        }
                                        viewModel.onPlannedDateChanged(newCal.timeInMillis)
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Date")
                        }
                    }

                    // Start Time and Duration Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TimeSelectionField(
                            label = stringResource(R.string.label_planned_start_time),
                            minutesFromMidnight = uiState.plannedStartTimeMinutes,
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = uiState.durationMinutes.toString(),
                            onValueChange = {
                                val mins = it.toIntOrNull() ?: 0
                                viewModel.onDurationChanged(mins)
                            },
                            label = { Text(stringResource(R.string.label_duration)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Total Marks & Passing Marks Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.totalMarksText,
                            onValueChange = { viewModel.onTotalMarksChanged(it) },
                            label = { Text(stringResource(R.string.label_total_marks)) },
                            placeholder = { Text(stringResource(R.string.placeholder_total_marks)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.passingMarksText,
                            onValueChange = { viewModel.onPassingMarksChanged(it) },
                            label = { Text(stringResource(R.string.label_passing_marks)) },
                            placeholder = { Text(stringResource(R.string.placeholder_passing_marks)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // Notes Section
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_exam_notes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onNotesChanged(it) },
                        placeholder = { Text(stringResource(R.string.placeholder_exam_notes)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )
                }
            }

            // Save Button
            Button(
                onClick = { viewModel.saveExam(onSuccess = onNavigateBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.action_save),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showTimePicker) {
        AppTimePickerDialog(
            initialMinutes = uiState.plannedStartTimeMinutes,
            title = stringResource(R.string.label_planned_start_time),
            onTimeSelected = {
                viewModel.onStartTimeChanged(it)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
