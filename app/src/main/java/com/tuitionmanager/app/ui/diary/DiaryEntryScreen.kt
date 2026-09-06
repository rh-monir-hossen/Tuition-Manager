package com.tuitionmanager.app.ui.diary

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tuitionmanager.app.R
import com.tuitionmanager.app.domain.model.HomeworkStatus
import com.tuitionmanager.app.domain.model.StudentUnderstanding
import com.tuitionmanager.app.ui.components.FormSectionTitle
import com.tuitionmanager.app.ui.components.LoadingStateView
import com.tuitionmanager.app.utils.TimeUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryScreen(
    viewModel: DiaryEntryViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showStudentDropdown by remember { mutableStateOf(false) }
    var showNewSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) stringResource(R.string.title_edit_diary_entry)
                        else stringResource(R.string.title_new_diary_entry),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveDiaryEntry() },
                        enabled = uiState.isFormValid && !uiState.isSaving,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(R.string.action_saving))
                        } else {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.action_save))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingStateView(message = "Loading diary details…")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.generalError?.let { error ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Student Selector
                FormSectionTitle(title = "Student & Date")

                ExposedDropdownMenuBox(
                    expanded = showStudentDropdown,
                    onExpandedChange = { showStudentDropdown = !showStudentDropdown }
                ) {
                    val selectedStudent = uiState.availableStudents.find { it.id == uiState.studentId }
                    OutlinedTextField(
                        value = selectedStudent?.name ?: "Select student",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.label_select_student)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStudentDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = showStudentDropdown,
                        onDismissRequest = { showStudentDropdown = false }
                    ) {
                        uiState.availableStudents.forEach { student ->
                            DropdownMenuItem(
                                text = { Text("${student.name} (${student.classGrade ?: "Student"})") },
                                onClick = {
                                    viewModel.onStudentSelected(student.id)
                                    showStudentDropdown = false
                                }
                            )
                        }
                    }
                }

                // Date Picker Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.label_diary_date),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = TimeUtils.formatLocalDate(uiState.dateEpochMs, "EEEE, dd MMMM yyyy"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val cal = Calendar.getInstance().apply { timeInMillis = uiState.dateEpochMs }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(y, m, d, 12, 0, 0)
                                    }
                                    viewModel.onDateChanged(newCal.timeInMillis)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Change Date")
                    }
                }

                // Subject Selector Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.label_select_subject),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (uiState.studentId.isNotBlank()) {
                            TextButton(onClick = { showNewSubjectDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.action_add_subject), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    if (uiState.availableSubjects.isEmpty()) {
                        Text(
                            text = "No subjects registered for this student yet. Tap '+ Add Subject' above to create one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.availableSubjects, key = { it.id }) { subject ->
                                FilterChip(
                                    selected = uiState.subjectId == subject.id,
                                    onClick = { viewModel.onSubjectSelected(subject) },
                                    label = { Text(subject.subjectName) }
                                )
                            }
                        }
                    }
                }

                // Section 2: Lesson Content
                FormSectionTitle(title = "Lesson Content")

                OutlinedTextField(
                    value = uiState.topicTitle,
                    onValueChange = { viewModel.onTopicTitleChanged(it) },
                    label = { Text(stringResource(R.string.label_topic_title)) },
                    placeholder = { Text(stringResource(R.string.placeholder_topic_title)) },
                    isError = uiState.topicTitleError != null,
                    supportingText = uiState.topicTitleError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = uiState.whatWasTaught,
                    onValueChange = { viewModel.onWhatWasTaughtChanged(it) },
                    label = { Text(stringResource(R.string.label_what_was_taught)) },
                    placeholder = { Text(stringResource(R.string.placeholder_what_was_taught)) },
                    isError = uiState.whatWasTaughtError != null,
                    supportingText = uiState.whatWasTaughtError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = uiState.practiceGiven,
                    onValueChange = { viewModel.onPracticeGivenChanged(it) },
                    label = { Text(stringResource(R.string.label_practice_given)) },
                    placeholder = { Text(stringResource(R.string.placeholder_practice_given)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Section 3: Student Understanding & Homework
                FormSectionTitle(title = "Comprehension & Homework")

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.label_student_understanding),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            StudentUnderstanding.EXCELLENT to ("Excellent" to Color(0xFF2E7D32)),
                            StudentUnderstanding.GOOD to ("Good" to Color(0xFF0288D1)),
                            StudentUnderstanding.AVERAGE to ("Average" to Color(0xFFF57C00)),
                            StudentUnderstanding.NEEDS_ATTENTION to ("Attention" to MaterialTheme.colorScheme.error)
                        ).forEach { (u, pair) ->
                            val isSelected = uiState.studentUnderstanding == u
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onUnderstandingChanged(u) },
                                label = { Text(pair.first) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = pair.second.copy(alpha = 0.2f),
                                    selectedLabelColor = pair.second
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.homeworkAssigned,
                    onValueChange = { viewModel.onHomeworkAssignedChanged(it) },
                    label = { Text(stringResource(R.string.label_homework_assigned)) },
                    placeholder = { Text(stringResource(R.string.placeholder_homework_assigned)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.label_homework_status),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            HomeworkStatus.ASSIGNED to "Assigned",
                            HomeworkStatus.SUBMITTED_COMPLETE to "Submitted (Full)",
                            HomeworkStatus.SUBMITTED_PARTIAL to "Submitted (Partial)",
                            HomeworkStatus.NOT_SUBMITTED to "Not Submitted",
                            HomeworkStatus.NONE to "None"
                        ).forEach { (status, label) ->
                            item {
                                FilterChip(
                                    selected = uiState.homeworkStatus == status,
                                    onClick = { viewModel.onHomeworkStatusChanged(status) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }

                // Section 4: Remarks & Next Plan
                FormSectionTitle(title = "Remarks & Planning")

                OutlinedTextField(
                    value = uiState.teacherRemarks,
                    onValueChange = { viewModel.onTeacherRemarksChanged(it) },
                    label = { Text(stringResource(R.string.label_teacher_remarks)) },
                    placeholder = { Text(stringResource(R.string.placeholder_teacher_remarks)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = uiState.nextClassPlan,
                    onValueChange = { viewModel.onNextClassPlanChanged(it) },
                    label = { Text(stringResource(R.string.label_next_class_plan)) },
                    placeholder = { Text(stringResource(R.string.placeholder_next_class_plan)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showNewSubjectDialog) {
        AlertDialog(
            onDismissRequest = {
                showNewSubjectDialog = false
                newSubjectName = ""
            },
            title = { Text(text = "Add New Subject", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newSubjectName,
                    onValueChange = { newSubjectName = it },
                    label = { Text("Subject Name") },
                    placeholder = { Text("e.g. Higher Mathematics") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubjectName.isNotBlank()) {
                            viewModel.onAddNewSubject(newSubjectName)
                            showNewSubjectDialog = false
                            newSubjectName = ""
                        }
                    },
                    enabled = newSubjectName.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNewSubjectDialog = false
                    newSubjectName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
