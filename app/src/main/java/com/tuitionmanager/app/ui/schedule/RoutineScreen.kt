package com.tuitionmanager.app.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import com.tuitionmanager.app.R
import com.tuitionmanager.app.domain.model.RoutineDay
import com.tuitionmanager.app.domain.model.ScheduleWithDetails
import com.tuitionmanager.app.ui.components.ConfirmActionDialog
import com.tuitionmanager.app.ui.components.EmptyStateView
import com.tuitionmanager.app.ui.components.LoadingStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    viewModel: RoutineViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onAddScheduleClick: () -> Unit,
    onEditScheduleClick: (String) -> Unit,
    onStudentClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var scheduleToDelete by remember { mutableStateOf<ScheduleWithDetails?>(null) }

    val days = listOf(
        null to stringResource(R.string.tab_all_week),
        6 to stringResource(R.string.day_sat_short),
        7 to stringResource(R.string.day_sun_short),
        1 to stringResource(R.string.day_mon_short),
        2 to stringResource(R.string.day_tue_short),
        3 to stringResource(R.string.day_wed_short),
        4 to stringResource(R.string.day_thu_short),
        5 to stringResource(R.string.day_fri_short)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.title_weekly_routine),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (!uiState.isLoading) {
                            Text(
                                text = "${uiState.totalSlotsCount} recurring slots",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                    IconButton(onClick = onAddScheduleClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.action_add_schedule)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddScheduleClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_add_schedule)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Day Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = days.indexOfFirst { it.first == uiState.selectedDayOfWeek }.coerceAtLeast(0),
                edgePadding = 16.dp,
                divider = {}
            ) {
                days.forEach { (dayInt, label) ->
                    val selected = uiState.selectedDayOfWeek == dayInt
                    Tab(
                        selected = selected,
                        onClick = { viewModel.onDaySelected(dayInt) },
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                LoadingStateView()
            } else if (uiState.displayedSchedules.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Outlined.CalendarMonth,
                    title = stringResource(R.string.empty_routine_title),
                    description = stringResource(R.string.empty_routine_desc),
                    actionButtonText = stringResource(R.string.action_add_schedule),
                    onActionClick = onAddScheduleClick
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.displayedSchedules,
                        key = { it.schedule.id }
                    ) { scheduleWithDetails ->
                        RoutineSlotCard(
                            item = scheduleWithDetails,
                            showDayBadge = uiState.selectedDayOfWeek == null,
                            onSlotClick = { onEditScheduleClick(scheduleWithDetails.schedule.id) },
                            onStudentClick = { onStudentClick(scheduleWithDetails.schedule.studentId) },
                            onToggleActive = {
                                viewModel.toggleActive(
                                    scheduleWithDetails.schedule.id,
                                    scheduleWithDetails.schedule.isActive
                                )
                            },
                            onDeleteClick = { scheduleToDelete = scheduleWithDetails }
                        )
                    }
                }
            }
        }
    }

    scheduleToDelete?.let { item ->
        ConfirmActionDialog(
            title = stringResource(R.string.dialog_delete_schedule_title),
            message = stringResource(R.string.dialog_delete_schedule_message),
            confirmText = stringResource(R.string.action_delete),
            onConfirm = {
                viewModel.deleteSchedule(item.schedule.id)
                scheduleToDelete = null
            },
            onDismiss = { scheduleToDelete = null }
        )
    }
}

@Composable
fun RoutineSlotCard(
    item: ScheduleWithDetails,
    showDayBadge: Boolean,
    onSlotClick: () -> Unit,
    onStudentClick: () -> Unit,
    onToggleActive: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.schedule.isActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSlotClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Day badge + Time Range + Active switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showDayBadge) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = RoutineDay.fromDayOfWeek(item.dayOfWeek).shortName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${item.startTimeFormatted} - ${item.endTimeFormatted}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Switch(
                    checked = item.schedule.isActive,
                    onCheckedChange = { onToggleActive() },
                    modifier = Modifier.height(24.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Student & Subject Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.studentName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onSlotClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.action_edit),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Location & Notes if available
            if (!item.schedule.location.isNullOrBlank() || !item.schedule.notes.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item.schedule.location?.let { loc ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = loc,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item.schedule.notes?.let { note ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notes,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = note,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
