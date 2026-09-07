package com.tuitionmanager.app.ui.agenda

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tuitionmanager.app.R
import com.tuitionmanager.app.domain.model.ClassSessionWithDetails
import com.tuitionmanager.app.domain.model.SessionStatus
import com.tuitionmanager.app.ui.components.EmptyStateView
import com.tuitionmanager.app.ui.components.LoadingStateView
import com.tuitionmanager.app.ui.components.SessionStatusChip
import com.tuitionmanager.app.utils.TimeUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyAgendaScreen(
    viewModel: DailyAgendaViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onSessionClick: (String) -> Unit,
    onStudentClick: (String) -> Unit,
    onCreateDiary: (studentId: String, classSessionId: String) -> Unit,
    onViewDiary: (diaryId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var sessionToComplete by remember { mutableStateOf<ClassSessionWithDetails?>(null) }
    var sessionToCancel by remember { mutableStateOf<ClassSessionWithDetails?>(null) }
    var sessionToMarkMissed by remember { mutableStateOf<ClassSessionWithDetails?>(null) }
    var sessionToReschedule by remember { mutableStateOf<ClassSessionWithDetails?>(null) }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.title_daily_agenda),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.isToday) "Today\'s Teaching Schedule" else "Schedule Overview",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.generateSessionsFromRoutine() },
                        enabled = !uiState.isGenerating
                    ) {
                        if (uiState.isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = stringResource(R.string.action_generate_sessions)
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
        ) {
            // Date Navigation Header
            DateNavigationControl(
                selectedDateEpochMs = uiState.selectedDateEpochMs,
                isToday = uiState.isToday,
                onPreviousDay = { viewModel.onPreviousDay() },
                onNextDay = { viewModel.onNextDay() },
                onToday = { viewModel.onToday() },
                onPickDate = {
                    val cal = Calendar.getInstance().apply { timeInMillis = uiState.selectedDateEpochMs }
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val newCal = Calendar.getInstance().apply {
                                set(y, m, d, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            viewModel.onDateSelected(newCal.timeInMillis)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            )

            // Day Progress Stats Bar
            if (uiState.sessions.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.sessions.size} classes total",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Done: ${uiState.completedCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Upcoming: ${uiState.scheduledCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.missedCount > 0) {
                                Text(
                                    text = "Missed: ${uiState.missedCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                LoadingStateView()
            } else if (uiState.sessions.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.EventBusy,
                    title = stringResource(R.string.empty_agenda_title),
                    description = stringResource(R.string.empty_agenda_desc),
                    actionButtonText = stringResource(R.string.action_generate_sessions),
                    onActionClick = { viewModel.generateSessionsFromRoutine() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.sessions,
                        key = { it.session.id }
                    ) { sessionDetails ->
                        AgendaSessionCard(
                            item = sessionDetails,
                            onClick = { onSessionClick(sessionDetails.session.id) },
                            onStudentClick = { onStudentClick(sessionDetails.session.studentId) },
                            onCallClick = { phone ->
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            },
                            onCompleteClick = { sessionToComplete = sessionDetails },
                            onCancelClick = { sessionToCancel = sessionDetails },
                            onMissedClick = { sessionToMarkMissed = sessionDetails },
                            onRescheduleClick = { sessionToReschedule = sessionDetails },
                            onCreateDiaryClick = {
                                onCreateDiary(sessionDetails.session.studentId, sessionDetails.session.id)
                            },
                            onViewDiaryClick = { diaryId ->
                                onViewDiary(diaryId)
                            }
                        )
                    }
                }
            }
        }
    }

    // Complete Session Dialog
    sessionToComplete?.let { item ->
        CompleteSessionDialog(
            sessionDetails = item,
            onConfirm = { actualStart, actualEnd, topicCovered, remarks, createDiary ->
                viewModel.markSessionCompleted(
                    sessionId = item.session.id,
                    actualStart = actualStart,
                    actualEnd = actualEnd,
                    topicCovered = topicCovered,
                    remarks = remarks,
                    onDiaryRequested = if (createDiary) { sId, cId ->
                        onCreateDiary(sId, cId)
                    } else null
                )
                sessionToComplete = null
            },
            onDismiss = { sessionToComplete = null }
        )
    }

    // Cancel Session Dialog
    sessionToCancel?.let { item ->
        CancelSessionDialog(
            sessionDetails = item,
            onConfirm = { reason ->
                viewModel.markSessionCancelled(item.session.id, reason)
                sessionToCancel = null
            },
            onDismiss = { sessionToCancel = null }
        )
    }

    // Missed Session Dialog
    sessionToMarkMissed?.let { item ->
        MissedSessionDialog(
            sessionDetails = item,
            onConfirm = { remarks ->
                viewModel.markSessionMissed(item.session.id, remarks)
                sessionToMarkMissed = null
            },
            onDismiss = { sessionToMarkMissed = null }
        )
    }

    // Reschedule Session Dialog
    sessionToReschedule?.let { item ->
        RescheduleSessionDialog(
            sessionDetails = item,
            onConfirm = { targetDateEpochMs, newStart, newEnd, reason ->
                viewModel.rescheduleSession(
                    sessionId = item.session.id,
                    targetDateEpochMs = targetDateEpochMs,
                    newStartMinutes = newStart,
                    newEndTimeMinutes = newEnd,
                    reason = reason
                )
                sessionToReschedule = null
            },
            onDismiss = { sessionToReschedule = null }
        )
    }
}

@Composable
fun DateNavigationControl(
    selectedDateEpochMs: Long,
    isToday: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    onPickDate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.action_previous_day)
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onPickDate)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isToday) "Today, ${TimeUtils.formatLocalDate(selectedDateEpochMs, "dd MMM")}" else TimeUtils.formatLocalDate(selectedDateEpochMs, "EEE, dd MMM yyyy"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isToday) {
                    TextButton(
                        onClick = onToday,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.action_today),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(onClick = onNextDay) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.action_next_day)
                    )
                }
            }
        }
    }
}

@Composable
fun AgendaSessionCard(
    item: ClassSessionWithDetails,
    onClick: () -> Unit,
    onStudentClick: () -> Unit,
    onCallClick: (String) -> Unit,
    onCompleteClick: () -> Unit,
    onCancelClick: () -> Unit,
    onMissedClick: () -> Unit,
    onRescheduleClick: () -> Unit,
    onCreateDiaryClick: () -> Unit,
    onViewDiaryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Time + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.timeFormatted,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                SessionStatusChip(status = item.session.status)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Student & Subject Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onStudentClick)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.subjectName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item.studentPhone?.let { phone ->
                    IconButton(
                        onClick = { onCallClick(phone) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call Student",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Topic covered if present
            if (!item.session.topicCovered.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Topic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = item.session.topicCovered,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Diary badge / indicator
            if (item.hasDiaryEntry) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.badge_has_diary),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Remarks / Reschedule note
            if (!item.session.remarks.isNullOrBlank()) {
                Text(
                    text = item.session.remarks,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Status Action Row
            when (item.session.status) {
                SessionStatus.SCHEDULED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onCompleteClick,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(stringResource(R.string.action_mark_completed), style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = onRescheduleClick,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(stringResource(R.string.action_reschedule), style = MaterialTheme.typography.labelMedium)
                        }

                        IconButton(
                            onClick = onCancelClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Cancel,
                                contentDescription = stringResource(R.string.action_mark_cancelled),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                SessionStatus.COMPLETED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.hasDiaryEntry && item.diaryEntryId != null) {
                            OutlinedButton(
                                onClick = { onViewDiaryClick(item.diaryEntryId) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.action_view_diary_entry), style = MaterialTheme.typography.labelMedium)
                            }
                        } else {
                            Button(
                                onClick = onCreateDiaryClick,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.action_create_diary_entry), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }

                SessionStatus.MISSED, SessionStatus.CANCELLED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onRescheduleClick,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(stringResource(R.string.action_reschedule), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                SessionStatus.RESCHEDULED -> {
                    // Already moved
                }
            }
        }
    }
}
