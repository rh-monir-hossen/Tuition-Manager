package com.tuitionmanager.app.data.repository

import com.tuitionmanager.app.data.local.dao.*
import com.tuitionmanager.app.data.local.entity.*
import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao
) : StudentRepository {

    override fun observeActiveStudents(): Flow<List<Student>> {
        return studentDao.observeActiveStudents().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getStudentById(id: String): Student? {
        return studentDao.getStudentById(id)?.toDomain()
    }

    override fun observeStudentById(id: String): Flow<Student?> {
        return studentDao.observeStudentById(id).map { it?.toDomain() }
    }

    override suspend fun addStudent(student: Student) {
        studentDao.insert(student.toEntity())
    }

    override suspend fun updateStudent(student: Student) {
        studentDao.update(student.toEntity())
    }

    override suspend fun deleteStudent(id: String) {
        studentDao.softDelete(id, System.currentTimeMillis())
    }
}

@Singleton
class StudentDiaryRepositoryImpl @Inject constructor(
    private val diaryDao: StudentDiaryDao,
    private val sessionDao: ClassSessionDao
) : StudentDiaryRepository {

    override fun observeDiaryHistory(studentId: String): Flow<List<StudentDiary>> {
        return diaryDao.observeStudentDiaryHistory(studentId).map { list -> list.map { it.toDomain() } }
    }

    override fun filterDiaryEntries(
        studentId: String,
        subjectId: String?,
        startDateEpochMs: Long,
        endDateEpochMs: Long
    ): Flow<List<StudentDiary>> {
        return diaryDao.filterDiaryEntries(studentId, subjectId, startDateEpochMs, endDateEpochMs)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun searchDiaryEntries(studentId: String, query: String): Flow<List<StudentDiary>> {
        return diaryDao.searchDiaryEntries(studentId, query).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getDiaryEntriesForSession(sessionId: String): List<StudentDiary> {
        return diaryDao.getDiaryEntriesForSession(sessionId).map { it.toDomain() }
    }

    override suspend fun addDiaryEntry(entry: StudentDiary) {
        diaryDao.insert(entry.toEntity())
        // Auto-update classSession.topicCovered if session exists and has empty topic
        entry.classSessionId?.let { sessionId ->
            val session = sessionDao.getSessionById(sessionId)
            if (session != null && session.topicCovered.isNullOrBlank()) {
                sessionDao.updateTopicCovered(sessionId, entry.topicTitle, System.currentTimeMillis())
            }
        }
    }

    override suspend fun updateDiaryEntry(entry: StudentDiary) {
        diaryDao.update(entry.toEntity())
    }

    override suspend fun deleteDiaryEntry(id: String) {
        diaryDao.softDelete(id, System.currentTimeMillis())
    }
}

@Singleton
class ExamRepositoryImpl @Inject constructor(
    private val examDao: ExamDao,
    private val examResultDao: ExamResultDao
) : ExamRepository {

    override fun observeExamsForStudent(studentId: String): Flow<List<Exam>> {
        return examDao.observeExamsForStudent(studentId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeUpcomingExams(todayEpochMs: Long): Flow<List<Exam>> {
        return examDao.observeUpcomingExams(todayEpochMs).map { list -> list.map { it.toDomain() } }
    }

    override fun observeTodayExams(todayEpochMs: Long): Flow<List<Exam>> {
        return examDao.observeTodayExams(todayEpochMs).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getExamById(id: String): Exam? {
        return examDao.getExamById(id)?.toDomain()
    }

    override suspend fun planExam(exam: Exam) {
        examDao.insert(exam.toEntity())
    }

    override suspend fun updateExam(exam: Exam) {
        examDao.update(exam.toEntity())
    }

    override suspend fun cancelExam(id: String) {
        examDao.updateStatus(id, ExamStatus.CANCELLED.name, System.currentTimeMillis())
    }

    override suspend fun deleteExam(id: String) {
        val now = System.currentTimeMillis()
        examDao.softDelete(id, now)
        examResultDao.softDelete(id, now)
    }

    override suspend fun getResultForExam(examId: String): ExamResult? {
        return examResultDao.getResultForExam(examId)?.toDomain()
    }

    override fun observeResultForExam(examId: String): Flow<ExamResult?> {
        return examResultDao.observeResultForExam(examId).map { it?.toDomain() }
    }

    override fun observeResultsForStudent(studentId: String): Flow<List<ExamResult>> {
        return examResultDao.observeResultsForStudent(studentId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun recordExamResult(result: ExamResult) {
        examResultDao.insert(result.toEntity())
        examDao.updateStatus(result.examId, ExamStatus.COMPLETED.name, System.currentTimeMillis())
    }

    override suspend fun updateExamResult(result: ExamResult) {
        examResultDao.update(result.toEntity())
    }
}

// ----------------- Mapping Functions -----------------

fun StudentEntity.toDomain() = Student(
    id = id,
    name = name,
    institution = institution,
    classGrade = classGrade,
    phone = phone,
    guardianName = guardianName,
    guardianPhone = guardianPhone,
    address = address,
    monthlyFeeAmount = monthlyFeeAmount,
    billingCycleDay = billingCycleDay,
    isActive = isActive,
    joinedDate = joinedDate,
    guardianPreferredChannel = GuardianPreferredChannel.valueOf(guardianPreferredChannel),
    isGuardianProgressSharingEnabled = isGuardianProgressSharingEnabled,
    guardianReportLanguage = GuardianReportLanguage.valueOf(guardianReportLanguage),
    guardianReportFormat = GuardianReportFormat.valueOf(guardianReportFormat),
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun Student.toEntity() = StudentEntity(
    id = id,
    name = name,
    institution = institution,
    classGrade = classGrade,
    phone = phone,
    guardianName = guardianName,
    guardianPhone = guardianPhone,
    address = address,
    monthlyFeeAmount = monthlyFeeAmount,
    billingCycleDay = billingCycleDay,
    isActive = isActive,
    joinedDate = joinedDate,
    guardianPreferredChannel = guardianPreferredChannel.name,
    isGuardianProgressSharingEnabled = isGuardianProgressSharingEnabled,
    guardianReportLanguage = guardianReportLanguage.name,
    guardianReportFormat = guardianReportFormat.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun StudentDiaryEntity.toDomain() = StudentDiary(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    classSessionId = classSessionId,
    date = date,
    topicTitle = topicTitle,
    whatWasTaught = whatWasTaught,
    homeworkAssigned = homeworkAssigned,
    homeworkStatus = HomeworkStatus.valueOf(homeworkStatus),
    practiceGiven = practiceGiven,
    studentUnderstanding = StudentUnderstanding.valueOf(studentUnderstanding),
    teacherRemarks = teacherRemarks,
    nextClassPlan = nextClassPlan,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun StudentDiary.toEntity() = StudentDiaryEntity(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    classSessionId = classSessionId,
    date = date,
    topicTitle = topicTitle,
    whatWasTaught = whatWasTaught,
    homeworkAssigned = homeworkAssigned,
    homeworkStatus = homeworkStatus.name,
    practiceGiven = practiceGiven,
    studentUnderstanding = studentUnderstanding.name,
    teacherRemarks = teacherRemarks,
    nextClassPlan = nextClassPlan,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun ExamEntity.toDomain() = Exam(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    title = title,
    syllabusTopic = syllabusTopic,
    plannedDate = plannedDate,
    plannedStartTimeMinutes = plannedStartTimeMinutes,
    durationMinutes = durationMinutes,
    totalMarks = totalMarks,
    passingMarks = passingMarks,
    examType = ExamType.valueOf(examType),
    status = ExamStatus.valueOf(status),
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun Exam.toEntity() = ExamEntity(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    title = title,
    syllabusTopic = syllabusTopic,
    plannedDate = plannedDate,
    plannedStartTimeMinutes = plannedStartTimeMinutes,
    durationMinutes = durationMinutes,
    totalMarks = totalMarks,
    passingMarks = passingMarks,
    examType = examType.name,
    status = status.name,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun ExamResultEntity.toDomain() = ExamResult(
    id = id,
    examId = examId,
    studentId = studentId,
    actualExamDate = actualExamDate,
    marksObtained = marksObtained,
    isPassed = isPassed,
    studentStrengths = studentStrengths,
    studentWeaknesses = studentWeaknesses,
    recommendations = recommendations,
    teacherRemarks = teacherRemarks,
    gradedAt = gradedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun ExamResult.toEntity() = ExamResultEntity(
    id = id,
    examId = examId,
    studentId = studentId,
    actualExamDate = actualExamDate,
    marksObtained = marksObtained,
    isPassed = isPassed,
    studentStrengths = studentStrengths,
    studentWeaknesses = studentWeaknesses,
    recommendations = recommendations,
    teacherRemarks = teacherRemarks,
    gradedAt = gradedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)
