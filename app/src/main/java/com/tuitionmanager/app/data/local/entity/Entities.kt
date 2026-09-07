package com.tuitionmanager.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tutor_profile")
data class TutorProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val defaultHourlyRate: Double,
    val defaultMonthlyFee: Double,
    val currencyCode: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "students",
    indices = [
        Index("isActive"),
        Index("isDeleted")
    ]
)
data class StudentEntity(
    @PrimaryKey val id: String,
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
    // STEP 2 Additions
    val guardianPreferredChannel: String, // WHATSAPP, SMS, etc.
    val isGuardianProgressSharingEnabled: Boolean,
    val guardianReportLanguage: String, // EN, BN
    val guardianReportFormat: String, // TEXT_SUMMARY, DETAILED_PDF
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "student_subjects",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("studentId"),
        Index("isDeleted")
    ]
)
data class StudentSubjectEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val subjectName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentSubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("studentId"),
        Index("subjectId"),
        Index("dayOfWeek"),
        Index("isActive"),
        Index("isDeleted")
    ]
)
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val subjectId: String,
    val dayOfWeek: Int,
    val startTimeMinutes: Int,
    val endTimeMinutes: Int,
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

@Entity(
    tableName = "class_sessions",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = StudentSubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("studentId", "sessionDate"),
        Index("scheduleId"),
        Index("subjectId"),
        Index("status"),
        Index("isDeleted")
    ]
)
data class ClassSessionEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val scheduleId: String?,
    val subjectId: String,
    val sessionDate: Long,
    val scheduledStartTime: Int,
    val scheduledEndTime: Int,
    val actualStartTime: Int?,
    val actualEndTime: Int?,
    val status: String,
    val remarks: String?,
    val topicCovered: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "reschedule_records",
    foreignKeys = [
        ForeignKey(
            entity = ClassSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["originalSessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClassSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["newSessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("originalSessionId"),
        Index("newSessionId"),
        Index("isDeleted")
    ]
)
data class RescheduleRecordEntity(
    @PrimaryKey val id: String,
    val originalSessionId: String,
    val newSessionId: String,
    val rescheduledBy: String,
    val reason: String?,
    val requestedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "backup_classes",
    foreignKeys = [
        ForeignKey(
            entity = ClassSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["missedSessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClassSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["backupSessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("missedSessionId"),
        Index("backupSessionId"),
        Index("isDeleted")
    ]
)
data class BackupClassEntity(
    @PrimaryKey val id: String,
    val missedSessionId: String,
    val backupSessionId: String,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "student_diaries",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentSubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = ClassSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["classSessionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("studentId", "date"),
        Index("studentId", "subjectId", "date"),
        Index("classSessionId"),
        Index("homeworkStatus"),
        Index("isDeleted")
    ]
)
data class StudentDiaryEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val subjectId: String,
    val classSessionId: String?,
    val date: Long,
    val topicTitle: String,
    val whatWasTaught: String,
    val homeworkAssigned: String?,
    val homeworkStatus: String,
    val practiceGiven: String?,
    val studentUnderstanding: String,
    val teacherRemarks: String?,
    val nextClassPlan: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "exams",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentSubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("studentId", "plannedDate"),
        Index("studentId", "status"),
        Index("plannedDate", "status"),
        Index("isDeleted")
    ]
)
data class ExamEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val subjectId: String,
    val title: String,
    val syllabusTopic: String,
    val plannedDate: Long,
    val plannedStartTimeMinutes: Int,
    val durationMinutes: Int,
    val totalMarks: Double,
    val passingMarks: Double?,
    val examType: String,
    val status: String,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "exam_results",
    foreignKeys = [
        ForeignKey(
            entity = ExamEntity::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("examId", unique = true),
        Index("studentId", "actualExamDate"),
        Index("isDeleted")
    ]
)
data class ExamResultEntity(
    @PrimaryKey val id: String,
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

@Entity(
    tableName = "monthly_fees",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("studentId", "year", "month", unique = true),
        Index("isDeleted")
    ]
)
data class MonthlyFeeEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val year: Int,
    val month: Int,
    val baseAmount: Double,
    val adjustmentAmount: Double,
    val finalAmount: Double,
    val dueDate: Long,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MonthlyFeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["monthlyFeeId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("studentId", "paymentDate"),
        Index("monthlyFeeId"),
        Index("isDeleted")
    ]
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val monthlyFeeId: String,
    val amountPaid: Double,
    val paymentDate: Long,
    val paymentMethod: String,
    val transactionReference: String?,
    val receiptNumber: String?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "income",
    foreignKeys = [
        ForeignKey(
            entity = PaymentEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedPaymentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("date"),
        Index("category"),
        Index("linkedPaymentId"),
        Index("isDeleted")
    ]
)
data class IncomeEntity(
    @PrimaryKey val id: String,
    val category: String,
    val sourceTitle: String,
    val amount: Double,
    val date: Long,
    val linkedPaymentId: String?,
    val remarks: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "expenses",
    indices = [
        Index("date"),
        Index("category"),
        Index("isDeleted")
    ]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val category: String,
    val title: String,
    val amount: Double,
    val date: Long,
    val receiptImageUri: String?,
    val remarks: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("studentId"),
        Index("isPinned"),
        Index("isDeleted")
    ]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val title: String,
    val content: String,
    val isPinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long
)

@Entity(tableName = "backup_metadata")
data class BackupMetadataEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val backupType: String,
    val driveFileId: String?,
    val recordCount: Int,
    val status: String,
    val errorMessage: String?
)

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val entityType: String,
    val lastSyncedAt: Long,
    val lastSyncStatus: String,
    val deviceId: String,
    val updatedAt: Long
)
