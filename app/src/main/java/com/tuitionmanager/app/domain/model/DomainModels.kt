package com.tuitionmanager.app.domain.model

data class Student(
    val id: String,
    val name: String,
    val institution: String?,
    val classGrade: String?,
    val phone: String?,
    val guardianName: String?,
    val guardianPhone: String?,
    val address: String?,
    val monthlyFeeAmount: Double,
    val billingCycleDay: Int,
    val isActive: Boolean,
    val joinedDate: Long,
    val guardianPreferredChannel: GuardianPreferredChannel,
    val isGuardianProgressSharingEnabled: Boolean,
    val guardianReportLanguage: GuardianReportLanguage,
    val guardianReportFormat: GuardianReportFormat,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

data class StudentSubject(
    val id: String,
    val studentId: String,
    val subjectName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

data class StudentStats(
    val totalClasses: Int = 0,
    val completedClasses: Int = 0,
    val missedClasses: Int = 0,
    val diaryEntriesCount: Int = 0,
    val examsCompletedCount: Int = 0,
    val averageExamPercentage: Double? = null
)

data class Schedule(
    val id: String,
    val studentId: String,
    val subjectId: String,
    val dayOfWeek: Int, // 1 (Monday) .. 7 (Sunday)
    val startTimeMinutes: Int, // 0..1439
    val endTimeMinutes: Int, // 0..1439
    val effectiveStartDate: Long,
    val effectiveEndDate: Long?,
    val isActive: Boolean,
    val location: String? = null,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

data class RescheduleRecord(
    val id: String,
    val originalSessionId: String,
    val newSessionId: String,
    val rescheduledBy: String = "TUTOR",
    val reason: String? = null,
    val requestedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

data class ScheduleWithDetails(
    val schedule: Schedule,
    val studentName: String,
    val studentPhone: String?,
    val subjectName: String,
    val dayOfWeek: Int,
    val startTimeFormatted: String,
    val endTimeFormatted: String
)

data class ClassSessionWithDetails(
    val session: ClassSession,
    val studentName: String,
    val studentPhone: String?,
    val subjectName: String,
    val timeFormatted: String,
    val dayFormatted: String,
    val dateFormatted: String,
    val hasDiaryEntry: Boolean = false,
    val diaryEntryId: String? = null,
    val rescheduleReason: String? = null
)

data class ScheduleConflictDetails(
    val schedule: Schedule,
    val studentName: String,
    val subjectName: String,
    val dayName: String,
    val timeFormatted: String
)

data class ClassSession(
    val id: String,
    val studentId: String,
    val scheduleId: String?,
    val subjectId: String,
    val sessionDate: Long, // 00:00 UTC epoch ms
    val scheduledStartTime: Int,
    val scheduledEndTime: Int,
    val actualStartTime: Int?,
    val actualEndTime: Int?,
    val status: SessionStatus,
    val remarks: String?,
    val topicCovered: String?, // High-speed summary
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

data class StudentDiary(
    val id: String,
    val studentId: String,
    val subjectId: String,
    val classSessionId: String?,
    val date: Long,
    val topicTitle: String,
    val whatWasTaught: String,
    val homeworkAssigned: String?,
    val homeworkStatus: HomeworkStatus,
    val practiceGiven: String?,
    val studentUnderstanding: StudentUnderstanding,
    val teacherRemarks: String?,
    val nextClassPlan: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

data class Exam(
    val id: String,
    val studentId: String,
    val subjectId: String,
    val title: String,
    val syllabusTopic: String,
    val plannedDate: Long,
    val plannedStartTimeMinutes: Int,
    val durationMinutes: Int,
    val totalMarks: Double,
    val passingMarks: Double?,
    val examType: ExamType,
    val status: ExamStatus,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

data class ExamResult(
    val id: String,
    val examId: String,
    val studentId: String,
    val actualExamDate: Long,
    val marksObtained: Double,
    val isPassed: Boolean,
    val studentStrengths: String?,
    val studentWeaknesses: String?,
    val recommendations: String?,
    val teacherRemarks: String?,
    val gradedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

data class ExamWithResult(
    val exam: Exam,
    val result: ExamResult?
) {
    val percentage: Double?
        get() = result?.let {
            if (exam.totalMarks > 0) (it.marksObtained / exam.totalMarks) * 100.0 else 0.0
        }

    val gradeLevel: String?
        get() = percentage?.let { pct ->
            when {
                pct >= 80.0 -> "A+ (Outstanding)"
                pct >= 70.0 -> "A (Excellent)"
                pct >= 60.0 -> "A- (Very Good)"
                pct >= 50.0 -> "B (Satisfactory)"
                pct >= 40.0 -> "C (Pass)"
                else -> "F (Needs Attention)"
            }
        }
}
