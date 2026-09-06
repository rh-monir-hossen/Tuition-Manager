package com.tuitionmanager.app.data.local.dao

import androidx.room.*
import com.tuitionmanager.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TutorProfileDao {
    @Query("SELECT * FROM tutor_profile WHERE isDeleted = 0 LIMIT 1")
    fun observeProfile(): Flow<TutorProfileEntity?>

    @Query("SELECT * FROM tutor_profile WHERE isDeleted = 0 LIMIT 1")
    suspend fun getProfile(): TutorProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: TutorProfileEntity)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE isDeleted = 0 ORDER BY name ASC")
    fun observeActiveStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id AND isDeleted = 0")
    suspend fun getStudentById(id: String): StudentEntity?

    @Query("SELECT * FROM students WHERE id = :id AND isDeleted = 0")
    fun observeStudentById(id: String): Flow<StudentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentEntity)

    @Update
    suspend fun update(student: StudentEntity)

    @Query("UPDATE students SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)

    @Query("SELECT * FROM students WHERE updatedAt > :sinceEpochMs")
    suspend fun getStudentsModifiedSince(sinceEpochMs: Long): List<StudentEntity>
}

@Dao
interface StudentSubjectDao {
    @Query("SELECT * FROM student_subjects WHERE studentId = :studentId AND isDeleted = 0 ORDER BY subjectName ASC")
    fun observeSubjectsForStudent(studentId: String): Flow<List<StudentSubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: StudentSubjectEntity)

    @Update
    suspend fun update(subject: StudentSubjectEntity)

    @Query("UPDATE student_subjects SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE studentId = :studentId AND isDeleted = 0 AND isActive = 1")
    fun observeSchedulesForStudent(studentId: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE isDeleted = 0 AND isActive = 1")
    suspend fun getAllActiveSchedules(): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ScheduleEntity)

    @Update
    suspend fun update(schedule: ScheduleEntity)

    @Query("UPDATE schedules SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)
}

@Dao
interface ClassSessionDao {
    @Query("SELECT * FROM class_sessions WHERE studentId = :studentId AND isDeleted = 0 ORDER BY sessionDate DESC")
    fun observeSessionsForStudent(studentId: String): Flow<List<ClassSessionEntity>>

    @Query("SELECT * FROM class_sessions WHERE sessionDate = :dateEpochMs AND isDeleted = 0 ORDER BY scheduledStartTime ASC")
    fun observeSessionsForDate(dateEpochMs: Long): Flow<List<ClassSessionEntity>>

    @Query("SELECT * FROM class_sessions WHERE id = :id AND isDeleted = 0")
    suspend fun getSessionById(id: String): ClassSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ClassSessionEntity)

    @Update
    suspend fun update(session: ClassSessionEntity)

    @Query("UPDATE class_sessions SET topicCovered = :topic, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTopicCovered(id: String, topic: String, updatedAt: Long)

    @Query("UPDATE class_sessions SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)

    @Query("UPDATE class_sessions SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)
}

@Dao
interface RescheduleRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RescheduleRecordEntity)

    @Query("SELECT * FROM reschedule_records WHERE originalSessionId = :sessionId AND isDeleted = 0")
    suspend fun getByOriginalSession(sessionId: String): RescheduleRecordEntity?
}

@Dao
interface BackupClassDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(backup: BackupClassEntity)

    @Query("SELECT * FROM backup_classes WHERE missedSessionId = :sessionId AND isDeleted = 0")
    suspend fun getByMissedSession(sessionId: String): BackupClassEntity?
}

@Dao
interface StudentDiaryDao {
    @Query("""
        SELECT * FROM student_diaries 
        WHERE studentId = :studentId AND isDeleted = 0 
        ORDER BY date DESC, createdAt DESC
    """)
    fun observeStudentDiaryHistory(studentId: String): Flow<List<StudentDiaryEntity>>

    @Query("""
        SELECT * FROM student_diaries 
        WHERE studentId = :studentId 
          AND (:subjectId IS NULL OR subjectId = :subjectId)
          AND date >= :startDateEpochMs 
          AND date <= :endDateEpochMs 
          AND isDeleted = 0
        ORDER BY date DESC
    """)
    fun filterDiaryEntries(
        studentId: String,
        subjectId: String?,
        startDateEpochMs: Long,
        endDateEpochMs: Long
    ): Flow<List<StudentDiaryEntity>>

    @Query("""
        SELECT * FROM student_diaries 
        WHERE studentId = :studentId 
          AND isDeleted = 0
          AND (topicTitle LIKE '%' || :query || '%' 
               OR whatWasTaught LIKE '%' || :query || '%' 
               OR homeworkAssigned LIKE '%' || :query || '%')
        ORDER BY date DESC
    """)
    fun searchDiaryEntries(studentId: String, query: String): Flow<List<StudentDiaryEntity>>

    @Query("SELECT * FROM student_diaries WHERE classSessionId = :sessionId AND isDeleted = 0")
    suspend fun getDiaryEntriesForSession(sessionId: String): List<StudentDiaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diary: StudentDiaryEntity)

    @Update
    suspend fun update(diary: StudentDiaryEntity)

    @Query("UPDATE student_diaries SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)

    @Query("UPDATE student_diaries SET isDeleted = 0, deletedAt = NULL, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restore(id: String, updatedAt: Long)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams WHERE studentId = :studentId AND isDeleted = 0 ORDER BY plannedDate DESC")
    fun observeExamsForStudent(studentId: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE plannedDate >= :todayEpochMs AND status = 'PLANNED' AND isDeleted = 0 ORDER BY plannedDate ASC, plannedStartTimeMinutes ASC")
    fun observeUpcomingExams(todayEpochMs: Long): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE plannedDate = :todayEpochMs AND isDeleted = 0 ORDER BY plannedStartTimeMinutes ASC")
    fun observeTodayExams(todayEpochMs: Long): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :id AND isDeleted = 0")
    suspend fun getExamById(id: String): ExamEntity?

    @Query("""
        SELECT * FROM exams 
        WHERE studentId = :studentId 
          AND (:status IS NULL OR status = :status)
          AND (:subjectId IS NULL OR subjectId = :subjectId)
          AND plannedDate >= :startDateEpochMs 
          AND plannedDate <= :endDateEpochMs 
          AND isDeleted = 0 
        ORDER BY plannedDate DESC
    """)
    fun filterExams(
        studentId: String,
        status: String?,
        subjectId: String?,
        startDateEpochMs: Long,
        endDateEpochMs: Long
    ): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exam: ExamEntity)

    @Update
    suspend fun update(exam: ExamEntity)

    @Query("UPDATE exams SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)

    @Query("UPDATE exams SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)
}

@Dao
interface ExamResultDao {
    @Query("SELECT * FROM exam_results WHERE examId = :examId AND isDeleted = 0 LIMIT 1")
    suspend fun getResultForExam(examId: String): ExamResultEntity?

    @Query("SELECT * FROM exam_results WHERE examId = :examId AND isDeleted = 0 LIMIT 1")
    fun observeResultForExam(examId: String): Flow<ExamResultEntity?>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId AND isDeleted = 0 ORDER BY actualExamDate DESC")
    fun observeResultsForStudent(studentId: String): Flow<List<ExamResultEntity>>

    @Query("""
        SELECT * FROM exam_results 
        WHERE studentId = :studentId 
          AND actualExamDate >= :startDateEpochMs 
          AND actualExamDate <= :endDateEpochMs 
          AND isDeleted = 0 
        ORDER BY actualExamDate DESC
    """)
    fun observeResultsByDateRange(
        studentId: String,
        startDateEpochMs: Long,
        endDateEpochMs: Long
    ): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: ExamResultEntity)

    @Update
    suspend fun update(result: ExamResultEntity)

    @Query("UPDATE exam_results SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)
}

@Dao
interface MonthlyFeeDao {
    @Query("SELECT * FROM monthly_fees WHERE studentId = :studentId AND isDeleted = 0 ORDER BY year DESC, month DESC")
    fun observeFeesForStudent(studentId: String): Flow<List<MonthlyFeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fee: MonthlyFeeEntity)

    @Update
    suspend fun update(fee: MonthlyFeeEntity)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE studentId = :studentId AND isDeleted = 0 ORDER BY paymentDate DESC")
    fun observePaymentsForStudent(studentId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentEntity)

    @Update
    suspend fun update(payment: PaymentEntity)
}

@Dao
interface IncomeDao {
    @Query("SELECT * FROM income WHERE isDeleted = 0 ORDER BY date DESC")
    fun observeAllIncome(): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: IncomeEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE isDeleted = 0 ORDER BY date DESC")
    fun observeAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE studentId = :studentId AND isDeleted = 0 ORDER BY isPinned DESC, createdAt DESC")
    fun observeNotesForStudent(studentId: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("UPDATE notes SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long)
}

@Dao
interface AppSettingsDao {
    @Query("SELECT value FROM app_settings WHERE key = :key LIMIT 1")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingsEntity)
}

@Dao
interface BackupMetadataDao {
    @Query("SELECT * FROM backup_metadata ORDER BY timestamp DESC LIMIT 10")
    fun observeBackupHistory(): Flow<List<BackupMetadataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: BackupMetadataEntity)
}

@Dao
interface SyncMetadataDao {
    @Query("SELECT * FROM sync_metadata WHERE entityType = :entityType LIMIT 1")
    suspend fun getMetadata(entityType: String): SyncMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(metadata: SyncMetadataEntity)
}
