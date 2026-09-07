package com.tuitionmanager.app.domain.repository

import com.tuitionmanager.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun observeActiveStudents(): Flow<List<Student>>
    fun observeRecentStudents(): Flow<List<Student>>
    fun observeAllStudents(): Flow<List<Student>>
    suspend fun getStudentById(id: String): Student?
    fun observeStudentById(id: String): Flow<Student?>
    suspend fun addStudent(student: Student)
    suspend fun updateStudent(student: Student)
    suspend fun deleteStudent(id: String)
    suspend fun restoreStudent(id: String)
    suspend fun updateActiveStatus(id: String, isActive: Boolean)
}

interface StudentSubjectRepository {
    fun observeSubjectsForStudent(studentId: String): Flow<List<StudentSubject>>
    suspend fun getSubjectsForStudent(studentId: String): List<StudentSubject>
    fun observeAllSubjects(): Flow<List<StudentSubject>>
    suspend fun addSubject(subject: StudentSubject)
    suspend fun updateSubject(subject: StudentSubject)
    suspend fun deleteSubject(id: String)
}

interface StudentDiaryRepository {
    fun observeDiaryHistory(studentId: String): Flow<List<StudentDiary>>
    fun observeAllDiaryHistory(): Flow<List<StudentDiary>>
    suspend fun getDiaryById(id: String): StudentDiary?
    fun observeDiaryById(id: String): Flow<StudentDiary?>
    fun filterDiaryEntries(
        studentId: String,
        subjectId: String?,
        startDateEpochMs: Long,
        endDateEpochMs: Long
    ): Flow<List<StudentDiary>>
    fun filterAllDiaryEntries(
        studentId: String?,
        subjectId: String?,
        startDateEpochMs: Long,
        endDateEpochMs: Long
    ): Flow<List<StudentDiary>>
    fun searchDiaryEntries(studentId: String, query: String): Flow<List<StudentDiary>>
    fun searchAllDiaryEntries(studentId: String?, query: String): Flow<List<StudentDiary>>
    suspend fun getDiaryEntriesForSession(sessionId: String): List<StudentDiary>
    suspend fun addDiaryEntry(entry: StudentDiary)
    suspend fun updateDiaryEntry(entry: StudentDiary)
    suspend fun deleteDiaryEntry(id: String)
    suspend fun restoreDiaryEntry(id: String)
}

interface ExamRepository {
    fun observeAllExams(): Flow<List<Exam>>
    fun observeExamsForStudent(studentId: String): Flow<List<Exam>>
    fun observeUpcomingExams(todayEpochMs: Long): Flow<List<Exam>>
    fun observeTodayExams(todayEpochMs: Long): Flow<List<Exam>>
    suspend fun getExamById(id: String): Exam?
    fun observeExamById(id: String): Flow<Exam?>
    suspend fun planExam(exam: Exam)
    suspend fun updateExam(exam: Exam)
    suspend fun updateStatus(id: String, status: ExamStatus)
    suspend fun cancelExam(id: String)
    suspend fun deleteExam(id: String)
    suspend fun restoreExam(id: String)
    fun filterAllExams(
        studentId: String?,
        subjectId: String?,
        examType: ExamType?,
        status: ExamStatus?,
        startDateEpochMs: Long?,
        endDateEpochMs: Long?
    ): Flow<List<Exam>>
    fun searchExams(studentId: String?, query: String): Flow<List<Exam>>

    // Result operations
    suspend fun getResultForExam(examId: String): ExamResult?
    suspend fun getResultById(id: String): ExamResult?
    fun observeResultForExam(examId: String): Flow<ExamResult?>
    fun observeResultsForStudent(studentId: String): Flow<List<ExamResult>>
    fun observeAllResults(): Flow<List<ExamResult>>
    suspend fun recordExamResult(result: ExamResult)
    suspend fun updateExamResult(result: ExamResult)
    suspend fun deleteExamResult(id: String)
    suspend fun restoreExamResult(id: String)
}

interface ClassSessionRepository {
    fun observeSessionsForStudent(studentId: String): Flow<List<ClassSession>>
    fun observeSessionsForDate(dateEpochMs: Long): Flow<List<ClassSession>>
    fun observeAllSessions(): Flow<List<ClassSession>>
    suspend fun getSessionById(id: String): ClassSession?
    fun observeSessionById(id: String): Flow<ClassSession?>
    suspend fun getSessionsForScheduleAndDate(scheduleId: String, dateEpochMs: Long): List<ClassSession>
    suspend fun getSessionsForDate(dateEpochMs: Long): List<ClassSession>
    suspend fun getSessionsByStatus(status: SessionStatus): List<ClassSession>
    suspend fun addSession(session: ClassSession)
    suspend fun updateSession(session: ClassSession)
    suspend fun updateStatus(
        id: String,
        status: SessionStatus,
        remarks: String? = null,
        actualStart: Int? = null,
        actualEnd: Int? = null
    )
    suspend fun updateTopicCovered(id: String, topic: String)
    suspend fun deleteSession(id: String)
}

interface ScheduleRepository {
    fun observeAllActiveSchedules(): Flow<List<Schedule>>
    fun observeSchedulesForDay(dayOfWeek: Int): Flow<List<Schedule>>
    fun observeSchedulesForStudent(studentId: String): Flow<List<Schedule>>
    suspend fun getScheduleById(id: String): Schedule?
    suspend fun getAllActiveSchedules(): List<Schedule>
    suspend fun getActiveSchedulesForDay(dayOfWeek: Int): List<Schedule>
    suspend fun addSchedule(schedule: Schedule)
    suspend fun updateSchedule(schedule: Schedule)
    suspend fun updateActiveStatus(id: String, isActive: Boolean)
    suspend fun deleteSchedule(id: String)
}

interface RescheduleRepository {
    suspend fun addRescheduleRecord(record: RescheduleRecord)
    suspend fun getByOriginalSession(sessionId: String): RescheduleRecord?
    fun observeByOriginalSession(sessionId: String): Flow<RescheduleRecord?>
    suspend fun getByNewSession(sessionId: String): RescheduleRecord?
}

interface PaymentRepository {
    suspend fun recordPayment(
        id: String,
        studentId: String,
        monthlyFeeId: String,
        amount: Double,
        date: Long,
        method: String,
        notes: String?
    )
}

interface FinanceRepository {
    suspend fun addIncome(category: String, title: String, amount: Double, date: Long, remarks: String?)
    suspend fun addExpense(category: String, title: String, amount: Double, date: Long, remarks: String?)
}

interface SyncRepository {
    suspend fun getLastSyncTimestamp(entityType: String): Long
    suspend fun updateSyncTimestamp(entityType: String, timestamp: Long, status: String)
}
