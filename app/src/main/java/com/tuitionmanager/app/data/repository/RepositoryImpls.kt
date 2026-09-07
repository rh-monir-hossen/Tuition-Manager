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

    override fun observeRecentStudents(): Flow<List<Student>> {
        return studentDao.observeRecentStudents().map { list -> list.map { it.toDomain() } }
    }

    override fun observeAllStudents(): Flow<List<Student>> {
        return studentDao.observeAllStudents().map { list -> list.map { it.toDomain() } }
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

    override suspend fun restoreStudent(id: String) {
        studentDao.restore(id, System.currentTimeMillis())
    }

    override suspend fun updateActiveStatus(id: String, isActive: Boolean) {
        studentDao.updateActiveStatus(id, isActive, System.currentTimeMillis())
    }
}

@Singleton
class StudentSubjectRepositoryImpl @Inject constructor(
    private val subjectDao: StudentSubjectDao
) : StudentSubjectRepository {

    override fun observeSubjectsForStudent(studentId: String): Flow<List<StudentSubject>> {
        return subjectDao.observeSubjectsForStudent(studentId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSubjectsForStudent(studentId: String): List<StudentSubject> {
        return subjectDao.getSubjectsForStudent(studentId).map { it.toDomain() }
    }

    override fun observeAllSubjects(): Flow<List<StudentSubject>> {
        return subjectDao.observeAllSubjects().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addSubject(subject: StudentSubject) {
        subjectDao.insert(subject.toEntity())
    }

    override suspend fun updateSubject(subject: StudentSubject) {
        subjectDao.update(subject.toEntity())
    }

    override suspend fun deleteSubject(id: String) {
        subjectDao.softDelete(id, System.currentTimeMillis())
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

    override fun observeAllDiaryHistory(): Flow<List<StudentDiary>> {
        return diaryDao.observeAllDiaryHistory().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getDiaryById(id: String): StudentDiary? {
        return diaryDao.getDiaryById(id)?.toDomain()
    }

    override fun observeDiaryById(id: String): Flow<StudentDiary?> {
        return diaryDao.observeDiaryById(id).map { it?.toDomain() }
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

    override fun filterAllDiaryEntries(
        studentId: String?,
        subjectId: String?,
        startDateEpochMs: Long,
        endDateEpochMs: Long
    ): Flow<List<StudentDiary>> {
        return diaryDao.filterAllDiaryEntries(studentId, subjectId, startDateEpochMs, endDateEpochMs)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun searchDiaryEntries(studentId: String, query: String): Flow<List<StudentDiary>> {
        return diaryDao.searchDiaryEntries(studentId, query).map { list -> list.map { it.toDomain() } }
    }

    override fun searchAllDiaryEntries(studentId: String?, query: String): Flow<List<StudentDiary>> {
        return diaryDao.searchAllDiaryEntries(studentId, query).map { list -> list.map { it.toDomain() } }
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

    override suspend fun restoreDiaryEntry(id: String) {
        diaryDao.restore(id, System.currentTimeMillis())
    }
}

@Singleton
class ExamRepositoryImpl @Inject constructor(
    private val examDao: ExamDao,
    private val examResultDao: ExamResultDao
) : ExamRepository {

    override fun observeAllExams(): Flow<List<Exam>> {
        return examDao.observeAllExams().map { list -> list.map { it.toDomain() } }
    }

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

    override fun observeExamById(id: String): Flow<Exam?> {
        return examDao.observeExamById(id).map { it?.toDomain() }
    }

    override suspend fun planExam(exam: Exam) {
        examDao.insert(exam.toEntity())
    }

    override suspend fun updateExam(exam: Exam) {
        examDao.update(exam.toEntity())
    }

    override suspend fun updateStatus(id: String, status: ExamStatus) {
        examDao.updateStatus(id, status.name, System.currentTimeMillis())
    }

    override suspend fun cancelExam(id: String) {
        examDao.updateStatus(id, ExamStatus.CANCELLED.name, System.currentTimeMillis())
    }

    override suspend fun deleteExam(id: String) {
        val now = System.currentTimeMillis()
        examDao.softDelete(id, now)
        examResultDao.softDelete(id, now)
    }

    override suspend fun restoreExam(id: String) {
        val now = System.currentTimeMillis()
        examDao.restore(id, now)
        examResultDao.restore(id, now)
    }

    override fun filterAllExams(
        studentId: String?,
        subjectId: String?,
        examType: ExamType?,
        status: ExamStatus?,
        startDateEpochMs: Long?,
        endDateEpochMs: Long?
    ): Flow<List<Exam>> {
        return examDao.filterAllExams(
            studentId = studentId,
            subjectId = subjectId,
            examType = examType?.name,
            status = status?.name,
            startDateEpochMs = startDateEpochMs,
            endDateEpochMs = endDateEpochMs
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun searchExams(studentId: String?, query: String): Flow<List<Exam>> {
        return examDao.searchExams(studentId, query).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getResultForExam(examId: String): ExamResult? {
        return examResultDao.getResultForExam(examId)?.toDomain()
    }

    override suspend fun getResultById(id: String): ExamResult? {
        return examResultDao.getResultById(id)?.toDomain()
    }

    override fun observeResultForExam(examId: String): Flow<ExamResult?> {
        return examResultDao.observeResultForExam(examId).map { it?.toDomain() }
    }

    override fun observeResultsForStudent(studentId: String): Flow<List<ExamResult>> {
        return examResultDao.observeResultsForStudent(studentId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeAllResults(): Flow<List<ExamResult>> {
        return examResultDao.observeAllResults().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun recordExamResult(result: ExamResult) {
        examResultDao.insert(result.toEntity())
        examDao.updateStatus(result.examId, ExamStatus.COMPLETED.name, System.currentTimeMillis())
    }

    override suspend fun updateExamResult(result: ExamResult) {
        examResultDao.update(result.toEntity())
    }

    override suspend fun deleteExamResult(id: String) {
        examResultDao.softDelete(id, System.currentTimeMillis())
    }

    override suspend fun restoreExamResult(id: String) {
        examResultDao.restore(id, System.currentTimeMillis())
    }
}

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override fun observeAllActiveSchedules(): Flow<List<Schedule>> {
        return scheduleDao.observeAllActiveSchedules().map { list -> list.map { it.toDomain() } }
    }

    override fun observeSchedulesForDay(dayOfWeek: Int): Flow<List<Schedule>> {
        return scheduleDao.observeSchedulesForDay(dayOfWeek).map { list -> list.map { it.toDomain() } }
    }

    override fun observeSchedulesForStudent(studentId: String): Flow<List<Schedule>> {
        return scheduleDao.observeSchedulesForStudent(studentId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getScheduleById(id: String): Schedule? {
        return scheduleDao.getScheduleById(id)?.toDomain()
    }

    override suspend fun getAllActiveSchedules(): List<Schedule> {
        return scheduleDao.getAllActiveSchedules().map { it.toDomain() }
    }

    override suspend fun getActiveSchedulesForDay(dayOfWeek: Int): List<Schedule> {
        return scheduleDao.getActiveSchedulesForDay(dayOfWeek).map { it.toDomain() }
    }

    override suspend fun addSchedule(schedule: Schedule) {
        scheduleDao.insert(schedule.toEntity())
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.update(schedule.toEntity())
    }

    override suspend fun updateActiveStatus(id: String, isActive: Boolean) {
        scheduleDao.updateActiveStatus(id, isActive, System.currentTimeMillis())
    }

    override suspend fun deleteSchedule(id: String) {
        scheduleDao.softDelete(id, System.currentTimeMillis())
    }
}

@Singleton
class ClassSessionRepositoryImpl @Inject constructor(
    private val classSessionDao: ClassSessionDao
) : ClassSessionRepository {

    override fun observeSessionsForStudent(studentId: String): Flow<List<ClassSession>> {
        return classSessionDao.observeSessionsForStudent(studentId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeSessionsForDate(dateEpochMs: Long): Flow<List<ClassSession>> {
        return classSessionDao.observeSessionsForDate(dateEpochMs).map { list -> list.map { it.toDomain() } }
    }

    override fun observeAllSessions(): Flow<List<ClassSession>> {
        return classSessionDao.observeAllSessions().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSessionById(id: String): ClassSession? {
        return classSessionDao.getSessionById(id)?.toDomain()
    }

    override fun observeSessionById(id: String): Flow<ClassSession?> {
        return classSessionDao.observeSessionById(id).map { it?.toDomain() }
    }

    override suspend fun getSessionsForScheduleAndDate(scheduleId: String, dateEpochMs: Long): List<ClassSession> {
        return classSessionDao.getSessionsForScheduleAndDate(scheduleId, dateEpochMs).map { it.toDomain() }
    }

    override suspend fun getSessionsForDate(dateEpochMs: Long): List<ClassSession> {
        return classSessionDao.getSessionsForDate(dateEpochMs).map { it.toDomain() }
    }

    override suspend fun getSessionsByStatus(status: SessionStatus): List<ClassSession> {
        return classSessionDao.getSessionsByStatus(status.name).map { it.toDomain() }
    }

    override suspend fun addSession(session: ClassSession) {
        classSessionDao.insert(session.toEntity())
    }

    override suspend fun updateSession(session: ClassSession) {
        classSessionDao.update(session.toEntity())
    }

    override suspend fun updateStatus(
        id: String,
        status: SessionStatus,
        remarks: String?,
        actualStart: Int?,
        actualEnd: Int?
    ) {
        classSessionDao.updateStatusWithDetails(
            id = id,
            status = status.name,
            remarks = remarks,
            actualStart = actualStart,
            actualEnd = actualEnd,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun updateTopicCovered(id: String, topic: String) {
        classSessionDao.updateTopicCovered(id, topic, System.currentTimeMillis())
    }

    override suspend fun deleteSession(id: String) {
        classSessionDao.softDelete(id, System.currentTimeMillis())
    }
}

@Singleton
class RescheduleRepositoryImpl @Inject constructor(
    private val rescheduleRecordDao: RescheduleRecordDao
) : RescheduleRepository {

    override suspend fun addRescheduleRecord(record: RescheduleRecord) {
        rescheduleRecordDao.insert(record.toEntity())
    }

    override suspend fun getByOriginalSession(sessionId: String): RescheduleRecord? {
        return rescheduleRecordDao.getByOriginalSession(sessionId)?.toDomain()
    }

    override fun observeByOriginalSession(sessionId: String): Flow<RescheduleRecord?> {
        return rescheduleRecordDao.observeByOriginalSession(sessionId).map { it?.toDomain() }
    }

    override suspend fun getByNewSession(sessionId: String): RescheduleRecord? {
        return rescheduleRecordDao.getByNewSession(sessionId)?.toDomain()
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

fun StudentSubjectEntity.toDomain() = StudentSubject(
    id = id,
    studentId = studentId,
    subjectName = subjectName,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun StudentSubject.toEntity() = StudentSubjectEntity(
    id = id,
    studentId = studentId,
    subjectName = subjectName,
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

fun ScheduleEntity.toDomain() = Schedule(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    dayOfWeek = dayOfWeek,
    startTimeMinutes = startTimeMinutes,
    endTimeMinutes = endTimeMinutes,
    effectiveStartDate = effectiveStartDate,
    effectiveEndDate = effectiveEndDate,
    isActive = isActive,
    location = location,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun Schedule.toEntity() = ScheduleEntity(
    id = id,
    studentId = studentId,
    subjectId = subjectId,
    dayOfWeek = dayOfWeek,
    startTimeMinutes = startTimeMinutes,
    endTimeMinutes = endTimeMinutes,
    effectiveStartDate = effectiveStartDate,
    effectiveEndDate = effectiveEndDate,
    isActive = isActive,
    location = location,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun ClassSessionEntity.toDomain() = ClassSession(
    id = id,
    studentId = studentId,
    scheduleId = scheduleId,
    subjectId = subjectId,
    sessionDate = sessionDate,
    scheduledStartTime = scheduledStartTime,
    scheduledEndTime = scheduledEndTime,
    actualStartTime = actualStartTime,
    actualEndTime = actualEndTime,
    status = try { SessionStatus.valueOf(status) } catch (e: Exception) { SessionStatus.SCHEDULED },
    remarks = remarks,
    topicCovered = topicCovered,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun ClassSession.toEntity() = ClassSessionEntity(
    id = id,
    studentId = studentId,
    scheduleId = scheduleId,
    subjectId = subjectId,
    sessionDate = sessionDate,
    scheduledStartTime = scheduledStartTime,
    scheduledEndTime = scheduledEndTime,
    actualStartTime = actualStartTime,
    actualEndTime = actualEndTime,
    status = status.name,
    remarks = remarks,
    topicCovered = topicCovered,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun RescheduleRecordEntity.toDomain() = RescheduleRecord(
    id = id,
    originalSessionId = originalSessionId,
    newSessionId = newSessionId,
    rescheduledBy = rescheduledBy,
    reason = reason,
    requestedAt = requestedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

fun RescheduleRecord.toEntity() = RescheduleRecordEntity(
    id = id,
    originalSessionId = originalSessionId,
    newSessionId = newSessionId,
    rescheduledBy = rescheduledBy,
    reason = reason,
    requestedAt = requestedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted,
    deletedAt = deletedAt
)

