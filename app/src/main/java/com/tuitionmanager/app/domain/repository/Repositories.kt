package com.tuitionmanager.app.domain.repository

import com.tuitionmanager.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun observeActiveStudents(): Flow<List<Student>>
    suspend fun getStudentById(id: String): Student?
    fun observeStudentById(id: String): Flow<Student?>
    suspend fun addStudent(student: Student)
    suspend fun updateStudent(student: Student)
    suspend fun deleteStudent(id: String)
}

interface StudentDiaryRepository {
    fun observeDiaryHistory(studentId: String): Flow<List<StudentDiary>>
    fun filterDiaryEntries(
        studentId: String,
        subjectId: String?,
        startDateEpochMs: Long,
        endDateEpochMs: Long
    ): Flow<List<StudentDiary>>
    fun searchDiaryEntries(studentId: String, query: String): Flow<List<StudentDiary>>
    suspend fun getDiaryEntriesForSession(sessionId: String): List<StudentDiary>
    suspend fun addDiaryEntry(entry: StudentDiary)
    suspend fun updateDiaryEntry(entry: StudentDiary)
    suspend fun deleteDiaryEntry(id: String)
}

interface ExamRepository {
    fun observeExamsForStudent(studentId: String): Flow<List<Exam>>
    fun observeUpcomingExams(todayEpochMs: Long): Flow<List<Exam>>
    fun observeTodayExams(todayEpochMs: Long): Flow<List<Exam>>
    suspend fun getExamById(id: String): Exam?
    suspend fun planExam(exam: Exam)
    suspend fun updateExam(exam: Exam)
    suspend fun cancelExam(id: String)
    suspend fun deleteExam(id: String)

    // Result operations
    suspend fun getResultForExam(examId: String): ExamResult?
    fun observeResultForExam(examId: String): Flow<ExamResult?>
    fun observeResultsForStudent(studentId: String): Flow<List<ExamResult>>
    suspend fun recordExamResult(result: ExamResult)
    suspend fun updateExamResult(result: ExamResult)
}

interface ClassSessionRepository {
    fun observeSessionsForStudent(studentId: String): Flow<List<ClassSession>>
    fun observeSessionsForDate(dateEpochMs: Long): Flow<List<ClassSession>>
    suspend fun getSessionById(id: String): ClassSession?
    suspend fun addSession(session: ClassSession)
    suspend fun updateSession(session: ClassSession)
    suspend fun updateTopicCovered(id: String, topic: String)
    suspend fun deleteSession(id: String)
}

interface ScheduleRepository {
    fun observeSchedulesForStudent(studentId: String): Flow<List<Schedule>>
    suspend fun getAllActiveSchedules(): List<Schedule>
    suspend fun addSchedule(schedule: Schedule)
    suspend fun updateSchedule(schedule: Schedule)
    suspend fun deleteSchedule(id: String)
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
