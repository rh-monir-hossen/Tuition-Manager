package com.tuitionmanager.app.usecase

import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.repository.*
import com.tuitionmanager.app.domain.usecase.*
import com.tuitionmanager.app.utils.AppResult
import com.tuitionmanager.app.utils.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class ScheduleAndSessionUseCaseTest {

    private val scheduleList = mutableListOf<Schedule>()
    private val sessionList = mutableListOf<ClassSession>()
    private val rescheduleList = mutableListOf<RescheduleRecord>()
    private val studentList = mutableListOf<Student>()
    private val subjectList = mutableListOf<StudentSubject>()

    private val fakeScheduleRepo = object : ScheduleRepository {
        override suspend fun addSchedule(schedule: Schedule) { scheduleList.add(schedule) }
        override suspend fun updateSchedule(schedule: Schedule) {
            val idx = scheduleList.indexOfFirst { it.id == schedule.id }
            if (idx >= 0) scheduleList[idx] = schedule
        }
        override suspend fun deleteSchedule(id: String) { scheduleList.removeAll { it.id == id } }
        override suspend fun updateActiveStatus(id: String, isActive: Boolean) {
            val idx = scheduleList.indexOfFirst { it.id == id }
            if (idx >= 0) scheduleList[idx] = scheduleList[idx].copy(isActive = isActive)
        }
        override suspend fun getScheduleById(id: String): Schedule? = scheduleList.find { it.id == id }
        override fun observeAllSchedules(): Flow<List<Schedule>> = flowOf(scheduleList)
        override fun observeSchedulesByDay(dayOfWeek: Int): Flow<List<Schedule>> =
            flowOf(scheduleList.filter { it.dayOfWeek == dayOfWeek })
        override fun observeSchedulesForStudent(studentId: String): Flow<List<Schedule>> =
            flowOf(scheduleList.filter { it.studentId == studentId })
        override suspend fun getActiveSchedulesForDay(dayOfWeek: Int): List<Schedule> =
            scheduleList.filter { it.dayOfWeek == dayOfWeek && it.isActive }
        override suspend fun getActiveSchedules(): List<Schedule> =
            scheduleList.filter { it.isActive }
    }

    private val fakeSessionRepo = object : ClassSessionRepository {
        override suspend fun addSession(session: ClassSession) { sessionList.add(session) }
        override suspend fun updateSession(session: ClassSession) {
            val idx = sessionList.indexOfFirst { it.id == session.id }
            if (idx >= 0) sessionList[idx] = session
        }
        override suspend fun updateStatus(id: String, status: SessionStatus, remarks: String?, actualStart: Int?, actualEnd: Int?) {
            val idx = sessionList.indexOfFirst { it.id == id }
            if (idx >= 0) {
                sessionList[idx] = sessionList[idx].copy(
                    status = status,
                    remarks = remarks,
                    actualStartTime = actualStart,
                    actualEndTime = actualEnd,
                    updatedAt = System.currentTimeMillis()
                )
            }
        }
        override suspend fun updateTopicCovered(id: String, topicCovered: String) {
            val idx = sessionList.indexOfFirst { it.id == id }
            if (idx >= 0) {
                sessionList[idx] = sessionList[idx].copy(
                    topicCovered = topicCovered,
                    updatedAt = System.currentTimeMillis()
                )
            }
        }
        override suspend fun getSessionById(id: String): ClassSession? = sessionList.find { it.id == id }
        override suspend fun getSessionsForDate(dateEpochMs: Long): List<ClassSession> =
            sessionList.filter { it.sessionDate == dateEpochMs }
        override suspend fun getSessionsForScheduleAndDate(scheduleId: String, dateEpochMs: Long): List<ClassSession> =
            sessionList.filter { it.scheduleId == scheduleId && it.sessionDate == dateEpochMs }
        override fun observeSessionsForDate(dateEpochMs: Long): Flow<List<ClassSession>> =
            flowOf(sessionList.filter { it.sessionDate == dateEpochMs })
        override fun observeSessionsForStudent(studentId: String): Flow<List<ClassSession>> =
            flowOf(sessionList.filter { it.studentId == studentId })
        override fun observeAllSessions(): Flow<List<ClassSession>> = flowOf(sessionList)
    }

    private val fakeRescheduleRepo = object : RescheduleRepository {
        override suspend fun addRescheduleRecord(record: RescheduleRecord) { rescheduleList.add(record) }
        override suspend fun getByOriginalSession(originalSessionId: String): RescheduleRecord? =
            rescheduleList.find { it.originalSessionId == originalSessionId }
        override suspend fun getByNewSession(newSessionId: String): RescheduleRecord? =
            rescheduleList.find { it.newSessionId == newSessionId }
        override fun observeReschedulesForSession(sessionId: String): Flow<List<RescheduleRecord>> =
            flowOf(rescheduleList.filter { it.originalSessionId == sessionId || it.newSessionId == sessionId })
    }

    private val fakeStudentRepo = object : StudentRepository {
        override suspend fun insert(student: Student) { studentList.add(student) }
        override suspend fun update(student: Student) {}
        override suspend fun delete(studentId: String) {}
        override suspend fun restore(studentId: String) {}
        override suspend fun updateActiveStatus(studentId: String, isActive: Boolean) {}
        override suspend fun getById(id: String): Student? = studentList.find { it.id == id }
        override fun observeAll(): Flow<List<Student>> = flowOf(studentList)
        override fun observeActive(): Flow<List<Student>> = flowOf(studentList.filter { it.isActive })
        override fun observeAllWithDeleted(): Flow<List<Student>> = flowOf(studentList)
        override fun observeById(id: String): Flow<Student?> = flowOf(studentList.find { it.id == id })
        override fun search(query: String, includeInactive: Boolean): Flow<List<Student>> = flowOf(studentList)
    }

    private val fakeSubjectRepo = object : StudentSubjectRepository {
        override suspend fun insert(subject: StudentSubject) { subjectList.add(subject) }
        override suspend fun insertAll(subjects: List<StudentSubject>) { subjectList.addAll(subjects) }
        override suspend fun delete(subjectId: String) {}
        override fun observeByStudentId(studentId: String): Flow<List<StudentSubject>> =
            flowOf(subjectList.filter { it.studentId == studentId })
        override suspend fun getByStudentId(studentId: String): List<StudentSubject> =
            subjectList.filter { it.studentId == studentId }
    }

    private lateinit var checkScheduleConflictUseCase: CheckScheduleConflictUseCase
    private lateinit var addScheduleUseCase: AddScheduleUseCase
    private lateinit var updateSessionStatusUseCase: UpdateSessionStatusUseCase
    private lateinit var rescheduleSessionUseCase: RescheduleSessionUseCase
    private lateinit var generateExpectedSessionsForDateUseCase: GenerateExpectedSessionsForDateUseCase

    private val testStudent = Student(
        id = "student-1",
        name = "Rahim Ahmed",
        grade = "Class 10",
        school = "Dhaka Collegiate School",
        phone = "+8801711223344",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private val testSubject = StudentSubject(
        id = "subject-1",
        studentId = "student-1",
        subjectName = "Physics"
    )

    @Before
    fun setup() {
        scheduleList.clear()
        sessionList.clear()
        rescheduleList.clear()
        studentList.clear()
        subjectList.clear()

        studentList.add(testStudent)
        subjectList.add(testSubject)

        checkScheduleConflictUseCase = CheckScheduleConflictUseCase(
            scheduleRepository = fakeScheduleRepo,
            studentRepository = fakeStudentRepo,
            subjectRepository = fakeSubjectRepo
        )

        addScheduleUseCase = AddScheduleUseCase(
            scheduleRepository = fakeScheduleRepo,
            checkScheduleConflictUseCase = checkScheduleConflictUseCase
        )

        updateSessionStatusUseCase = UpdateSessionStatusUseCase(
            classSessionRepository = fakeSessionRepo
        )

        rescheduleSessionUseCase = RescheduleSessionUseCase(
            classSessionRepository = fakeSessionRepo,
            rescheduleRepository = fakeRescheduleRepo,
            checkScheduleConflictUseCase = checkScheduleConflictUseCase
        )

        generateExpectedSessionsForDateUseCase = GenerateExpectedSessionsForDateUseCase(
            scheduleRepository = fakeScheduleRepo,
            classSessionRepository = fakeSessionRepo
        )
    }

    @Test
    fun testAddSchedule_successWhenNoConflict() = runTest {
        val schedule = Schedule(
            id = "schedule-1",
            studentId = "student-1",
            subjectId = "subject-1",
            dayOfWeek = 6, // Saturday
            startTimeMinutes = 600, // 10:00 AM
            endTimeMinutes = 690, // 11:30 AM
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val result = addScheduleUseCase(schedule)
        assertTrue("Schedule should be added successfully", result is AppResult.Success)
        assertEquals(1, scheduleList.size)
    }

    @Test
    fun testAddSchedule_failsWhenOverlappingSlotExists() = runTest {
        val schedule1 = Schedule(
            id = "schedule-1",
            studentId = "student-1",
            subjectId = "subject-1",
            dayOfWeek = 6, // Saturday
            startTimeMinutes = 600, // 10:00 AM
            endTimeMinutes = 690, // 11:30 AM
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        fakeScheduleRepo.addSchedule(schedule1)

        val conflictingSchedule = Schedule(
            id = "schedule-2",
            studentId = "student-1",
            subjectId = "subject-1",
            dayOfWeek = 6, // Saturday
            startTimeMinutes = 630, // 10:30 AM (overlaps)
            endTimeMinutes = 720,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val result = addScheduleUseCase(conflictingSchedule)
        assertTrue("Overlapping schedule must fail validation", result is AppResult.Error)
        assertEquals(1, scheduleList.size)
    }

    @Test
    fun testUpdateSessionStatus_completingSessionSetsActualTimes() = runTest {
        val session = ClassSession(
            id = "session-1",
            studentId = "student-1",
            scheduleId = "schedule-1",
            subjectId = "subject-1",
            sessionDate = TimeUtils.getStartOfDayEpochMs(System.currentTimeMillis()),
            scheduledStartTime = 600,
            scheduledEndTime = 690,
            status = SessionStatus.SCHEDULED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        fakeSessionRepo.addSession(session)

        val result = updateSessionStatusUseCase(
            sessionId = "session-1",
            status = SessionStatus.COMPLETED,
            remarks = "Great performance in Physics chapter 3",
            topicCovered = "Thermodynamics Laws"
        )

        assertTrue(result is AppResult.Success)
        val updated = fakeSessionRepo.getSessionById("session-1")
        assertNotNull(updated)
        assertEquals(SessionStatus.COMPLETED, updated?.status)
        assertEquals("Great performance in Physics chapter 3", updated?.remarks)
        assertEquals("Thermodynamics Laws", updated?.topicCovered)
        assertEquals(600, updated?.actualStartTime)
        assertEquals(690, updated?.actualEndTime)
    }

    @Test
    fun testRescheduleSession_marksOriginalAsRescheduledAndCreatesNewSession() = runTest {
        val today = TimeUtils.getStartOfDayEpochMs(System.currentTimeMillis())
        val tomorrow = today + 86400000L

        val originalSession = ClassSession(
            id = "session-orig",
            studentId = "student-1",
            scheduleId = "schedule-1",
            subjectId = "subject-1",
            sessionDate = today,
            scheduledStartTime = 600,
            scheduledEndTime = 690,
            status = SessionStatus.SCHEDULED,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        fakeSessionRepo.addSession(originalSession)

        val result = rescheduleSessionUseCase(
            originalSessionId = "session-orig",
            targetDateEpochMs = tomorrow,
            newStartTimeMinutes = 720, // 12:00 PM
            newEndTimeMinutes = 810, // 01:30 PM
            reason = "Student had school exam"
        )

        assertTrue("Reschedule must succeed", result is AppResult.Success)
        val newSession = (result as AppResult.Success).data

        // Verify original session is updated
        val updatedOrig = fakeSessionRepo.getSessionById("session-orig")
        assertEquals(SessionStatus.RESCHEDULED, updatedOrig?.status)

        // Verify new session exists
        assertNotNull(newSession)
        assertEquals("student-1", newSession.studentId)
        assertEquals(720, newSession.scheduledStartTime)
        assertEquals(810, newSession.scheduledEndTime)
        assertEquals(tomorrow, newSession.sessionDate)
        assertEquals(SessionStatus.SCHEDULED, newSession.status)

        // Verify reschedule audit record exists
        val auditRecord = fakeRescheduleRepo.getByOriginalSession("session-orig")
        assertNotNull(auditRecord)
        assertEquals(newSession.id, auditRecord?.newSessionId)
        assertEquals("Student had school exam", auditRecord?.reason)
        assertEquals("TUTOR", auditRecord?.rescheduledBy)
    }

    @Test
    fun testGenerateExpectedSessionsForDate_createsDeterministicSessions() = runTest {
        val testDate = TimeUtils.getStartOfDayEpochMs(System.currentTimeMillis())
        val dayOfWeek = TimeUtils.getDayOfWeek(testDate)

        val activeSchedule = Schedule(
            id = "sched-auto",
            studentId = "student-1",
            subjectId = "subject-1",
            dayOfWeek = dayOfWeek,
            startTimeMinutes = 540,
            endTimeMinutes = 630,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        fakeScheduleRepo.addSchedule(activeSchedule)

        val result = generateExpectedSessionsForDateUseCase(testDate)
        assertTrue(result is AppResult.Success)

        val sessions = (result as AppResult.Success).data
        assertEquals(1, sessions.size)
        assertEquals("sched-auto", sessions.first().scheduleId)
        assertEquals(540, sessions.first().scheduledStartTime)
        assertEquals(SessionStatus.SCHEDULED, sessions.first().status)

        // Calling it a second time should return the already created session without duplicating
        val secondResult = generateExpectedSessionsForDateUseCase(testDate)
        assertTrue(secondResult is AppResult.Success)
        val secondSessions = (secondResult as AppResult.Success).data
        assertEquals(1, secondSessions.size)
        assertEquals(sessions.first().id, secondSessions.first().id)
    }
}
