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
import com.tuitionmanager.app.domain.model.HomeworkStatus
import com.tuitionmanager.app.domain.model.Student
import com.tuitionmanager.app.domain.model.StudentDiary
import com.tuitionmanager.app.domain.model.StudentUnderstanding
import com.tuitionmanager.app.domain.model.ScheduleWithDetails
import com.tuitionmanager.app.domain.model.ClassSessionWithDetails
import com.tuitionmanager.app.domain.model.ExamWithDetails
import com.tuitionmanager.app.ui.components.*
import com.tuitionmanager.app.ui.exam.ExamCard
import com.tuitionmanager.app.ui.schedule.SessionStatusChip
import com.tuitionmanager.app.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    viewModel: StudentProfileViewModel,
    onNavigateBack: () -> Unit,
    onEditStudent: (String) -> Unit,
    onAddDiary: (String) -> Unit,
    onDiaryClick: (String) -> Unit,
    onViewAllDiaries: (String) -> Unit,
    onAddSchedule: (String) -> Unit = {},
    onEditSchedule: (String) -> Unit = {},
    onSessionClick: (String) -> Unit = {},
    onViewAllSessions: (String) -> Unit = {},
    onAddExam: (String) -> Unit = {},
    onExamClick: (String) -> Unit = {},
    onEnterResultClick: (String) -> Unit = {},
    onViewAllExams: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.student?.name ?: stringResource(R.string.title_student_profile),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    uiState.student?.let { student ->
                        IconButton(onClick = { onEditStudent(student.id) }) {
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
            LoadingStateView(message = "Loading student profile…")
        } else if (uiState.student == null) {
            ErrorStateView(message = uiState.error ?: "Student not found", onRetry = onNavigateBack)
        } else {
            val student = uiState.student!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Profile Header Card
                ProfileHeader(
                    student = student,
                    onAddDiary = { onAddDiary(student.id) },
                    onToggleActive = { viewModel.toggleActive() }
                )

                // Scrollable Tabs Row
                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedTab.ordinal,
                    edgePadding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileTab.values().forEach { tab ->
                        Tab(
                            selected = uiState.selectedTab == tab,
                            onClick = { viewModel.onTabSelected(tab) },
                            text = {
                                Text(
                                    text = when (tab) {
                                        ProfileTab.OVERVIEW -> stringResource(R.string.tab_overview)
                                        ProfileTab.SCHEDULE -> stringResource(R.string.tab_schedule)
                                        ProfileTab.CLASSES -> stringResource(R.string.tab_classes)
                                        ProfileTab.DIARY -> stringResource(R.string.tab_diary)
                                        ProfileTab.EXAMS -> stringResource(R.string.tab_exams)
                                        ProfileTab.PAYMENTS -> stringResource(R.string.tab_payments)
                                        ProfileTab.PROGRESS -> stringResource(R.string.tab_progress)
                                        ProfileTab.NOTES -> stringResource(R.string.tab_notes)
                                    }
                                )
                            }
                        )
                    }
                }

                // Tab Content
                when (uiState.selectedTab) {
                    ProfileTab.OVERVIEW -> {
                        OverviewTabContent(
                            uiState = uiState,
                            onAddDiary = { onAddDiary(student.id) },
                            onDiaryClick = onDiaryClick,
                            onViewAllDiaries = { onViewAllDiaries(student.id) }
                        )
                    }
                    ProfileTab.SCHEDULE -> {
                        StudentScheduleTabContent(
                            studentId = student.id,
                            schedules = uiState.schedules,
                            onAddSchedule = { onAddSchedule(student.id) },
                            onEditSchedule = onEditSchedule
                        )
                    }
                    ProfileTab.CLASSES -> {
                        StudentClassesTabContent(
                            studentId = student.id,
                            sessions = uiState.sessions,
                            onSessionClick = onSessionClick,
                            onViewAllSessions = { onViewAllSessions(student.id) }
                        )
                    }
                    ProfileTab.DIARY -> {
                        DiaryTabContent(
                            studentId = student.id,
                            diaries = uiState.recentDiaries,
                            onAddDiary = { onAddDiary(student.id) },
                            onDiaryClick = onDiaryClick
                        )
                    }
                    ProfileTab.EXAMS -> {
                        StudentExamsTabContent(
                            studentId = student.id,
                            exams = uiState.exams,
                            onAddExam = { onAddExam(student.id) },
                            onExamClick = onExamClick,
                            onEnterResultClick = onEnterResultClick
                        )
                    }
                    else -> {
                        PlaceholderTabContent(tab = uiState.selectedTab)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_delete_student_title),
            message = stringResource(R.string.dialog_delete_student_message),
            confirmText = stringResource(R.string.action_delete),
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteStudent(onComplete = onNavigateBack)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun ProfileHeader(
    student: Student,
    onAddDiary: () -> Unit,
    onToggleActive: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            if (student.isActive) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (student.isActive) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (student.isActive) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            modifier = Modifier.clickable { onToggleActive() }
                        ) {
                            Text(
                                text = if (student.isActive) "Active" else "Inactive",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (student.isActive) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val subtitle = listOfNotNull(
                        student.classGrade?.takeIf { it.isNotBlank() },
                        student.institution?.takeIf { it.isNotBlank() }
                    ).joinToString(" • ")

                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddDiary,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.quick_add_diary),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                if (!student.phone.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = { /* In Android runtime, calls Intent(ACTION_DIAL) */ },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Call", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTabContent(
    uiState: StudentProfileUiState,
    onAddDiary: () -> Unit,
    onDiaryClick: (String) -> Unit,
    onViewAllDiaries: () -> Unit
) {
    val stats = uiState.stats
    val student = uiState.student!!

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-Density Metrics Grid (2 columns)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = stringResource(R.string.stat_total_classes),
                        value = "${stats.totalClasses}",
                        icon = Icons.Outlined.School,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = stringResource(R.string.stat_completed),
                        value = "${stats.completedClasses}",
                        icon = Icons.Outlined.CheckCircle,
                        accentColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = stringResource(R.string.stat_missed),
                        value = "${stats.missedClasses}",
                        icon = Icons.Outlined.Cancel,
                        accentColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = stringResource(R.string.stat_diary_entries),
                        value = "${stats.diaryEntriesCount}",
                        icon = Icons.Outlined.HistoryEdu,
                        accentColor = Color(0xFF0288D1),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = stringResource(R.string.stat_exams_taken),
                        value = "${stats.examsCompletedCount}",
                        icon = Icons.Outlined.AssignmentTurnedIn,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = stringResource(R.string.stat_avg_score),
                        value = if (stats.averageExamPercentage != null) {
                            "${String.format("%.1f", stats.averageExamPercentage)}%"
                        } else "—",
                        icon = Icons.Outlined.Analytics,
                        accentColor = Color(0xFFE65100),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Contact & Tuition Information Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Tuition & Contact Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (student.monthlyFeeAmount > 0) {
                        DetailRow(label = "Monthly Fee", value = "৳ ${student.monthlyFeeAmount.toInt()} (Billing on day ${student.billingCycleDay})")
                    }
                    if (!student.phone.isNullOrBlank()) {
                        DetailRow(label = "Student Phone", value = student.phone)
                    }
                    if (!student.guardianName.isNullOrBlank()) {
                        DetailRow(label = "Guardian", value = "${student.guardianName} (${student.guardianPhone ?: "No phone"})")
                    }
                    if (!student.address.isNullOrBlank()) {
                        DetailRow(label = "Address", value = student.address)
                    }
                    DetailRow(
                        label = "Guardian Channel",
                        value = "${student.guardianPreferredChannel.name} · Sharing ${if (student.isGuardianProgressSharingEnabled) "Enabled" else "Disabled"}"
                    )
                }
            }
        }

        // Subjects Card
        if (uiState.subjects.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Enrolled Subjects",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            uiState.subjects.forEach { subject ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = subject.subjectName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Diary Entries Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Lesson Diaries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (uiState.recentDiaries.isNotEmpty()) {
                    TextButton(onClick = onViewAllDiaries) {
                        Text(text = stringResource(R.string.action_view_all))
                    }
                }
            }
        }

        // Recent Diary Items
        if (uiState.recentDiaries.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Outlined.EditNote,
                    title = "No diary entries yet",
                    description = "Keep parents and students updated by recording what was taught in class.",
                    actionButtonText = "+ Add First Diary Entry",
                    onActionClick = onAddDiary
                )
            }
        } else {
            items(uiState.recentDiaries, key = { it.id }) { diary ->
                DiaryItemCard(
                    diary = diary,
                    onClick = { onDiaryClick(diary.id) }
                )
            }
        }
    }
}

@Composable
private fun DiaryTabContent(
    studentId: String,
    diaries: List<StudentDiary>,
    onAddDiary: () -> Unit,
    onDiaryClick: (String) -> Unit
) {
    if (diaries.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.EditNote,
            title = stringResource(R.string.empty_diary_title),
            description = stringResource(R.string.empty_diary_desc),
            actionButtonText = stringResource(R.string.title_new_diary_entry),
            onActionClick = onAddDiary
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Diary Entries (${diaries.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onAddDiary,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Entry", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            items(diaries, key = { it.id }) { diary ->
                DiaryItemCard(
                    diary = diary,
                    onClick = { onDiaryClick(diary.id) }
                )
            }
        }
    }
}

@Composable
fun DiaryItemCard(
    diary: StudentDiary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TimeUtils.formatLocalDate(diary.date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                // Understanding badge
                val (uColor, uText) = when (diary.studentUnderstanding) {
                    StudentUnderstanding.EXCELLENT -> Color(0xFF2E7D32) to "Excellent"
                    StudentUnderstanding.GOOD -> Color(0xFF0288D1) to "Good"
                    StudentUnderstanding.AVERAGE -> Color(0xFFF57C00) to "Average"
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
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = diary.topicTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = diary.whatWasTaught,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!diary.homeworkAssigned.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "HW: ${diary.homeworkAssigned}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PlaceholderTabContent(tab: ProfileTab) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = when (tab) {
                    ProfileTab.SCHEDULE -> Icons.Outlined.CalendarToday
                    ProfileTab.CLASSES -> Icons.Outlined.EventNote
                    ProfileTab.EXAMS -> Icons.Outlined.Quiz
                    ProfileTab.PAYMENTS -> Icons.Outlined.Payments
                    ProfileTab.PROGRESS -> Icons.Outlined.TrendingUp
                    ProfileTab.NOTES -> Icons.Outlined.StickyNote2
                    else -> Icons.Outlined.Info
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Text(
                text = "${tab.name.lowercase().replaceFirstChar { it.uppercase() }} Section Ready",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Database models and queries are active. Full interactive UI for this module will be connected in subsequent steps.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun StudentScheduleTabContent(
    studentId: String,
    schedules: List<ScheduleWithDetails>,
    onAddSchedule: () -> Unit,
    onEditSchedule: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.schedule_slots_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FilledTonalButton(onClick = onAddSchedule) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.add_schedule_slot))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (schedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.student_schedule_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(schedules, key = { it.schedule.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditSchedule(item.schedule.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = item.dayName,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.subjectName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.timeFormatted,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (item.schedule.isActive) {
                                    AssistChip(
                                        onClick = { onEditSchedule(item.schedule.id) },
                                        label = { Text("Active", style = MaterialTheme.typography.labelSmall) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            labelColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                } else {
                                    AssistChip(
                                        onClick = { onEditSchedule(item.schedule.id) },
                                        label = { Text("Inactive", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentClassesTabContent(
    studentId: String,
    sessions: List<ClassSessionWithDetails>,
    onSessionClick: (String) -> Unit,
    onViewAllSessions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val completedCount = sessions.count { it.session.status == com.tuitionmanager.app.domain.model.SessionStatus.COMPLETED }
            val missedCount = sessions.count { it.session.status == com.tuitionmanager.app.domain.model.SessionStatus.MISSED }

            Text(
                text = "Sessions (${sessions.size}) • $completedCount Done • $missedCount Missed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(onClick = onViewAllSessions) {
                Text(stringResource(R.string.class_history_title))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.EventNote,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.student_classes_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions.take(30), key = { it.session.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSessionClick(item.session.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.subjectName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                SessionStatusChip(status = item.session.status)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.dateFormatted} (${item.dayFormatted})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = item.timeFormatted,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (!item.session.topicCovered.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.session.topicCovered ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentExamsTabContent(
    studentId: String,
    exams: List<ExamWithDetails>,
    onAddExam: () -> Unit,
    onExamClick: (String) -> Unit,
    onEnterResultClick: (String) -> Unit
) {
    if (exams.isEmpty()) {
        EmptyStateView(
            icon = Icons.Outlined.Assignment,
            title = stringResource(R.string.student_exams_empty),
            description = stringResource(R.string.empty_exams_desc),
            actionButtonText = stringResource(R.string.action_add_exam),
            onActionClick = onAddExam
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exams & Assessments (${exams.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onAddExam,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.action_add_exam), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            items(exams, key = { it.exam.id }) { examDetails ->
                ExamCard(
                    examDetails = examDetails,
                    onClick = { onExamClick(examDetails.exam.id) },
                    onEnterResultClick = if (examDetails.result == null) {
                        { onEnterResultClick(examDetails.exam.id) }
                    } else null
                )
            }
        }
    }
}

