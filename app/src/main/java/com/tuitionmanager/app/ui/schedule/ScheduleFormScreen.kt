package com.tuitionmanager.app.ui.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tuitionmanager.app.R
import com.tuitionmanager.app.domain.model.RoutineDay
import com.tuitionmanager.app.ui.components.AppTimePickerDialog
import com.tuitionmanager.app.ui.components.LoadingStateView
import com.tuitionmanager.app.ui.components.ScheduleConflictBanner
import com.tuitionmanager.app.ui.components.TimeSelectionField
import com.tuitionmanager.app.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleFormScreen(
    viewModel: ScheduleFormViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var studentMenuExpanded by remember { mutableStateOf(false) }
    var subjectMenuExpanded by remember { mutableStateOf(false) }

    val days = listOf(
        RoutineDay.SATURDAY,
        RoutineDay.SUNDAY,
        RoutineDay.MONDAY,
        RoutineDay.TUESDAY,
        RoutineDay.WEDNESDAY,
        RoutineDay.THURSDAY,
        RoutineDay.FRIDAY
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (uiState.isEditMode) R.string.title_edit_schedule else R.string.title_add_schedule
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveSchedule(onSuccess = onNavigateBack) },
                        enabled = uiState.isFormValid && !uiState.isSaving,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.action_save))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
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
                // Conflict Banner
                if (uiState.hasConflict && uiState.conflictingSchedules.isNotEmpty()) {
                    ScheduleConflictBanner(conflicts = uiState.conflictingSchedules)
                }

                // Error Message if any
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
                                onClick = { viewModel.dismissError() },
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

                // Student Selection
                Card(
                    shape = RoundedCornerShape(16.dp),
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
                            expanded = studentMenuExpanded,
                            onExpandedChange = { studentMenuExpanded = it }
                        ) {
                            val selectedStudent = uiState.availableStudents.firstOrNull { it.id == uiState.studentId }
                            OutlinedTextField(
                                value = selectedStudent?.name ?: "Select Student",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.label_select_student)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = studentMenuExpanded,
                                onDismissRequest = { studentMenuExpanded = false }
                            ) {
                                uiState.availableStudents.forEach { student ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(student.name, fontWeight = FontWeight.Medium)
                                                student.phone?.let {
                                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        },
                                        onClick = {
                                            viewModel.onStudentSelected(student.id)
                                            studentMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Subject Dropdown
                        ExposedDropdownMenuBox(
                            expanded = subjectMenuExpanded,
                            onExpandedChange = { subjectMenuExpanded = it }
                        ) {
                            val selectedSubject = uiState.availableSubjects.firstOrNull { it.id == uiState.subjectId }
                            OutlinedTextField(
                                value = selectedSubject?.subjectName ?: if (uiState.availableSubjects.isEmpty()) "No subjects configured" else "Select Subject",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.label_select_subject)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = subjectMenuExpanded,
                                onDismissRequest = { subjectMenuExpanded = false }
                            ) {
                                uiState.availableSubjects.forEach { subject ->
                                    DropdownMenuItem(
                                        text = { Text(subject.subjectName) },
                                        onClick = {
                                            viewModel.onSubjectSelected(subject.id)
                                            subjectMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Day of Week & Timing Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Schedule Timing",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Day of Week Chips
                        Text(
                            text = stringResource(R.string.label_day_of_week),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            days.forEach { routineDay ->
                                val isSelected = uiState.dayOfWeek == routineDay.dayOfWeek
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onDayOfWeekSelected(routineDay.dayOfWeek) },
                                    label = {
                                        Text(
                                            text = routineDay.shortName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Time Selection Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TimeSelectionField(
                                label = stringResource(R.string.label_start_time),
                                minutesFromMidnight = uiState.startTimeMinutes,
                                onClick = { showStartTimePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                            TimeSelectionField(
                                label = stringResource(R.string.label_end_time),
                                minutesFromMidnight = uiState.endTimeMinutes,
                                onClick = { showEndTimePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Duration Display
                        val hours = uiState.durationMinutes / 60
                        val mins = uiState.durationMinutes % 60
                        val durationText = buildString {
                            if (hours > 0) append("$hours hr ")
                            if (mins > 0 || hours == 0) append("$mins min")
                        }
                        Text(
                            text = "Duration: $durationText (${TimeUtils.formatMinutesToTime(uiState.startTimeMinutes)} to ${TimeUtils.formatMinutesToTime(uiState.endTimeMinutes)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Location and Notes Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Location & Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = uiState.location,
                            onValueChange = { viewModel.onLocationChanged(it) },
                            label = { Text(stringResource(R.string.label_location)) },
                            placeholder = { Text(stringResource(R.string.placeholder_location)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = { viewModel.onNotesChanged(it) },
                            label = { Text(stringResource(R.string.label_schedule_notes)) },
                            placeholder = { Text(stringResource(R.string.placeholder_schedule_notes)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.label_is_active_schedule),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Switch(
                                checked = uiState.isActive,
                                onCheckedChange = { viewModel.onActiveToggled(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStartTimePicker) {
        AppTimePickerDialog(
            initialMinutes = uiState.startTimeMinutes,
            title = stringResource(R.string.label_start_time),
            onTimeSelected = { viewModel.onStartTimeSelected(it) },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        AppTimePickerDialog(
            initialMinutes = uiState.endTimeMinutes,
            title = stringResource(R.string.label_end_time),
            onTimeSelected = { viewModel.onEndTimeSelected(it) },
            onDismiss = { showEndTimePicker = false }
        )
    }
}
