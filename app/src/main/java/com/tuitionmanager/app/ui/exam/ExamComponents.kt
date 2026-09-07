package com.tuitionmanager.app.ui.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tuitionmanager.app.R
import com.tuitionmanager.app.domain.model.*

@Composable
fun ExamTypeChip(
    type: ExamType,
    modifier: Modifier = Modifier
) {
    val textRes = when (type) {
        ExamType.WEEKLY_QUIZ -> R.string.exam_type_weekly_quiz
        ExamType.CHAPTER_TEST -> R.string.exam_type_chapter_test
        ExamType.MONTHLY_ASSESSMENT -> R.string.exam_type_monthly_assessment
        ExamType.MID_TERM -> R.string.exam_type_mid_term
        ExamType.FINAL_MODEL_TEST -> R.string.exam_type_final_model_test
        ExamType.SURPRISE_TEST -> R.string.exam_type_surprise_test
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier
    ) {
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ExamStatusChip(
    status: ExamStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, textRes) = when (status) {
        ExamStatus.PLANNED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            R.string.exam_status_planned
        )
        ExamStatus.COMPLETED -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            R.string.exam_status_completed
        )
        ExamStatus.MISSED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            R.string.exam_status_missed
        )
        ExamStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            R.string.exam_status_cancelled
        )
        ExamStatus.RESCHEDULED -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            R.string.exam_status_rescheduled
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PassFailBadge(
    isPassed: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isPassed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val textColor = if (isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
    val textRes = if (isPassed) R.string.badge_passed else R.string.badge_failed

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(textRes),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun ExamCard(
    examDetails: ExamWithDetails,
    onClick: () -> Unit,
    onEnterResultClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val exam = examDetails.exam
    val result = examDetails.result

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Student + Subject & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = examDetails.studentName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = examDetails.subjectName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                ExamStatusChip(status = exam.status)
            }

            // Title & Syllabus
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = exam.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (exam.syllabusTopic.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.topic_label, exam.syllabusTopic),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Metadata Row: Type, Date, Time, Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExamTypeChip(type = exam.examType)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = examDetails.dateFormatted,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${examDetails.timeFormatted} (${exam.durationMinutes}m)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Result Section or Action
            if (result != null && examDetails.percentage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val marksText = if (result.marksObtained % 1.0 == 0.0) result.marksObtained.toInt().toString() else result.marksObtained.toString()
                            val totalText = if (exam.totalMarks % 1.0 == 0.0) exam.totalMarks.toInt().toString() else exam.totalMarks.toString()
                            val pctText = String.format("%.1f", examDetails.percentage)

                            Text(
                                text = "$marksText / $totalText ($pctText%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (examDetails.gradeLevel != null) {
                                Text(
                                    text = examDetails.gradeLevel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        PassFailBadge(isPassed = result.isPassed)
                    }
                }
            } else if (onEnterResultClick != null && exam.status != ExamStatus.CANCELLED) {
                OutlinedButton(
                    onClick = onEnterResultClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.AssignmentTurnedIn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_record_result))
                }
            }
        }
    }
}
