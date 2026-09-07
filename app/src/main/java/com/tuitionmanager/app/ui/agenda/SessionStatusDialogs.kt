package com.tuitionmanager.app.ui.agenda

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.tuitionmanager.app.R
import com.tuitionmanager.app.domain.model.ClassSessionWithDetails
import com.tuitionmanager.app.ui.components.AppTimePickerDialog
import com.tuitionmanager.app.ui.components.TimeSelectionField
import com.tuitionmanager.app.utils.TimeUtils
import java.util.Calendar

@Composable
fun CompleteSessionDialog(
    sessionDetails: ClassSessionWithDetails,
    onConfirm: (actualStart: Int, actualEnd: Int, topicCovered: String, remarks: String, createDiary: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var actualStart by remember { mutableIntStateOf(sessionDetails.session.actualStartTime ?: sessionDetails.session.scheduledStartTime) }
    var actualEnd by remember { mutableIntStateOf(sessionDetails.session.actualEndTime ?: sessionDetails.session.scheduledEndTime) }
    var topicCovered by remember { mutableStateOf(sessionDetails.session.topicCovered ?: "") }
    var remarks by remember { mutableStateOf(sessionDetails.session.remarks ?: "") }
    var createDiary by remember { mutableStateOf(!sessionDetails.hasDiaryEntry) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.dialog_complete_session_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${sessionDetails.studentName} · ${sessionDetails.subjectName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = stringResource(R.string.label_actual_time),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimeSelectionField(
                        label = stringResource(R.string.label_start_time),
                        minutesFromMidnight = actualStart,
                        onClick = { showStartPicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    TimeSelectionField(
                        label = stringResource(R.string.label_end_time),
                        minutesFromMidnight = actualEnd,
                        onClick = { showEndPicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = topicCovered,
                    onValueChange = { topicCovered = it },
                    label = { Text(stringResource(R.string.label_topic_summary)) },
                    placeholder = { Text(stringResource(R.string.placeholder_topic_summary)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text(stringResource(R.string.label_session_remarks)) },
                    placeholder = { Text(stringResource(R.string.placeholder_session_remarks)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                if (!sessionDetails.hasDiaryEntry) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = createDiary,
                            onCheckedChange = { createDiary = it }
                        )
                        Text(
                            text = stringResource(R.string.action_create_diary_entry),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(actualStart, actualEnd, topicCovered, remarks, createDiary)
                        }
                    ) {
                        Text(stringResource(R.string.action_mark_completed))
                    }
                }
            }
        }
    }

    if (showStartPicker) {
        AppTimePickerDialog(
            initialMinutes = actualStart,
            title = stringResource(R.string.label_start_time),
            onTimeSelected = { actualStart = it },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        AppTimePickerDialog(
            initialMinutes = actualEnd,
            title = stringResource(R.string.label_end_time),
            onTimeSelected = { actualEnd = it },
            onDismiss = { showEndPicker = false }
        )
    }
}

@Composable
fun CancelSessionDialog(
    sessionDetails: ClassSessionWithDetails,
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_cancel_session_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${sessionDetails.studentName} (${sessionDetails.subjectName}) · ${sessionDetails.timeFormatted}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.dialog_cancel_session_message),
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(stringResource(R.string.label_cancellation_reason)) },
                    placeholder = { Text(stringResource(R.string.placeholder_cancellation_reason)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.action_mark_cancelled))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun MissedSessionDialog(
    sessionDetails: ClassSessionWithDetails,
    onConfirm: (remarks: String) -> Unit,
    onDismiss: () -> Unit
) {
    var remarks by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_missed_session_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${sessionDetails.studentName} (${sessionDetails.subjectName}) · ${sessionDetails.timeFormatted}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.dialog_missed_session_message),
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text(stringResource(R.string.label_missed_reason)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(remarks) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.action_mark_missed))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun RescheduleSessionDialog(
    sessionDetails: ClassSessionWithDetails,
    onConfirm: (targetDateEpochMs: Long, newStartMinutes: Int, newEndMinutes: Int, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var targetDateEpochMs by remember {
        // Default tomorrow
        mutableLongStateOf(sessionDetails.session.sessionDate + 86400000L)
    }
    var newStartMinutes by remember { mutableIntStateOf(sessionDetails.session.scheduledStartTime) }
    var newEndMinutes by remember { mutableIntStateOf(sessionDetails.session.scheduledEndTime) }
    var reason by remember { mutableStateOf("") }

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.dialog_reschedule_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${sessionDetails.studentName} · ${sessionDetails.subjectName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                // Date Selection
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = targetDateEpochMs }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(y, m, d, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    targetDateEpochMs = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.label_new_date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = TimeUtils.formatLocalDate(targetDateEpochMs, "EEEE, dd MMM yyyy"),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Time Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TimeSelectionField(
                        label = stringResource(R.string.label_new_start_time),
                        minutesFromMidnight = newStartMinutes,
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    TimeSelectionField(
                        label = stringResource(R.string.label_new_end_time),
                        minutesFromMidnight = newEndMinutes,
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(stringResource(R.string.label_reschedule_reason)) },
                    placeholder = { Text(stringResource(R.string.placeholder_reschedule_reason)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(targetDateEpochMs, newStartMinutes, newEndMinutes, reason)
                        },
                        enabled = newStartMinutes < newEndMinutes
                    ) {
                        Text(stringResource(R.string.action_confirm_reschedule))
                    }
                }
            }
        }
    }

    if (showStartTimePicker) {
        AppTimePickerDialog(
            initialMinutes = newStartMinutes,
            title = stringResource(R.string.label_new_start_time),
            onTimeSelected = { newStartMinutes = it },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        AppTimePickerDialog(
            initialMinutes = newEndMinutes,
            title = stringResource(R.string.label_new_end_time),
            onTimeSelected = { newEndMinutes = it },
            onDismiss = { showEndTimePicker = false }
        )
    }
}
