package com.tuitionmanager.app.domain.usecase

import com.tuitionmanager.app.data.local.dao.ClassSessionDao
import com.tuitionmanager.app.data.local.dao.ExamResultDao
import com.tuitionmanager.app.data.local.dao.StudentDiaryDao
import com.tuitionmanager.app.domain.engine.ExamEvaluationEngine
import com.tuitionmanager.app.domain.engine.ScheduleConflictEngine
import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.repository.*
import com.tuitionmanager.app.domain.validation.*
import com.tuitionmanager.app.utils.AppResult
import com.tuitionmanager.app.utils.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

// ----------------- Student Use Cases -----------------

class AddStudentUseCase @Inject constructor(
    private val studentRepository: StudentRepository,
    private val subjectRepository: StudentSubjectRepository
) {
    suspend operator fun invoke(
        student: Student,
        initialSubjects: List<String> = emptyList()
    ): AppResult<Unit> {
        val validation = StudentValidator.validate(student.name)
        if (!validation.isValid) {
            return AppResult.Error((validation as ValidationResult.Invalid).errorMessage)
        }
        return try {
            studentRepository.addStudent(student)
            val now = System.currentTimeMillis()
            initialSubjects.filter { it.isNotBlank() }.forEach { subName ->
                subjectRepository.addSubject(
                    StudentSubject(
                        id = UUID.randomUUID().toString(),
                        studentId = student.id,
                        subjectName = subName.trim(),
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to add student")
        }
    }
}

class UpdateStudentUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(student: Student): AppResult<Unit> {
        val validation = StudentValidator.validate(student.name)
        if (!validation.isValid) {
            return AppResult.Error((validation as ValidationResult.Invalid).errorMessage)
        }
        return try {
            studentRepository.updateStudent(student)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update student")
        }
    }
}

class DeleteStudentUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(studentId: String): AppResult<Unit> {
        return try {
            studentRepository.deleteStudent(studentId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to delete student")
        }
    }
}

class RestoreStudentUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(studentId: String): AppResult<Unit> {
        return try {
            studentRepository.restoreStudent(studentId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to restore student")
        }
    }
}

class ToggleStudentActiveUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(studentId: String, isActive: Boolean): AppResult<Unit> {
        return try {
            studentRepository.updateActiveStatus(studentId, isActive)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update student status")
        }
    }
}

class ObserveStudentsUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    operator fun invoke(
        includeInactive: Boolean = true,
        sortByRecent: Boolean = false,
        query: String = ""
    ): Flow<List<Student>> {
        val baseFlow = if (sortByRecent) {
            studentRepository.observeRecentStudents()
        } else if (includeInactive) {
            studentRepository.observeAllStudents()
        } else {
            studentRepository.observeActiveStudents()
        }

        return baseFlow.map { list ->
            var filtered = list
            if (!includeInactive) {
                filtered = filtered.filter { it.isActive && !it.isDeleted }
            }
            if (query.isNotBlank()) {
                val q = query.trim().lowercase()
                filtered = filtered.filter {
                    it.name.lowercase().contains(q) ||
                    (it.institution?.lowercase()?.contains(q) == true) ||
                    (it.classGrade?.lowercase()?.contains(q) == true) ||
                    (it.phone?.contains(q) == true) ||
                    (it.guardianName?.lowercase()?.contains(q) == true)
                }
            }
            filtered
        }
    }
}

class GetStudentByIdUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(id: String): Student? = studentRepository.getStudentById(id)
}

class ObserveStudentByIdUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    operator fun invoke(id: String): Flow<Student?> = studentRepository.observeStudentById(id)
}

class ObserveSubjectsForStudentUseCase @Inject constructor(
    private val subjectRepository: StudentSubjectRepository
) {
    operator fun invoke(studentId: String): Flow<List<StudentSubject>> =
        subjectRepository.observeSubjectsForStudent(studentId)
}

class AddSubjectUseCase @Inject constructor(
    private val subjectRepository: StudentSubjectRepository
) {
    suspend operator fun invoke(studentId: String, subjectName: String): AppResult<Unit> {
        if (subjectName.isBlank()) return AppResult.Error("Subject name cannot be empty")
        return try {
            val now = System.currentTimeMillis()
            val subject = StudentSubject(
                id = UUID.randomUUID().toString(),
                studentId = studentId,
                subjectName = subjectName.trim(),
                createdAt = now,
                updatedAt = now
            )
            subjectRepository.addSubject(subject)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to add subject")
        }
    }
}

class DeleteSubjectUseCase @Inject constructor(
    private val subjectRepository: StudentSubjectRepository
) {
    suspend operator fun invoke(subjectId: String): AppResult<Unit> {
        return try {
            subjectRepository.deleteSubject(subjectId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to delete subject")
        }
    }
}

class GetStudentStatsUseCase @Inject constructor(
    private val sessionDao: ClassSessionDao,
    private val diaryDao: StudentDiaryDao,
    private val examResultDao: ExamResultDao,
    private val evaluationEngine: ExamEvaluationEngine
) {
    operator fun invoke(studentId: String): Flow<StudentStats> {
        return combine(
            sessionDao.observeSessionsForStudent(studentId),
            diaryDao.observeStudentDiaryHistory(studentId),
            examResultDao.observeResultsForStudent(studentId)
        ) { sessions, diaries, results ->
            val total = sessions.size
            val completed = sessions.count { it.status == SessionStatus.COMPLETED.name }
            val missed = sessions.count {
                it.status == SessionStatus.CANCELLED_BY_STUDENT.name ||
                it.status == SessionStatus.CANCELLED_BY_TUTOR.name
            }
            val avgPercentage = if (results.isNotEmpty()) {
                val totalPercent = results.sumOf {
                    evaluationEngine.calculatePercentage(it.marksObtained, it.totalMarks)
                }
                totalPercent / results.size
            } else null

            StudentStats(
                totalClasses = total,
                completedClasses = completed,
                missedClasses = missed,
                diaryEntriesCount = diaries.size,
                examsCompletedCount = results.size,
                averageExamPercentage = avgPercentage
            )
        }
    }
}

// ----------------- Diary Use Cases -----------------

class AddDiaryEntryUseCase @Inject constructor(
    private val diaryRepository: StudentDiaryRepository
) {
    suspend operator fun invoke(entry: StudentDiary): AppResult<Unit> {
        val validation = DiaryValidator.validate(
            studentId = entry.studentId,
            subjectId = entry.subjectId,
            topicTitle = entry.topicTitle,
            whatWasTaught = entry.whatWasTaught
        )
        if (!validation.isValid) {
            return AppResult.Error((validation as ValidationResult.Invalid).errorMessage)
        }
        return try {
            diaryRepository.addDiaryEntry(entry)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to save diary entry")
        }
    }
}

class UpdateDiaryEntryUseCase @Inject constructor(
    private val diaryRepository: StudentDiaryRepository
) {
    suspend operator fun invoke(entry: StudentDiary): AppResult<Unit> {
        val validation = DiaryValidator.validate(
            studentId = entry.studentId,
            subjectId = entry.subjectId,
            topicTitle = entry.topicTitle,
            whatWasTaught = entry.whatWasTaught
        )
        if (!validation.isValid) {
            return AppResult.Error((validation as ValidationResult.Invalid).errorMessage)
        }
        return try {
            diaryRepository.updateDiaryEntry(entry)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update diary entry")
        }
    }
}

class DeleteDiaryEntryUseCase @Inject constructor(
    private val diaryRepository: StudentDiaryRepository
) {
    suspend operator fun invoke(id: String): AppResult<Unit> {
        return try {
            diaryRepository.deleteDiaryEntry(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to delete diary entry")
        }
    }
}

class RestoreDiaryEntryUseCase @Inject constructor(
    private val diaryRepository: StudentDiaryRepository
) {
    suspend operator fun invoke(id: String): AppResult<Unit> {
        return try {
            diaryRepository.restoreDiaryEntry(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to restore diary entry")
        }
    }
}

class GetDiaryByIdUseCase @Inject constructor(
    private val diaryRepository: StudentDiaryRepository
) {
    suspend operator fun invoke(id: String): StudentDiary? = diaryRepository.getDiaryById(id)
}

class ObserveDiaryByIdUseCase @Inject constructor(
    private val diaryRepository: StudentDiaryRepository
) {
    operator fun invoke(id: String): Flow<StudentDiary?> = diaryRepository.observeDiaryById(id)
}

class ObserveDiaryHistoryUseCase @Inject constructor(
    private val diaryRepository: StudentDiaryRepository
) {
    operator fun invoke(studentId: String? = null): Flow<List<StudentDiary>> {
        return if (studentId.isNullOrBlank()) {
            diaryRepository.observeAllDiaryHistory()
        } else {
            diaryRepository.observeDiaryHistory(studentId)
        }
    }
}

class SearchDiaryUseCase @Inject constructor(
    private val diaryRepository: StudentDiaryRepository
) {
    operator fun invoke(studentId: String?, query: String): Flow<List<StudentDiary>> {
        return if (studentId.isNullOrBlank()) {
            diaryRepository.searchAllDiaryEntries(null, query)
        } else {
            diaryRepository.searchDiaryEntries(studentId, query)
        }
    }
}

class FilterDiaryHistoryUseCase @Inject constructor(
    private val diaryRepository: StudentDiaryRepository
) {
    operator fun invoke(
        studentId: String?,
        subjectId: String?,
        startDateEpochMs: Long,
        endDateEpochMs: Long
    ): Flow<List<StudentDiary>> {
        return if (studentId.isNullOrBlank()) {
            diaryRepository.filterAllDiaryEntries(null, subjectId, startDateEpochMs, endDateEpochMs)
        } else {
            diaryRepository.filterDiaryEntries(studentId, subjectId, startDateEpochMs, endDateEpochMs)
        }
    }
}

// ----------------- Exam Use Cases -----------------

class PlanExamUseCase @Inject constructor(
    private val examRepository: ExamRepository
) {
    suspend operator fun invoke(exam: Exam): AppResult<Unit> {
        val validation = ExamValidator.validate(
            studentId = exam.studentId,
            subjectId = exam.subjectId,
            title = exam.title,
            totalMarks = exam.totalMarks,
            durationMinutes = exam.durationMinutes,
            startTimeMinutes = exam.plannedStartTimeMinutes
        )
        if (!validation.isValid) {
            return AppResult.Error((validation as ValidationResult.Invalid).errorMessage)
        }
        return try {
            examRepository.planExam(exam)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to plan exam")
        }
    }
}

class UpdateExamUseCase @Inject constructor(
    private val examRepository: ExamRepository
) {
    suspend operator fun invoke(exam: Exam): AppResult<Unit> {
        val validation = ExamValidator.validate(
            studentId = exam.studentId,
            subjectId = exam.subjectId,
            title = exam.title,
            totalMarks = exam.totalMarks,
            durationMinutes = exam.durationMinutes,
            startTimeMinutes = exam.plannedStartTimeMinutes
        )
        if (!validation.isValid) {
            return AppResult.Error((validation as ValidationResult.Invalid).errorMessage)
        }
        return try {
            examRepository.updateExam(exam)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update exam")
        }
    }
}

class CancelExamUseCase @Inject constructor(
    private val examRepository: ExamRepository
) {
    suspend operator fun invoke(id: String): AppResult<Unit> {
        return try {
            examRepository.cancelExam(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to cancel exam")
        }
    }
}

class DeleteExamUseCase @Inject constructor(
    private val examRepository: ExamRepository
) {
    suspend operator fun invoke(id: String): AppResult<Unit> {
        return try {
            examRepository.deleteExam(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to delete exam")
        }
    }
}

class RecordExamResultUseCase @Inject constructor(
    private val examRepository: ExamRepository,
    private val evaluationEngine: ExamEvaluationEngine
) {
    suspend operator fun invoke(result: ExamResult): AppResult<Unit> {
        val exam = examRepository.getExamById(result.examId)
            ?: return AppResult.Error("Associated exam event was not found")

        val validation = ExamResultValidator.validate(
            marksObtained = result.marksObtained,
            totalMarks = exam.totalMarks,
            actualExamDate = result.actualExamDate
        )
        if (!validation.isValid) {
            return AppResult.Error((validation as ValidationResult.Invalid).errorMessage)
        }

        // Auto-evaluate isPassed
        val isPassed = evaluationEngine.determineIsPassed(
            marksObtained = result.marksObtained,
            totalMarks = exam.totalMarks,
            passingMarks = exam.passingMarks
        )

        return try {
            examRepository.recordExamResult(result.copy(isPassed = isPassed))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to record exam result")
        }
    }
}

class GetExamHistoryUseCase @Inject constructor(
    private val examRepository: ExamRepository
) {
    operator fun invoke(studentId: String): Flow<List<Exam>> {
        return examRepository.observeExamsForStudent(studentId)
    }
}

// ----------------- Schedule Use Cases -----------------

data class ConflictCheckResult(
    val hasConflict: Boolean,
    val conflicts: List<ScheduleConflictDetails>
)

class CheckScheduleConflictUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val studentRepository: StudentRepository,
    private val subjectRepository: StudentSubjectRepository,
    private val conflictEngine: ScheduleConflictEngine
) {
    suspend operator fun invoke(
        dayOfWeek: Int,
        startTimeMinutes: Int,
        endTimeMinutes: Int,
        ignoreScheduleId: String? = null
    ): ConflictCheckResult {
        if (startTimeMinutes >= endTimeMinutes || dayOfWeek !in 1..7) {
            return ConflictCheckResult(hasConflict = false, conflicts = emptyList())
        }

        val activeSchedules = scheduleRepository.getAllActiveSchedules()
        val result = conflictEngine.checkConflict(
            proposedDayOfWeek = dayOfWeek,
            proposedStartMinutes = startTimeMinutes,
            proposedEndMinutes = endTimeMinutes,
            existingSchedules = activeSchedules,
            ignoreScheduleId = ignoreScheduleId
        )

        if (!result.hasConflict) {
            return ConflictCheckResult(hasConflict = false, conflicts = emptyList())
        }

        val conflictDetails = result.conflictingSchedules.map { schedule ->
            val student = studentRepository.getStudentById(schedule.studentId)
            val subjects = subjectRepository.getSubjectsForStudent(schedule.studentId)
            val subject = subjects.firstOrNull { it.id == schedule.subjectId }
            val dayName = RoutineDay.fromDayOfWeek(schedule.dayOfWeek).displayName
            val timeFormatted = "${TimeUtils.formatMinutesToTime(schedule.startTimeMinutes)} - ${TimeUtils.formatMinutesToTime(schedule.endTimeMinutes)}"

            ScheduleConflictDetails(
                schedule = schedule,
                studentName = student?.name ?: "Unknown Student",
                subjectName = subject?.subjectName ?: "General",
                dayName = dayName,
                timeFormatted = timeFormatted
            )
        }

        return ConflictCheckResult(hasConflict = true, conflicts = conflictDetails)
    }
}

class AddScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val checkScheduleConflictUseCase: CheckScheduleConflictUseCase
) {
    suspend operator fun invoke(schedule: Schedule): AppResult<Unit> {
        val validation = ScheduleValidator.validate(
            studentId = schedule.studentId,
            subjectId = schedule.subjectId,
            dayOfWeek = schedule.dayOfWeek,
            startTimeMinutes = schedule.startTimeMinutes,
            endTimeMinutes = schedule.endTimeMinutes
        )
        if (!validation.isValid) {
            return AppResult.Error((validation as ValidationResult.Invalid).errorMessage)
        }

        val conflictCheck = checkScheduleConflictUseCase(
            dayOfWeek = schedule.dayOfWeek,
            startTimeMinutes = schedule.startTimeMinutes,
            endTimeMinutes = schedule.endTimeMinutes
        )

        if (conflictCheck.hasConflict) {
            val first = conflictCheck.conflicts.first()
            return AppResult.Error(
                "Time slot conflicts with ${first.studentName} (${first.subjectName}) on ${first.dayName} at ${first.timeFormatted}"
            )
        }

        return try {
            scheduleRepository.addSchedule(schedule)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to save schedule")
        }
    }
}

class UpdateScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val checkScheduleConflictUseCase: CheckScheduleConflictUseCase
) {
    suspend operator fun invoke(schedule: Schedule): AppResult<Unit> {
        val validation = ScheduleValidator.validate(
            studentId = schedule.studentId,
            subjectId = schedule.subjectId,
            dayOfWeek = schedule.dayOfWeek,
            startTimeMinutes = schedule.startTimeMinutes,
            endTimeMinutes = schedule.endTimeMinutes
        )
        if (!validation.isValid) {
            return AppResult.Error((validation as ValidationResult.Invalid).errorMessage)
        }

        val conflictCheck = checkScheduleConflictUseCase(
            dayOfWeek = schedule.dayOfWeek,
            startTimeMinutes = schedule.startTimeMinutes,
            endTimeMinutes = schedule.endTimeMinutes,
            ignoreScheduleId = schedule.id
        )

        if (conflictCheck.hasConflict) {
            val first = conflictCheck.conflicts.first()
            return AppResult.Error(
                "Time slot conflicts with ${first.studentName} (${first.subjectName}) on ${first.dayName} at ${first.timeFormatted}"
            )
        }

        return try {
            scheduleRepository.updateSchedule(schedule)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update schedule")
        }
    }
}

class DeleteScheduleUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(id: String): AppResult<Unit> {
        return try {
            scheduleRepository.deleteSchedule(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to delete schedule")
        }
    }
}

class ToggleScheduleActiveUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(id: String, isActive: Boolean): AppResult<Unit> {
        return try {
            scheduleRepository.updateActiveStatus(id, isActive)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update schedule status")
        }
    }
}

class ObserveWeeklyRoutineUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val studentRepository: StudentRepository,
    private val subjectRepository: StudentSubjectRepository
) {
    operator fun invoke(): Flow<List<ScheduleWithDetails>> {
        return combine(
            scheduleRepository.observeAllActiveSchedules(),
            studentRepository.observeAllStudents()
        ) { schedules, students ->
            val studentMap = students.associateBy { it.id }

            schedules.map { schedule ->
                val student = studentMap[schedule.studentId]
                val subjects = subjectRepository.getSubjectsForStudent(schedule.studentId)
                val subject = subjects.firstOrNull { it.id == schedule.subjectId }

                ScheduleWithDetails(
                    schedule = schedule,
                    studentName = student?.name ?: "Unknown Student",
                    studentPhone = student?.phone,
                    subjectName = subject?.subjectName ?: "Tuition",
                    dayOfWeek = schedule.dayOfWeek,
                    startTimeFormatted = TimeUtils.formatMinutesToTime(schedule.startTimeMinutes),
                    endTimeFormatted = TimeUtils.formatMinutesToTime(schedule.endTimeMinutes)
                )
            }
        }
    }
}

class ObserveSchedulesForStudentUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val studentRepository: StudentRepository,
    private val subjectRepository: StudentSubjectRepository
) {
    operator fun invoke(studentId: String): Flow<List<ScheduleWithDetails>> {
        return scheduleRepository.observeSchedulesForStudent(studentId).map { schedules ->
            val student = studentRepository.getStudentById(studentId)
            val subjects = subjectRepository.getSubjectsForStudent(studentId)
            val subjectMap = subjects.associateBy { it.id }

            schedules.map { schedule ->
                ScheduleWithDetails(
                    schedule = schedule,
                    studentName = student?.name ?: "Student",
                    studentPhone = student?.phone,
                    subjectName = subjectMap[schedule.subjectId]?.subjectName ?: "Tuition",
                    dayOfWeek = schedule.dayOfWeek,
                    startTimeFormatted = TimeUtils.formatMinutesToTime(schedule.startTimeMinutes),
                    endTimeFormatted = TimeUtils.formatMinutesToTime(schedule.endTimeMinutes)
                )
            }
        }
    }
}

class GetScheduleByIdUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(id: String): Schedule? {
        return scheduleRepository.getScheduleById(id)
    }
}

// ----------------- Class Session Use Cases -----------------

class GenerateExpectedSessionsForDateUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val classSessionRepository: ClassSessionRepository
) {
    suspend operator fun invoke(dateEpochMs: Long): AppResult<List<ClassSession>> {
        return try {
            val normalizedDate = TimeUtils.getStartOfDayEpochMs(dateEpochMs)
            val dayOfWeek = TimeUtils.getDayOfWeek(normalizedDate)

            val activeSchedules = scheduleRepository.getActiveSchedulesForDay(dayOfWeek)
            val now = System.currentTimeMillis()

            val generatedOrExistingSessions = mutableListOf<ClassSession>()

            for (schedule in activeSchedules) {
                // Check if session already exists for this schedule on this date
                val existing = classSessionRepository.getSessionsForScheduleAndDate(schedule.id, normalizedDate)
                if (existing.isNotEmpty()) {
                    generatedOrExistingSessions.addAll(existing)
                } else {
                    // Create new deterministic session
                    val newSession = ClassSession(
                        id = UUID.randomUUID().toString(),
                        studentId = schedule.studentId,
                        scheduleId = schedule.id,
                        subjectId = schedule.subjectId,
                        sessionDate = normalizedDate,
                        scheduledStartTime = schedule.startTimeMinutes,
                        scheduledEndTime = schedule.endTimeMinutes,
                        actualStartTime = null,
                        actualEndTime = null,
                        status = SessionStatus.SCHEDULED,
                        remarks = null,
                        topicCovered = null,
                        createdAt = now,
                        updatedAt = now
                    )
                    classSessionRepository.addSession(newSession)
                    generatedOrExistingSessions.add(newSession)
                }
            }

            // Also include any one-off or rescheduled sessions that exist for this date
            val allForDate = classSessionRepository.getSessionsForDate(normalizedDate)
            val allDistinct = (generatedOrExistingSessions + allForDate).distinctBy { it.id }

            AppResult.Success(allDistinct.sortedBy { it.scheduledStartTime })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to generate sessions")
        }
    }
}

class ObserveSessionsForDateUseCase @Inject constructor(
    private val classSessionRepository: ClassSessionRepository,
    private val studentRepository: StudentRepository,
    private val subjectRepository: StudentSubjectRepository,
    private val diaryDao: StudentDiaryDao,
    private val rescheduleRepository: RescheduleRepository
) {
    operator fun invoke(dateEpochMs: Long): Flow<List<ClassSessionWithDetails>> {
        val normalizedDate = TimeUtils.getStartOfDayEpochMs(dateEpochMs)

        return combine(
            classSessionRepository.observeSessionsForDate(normalizedDate),
            studentRepository.observeAllStudents()
        ) { sessions, students ->
            val studentMap = students.associateBy { it.id }

            sessions.map { session ->
                val student = studentMap[session.studentId]
                val subjects = subjectRepository.getSubjectsForStudent(session.studentId)
                val subject = subjects.firstOrNull { it.id == session.subjectId }
                val diaries = diaryDao.getDiaryEntriesForSession(session.id)
                val reschedule = rescheduleRepository.getByOriginalSession(session.id)

                val dayOfWeek = TimeUtils.getDayOfWeek(session.sessionDate)
                val dayName = RoutineDay.fromDayOfWeek(dayOfWeek).displayName
                val dateFormatted = TimeUtils.formatLocalDate(session.sessionDate, "dd MMM yyyy")
                val timeFormatted = "${TimeUtils.formatMinutesToTime(session.scheduledStartTime)} - ${TimeUtils.formatMinutesToTime(session.scheduledEndTime)}"

                ClassSessionWithDetails(
                    session = session,
                    studentName = student?.name ?: "Unknown Student",
                    studentPhone = student?.phone,
                    subjectName = subject?.subjectName ?: "Tuition",
                    timeFormatted = timeFormatted,
                    dayFormatted = dayName,
                    dateFormatted = dateFormatted,
                    hasDiaryEntry = diaries.isNotEmpty(),
                    diaryEntryId = diaries.firstOrNull()?.id,
                    rescheduleReason = reschedule?.reason
                )
            }.sortedBy { it.session.scheduledStartTime }
        }
    }
}

class ObserveSessionsForStudentUseCase @Inject constructor(
    private val classSessionRepository: ClassSessionRepository,
    private val studentRepository: StudentRepository,
    private val subjectRepository: StudentSubjectRepository,
    private val diaryDao: StudentDiaryDao,
    private val rescheduleRepository: RescheduleRepository
) {
    operator fun invoke(studentId: String): Flow<List<ClassSessionWithDetails>> {
        return classSessionRepository.observeSessionsForStudent(studentId).map { sessions ->
            val student = studentRepository.getStudentById(studentId)
            val subjects = subjectRepository.getSubjectsForStudent(studentId)
            val subjectMap = subjects.associateBy { it.id }

            sessions.map { session ->
                val diaries = diaryDao.getDiaryEntriesForSession(session.id)
                val reschedule = rescheduleRepository.getByOriginalSession(session.id)

                val dayOfWeek = TimeUtils.getDayOfWeek(session.sessionDate)
                val dayName = RoutineDay.fromDayOfWeek(dayOfWeek).displayName
                val dateFormatted = TimeUtils.formatLocalDate(session.sessionDate, "dd MMM yyyy")
                val timeFormatted = "${TimeUtils.formatMinutesToTime(session.scheduledStartTime)} - ${TimeUtils.formatMinutesToTime(session.scheduledEndTime)}"

                ClassSessionWithDetails(
                    session = session,
                    studentName = student?.name ?: "Student",
                    studentPhone = student?.phone,
                    subjectName = subjectMap[session.subjectId]?.subjectName ?: "Tuition",
                    timeFormatted = timeFormatted,
                    dayFormatted = dayName,
                    dateFormatted = dateFormatted,
                    hasDiaryEntry = diaries.isNotEmpty(),
                    diaryEntryId = diaries.firstOrNull()?.id,
                    rescheduleReason = reschedule?.reason
                )
            }
        }
    }
}

class GetSessionByIdUseCase @Inject constructor(
    private val classSessionRepository: ClassSessionRepository,
    private val studentRepository: StudentRepository,
    private val subjectRepository: StudentSubjectRepository,
    private val diaryDao: StudentDiaryDao,
    private val rescheduleRepository: RescheduleRepository
) {
    suspend operator fun invoke(id: String): ClassSessionWithDetails? {
        val session = classSessionRepository.getSessionById(id) ?: return null
        val student = studentRepository.getStudentById(session.studentId)
        val subjects = subjectRepository.getSubjectsForStudent(session.studentId)
        val subject = subjects.firstOrNull { it.id == session.subjectId }
        val diaries = diaryDao.getDiaryEntriesForSession(session.id)
        val reschedule = rescheduleRepository.getByOriginalSession(session.id)

        val dayOfWeek = TimeUtils.getDayOfWeek(session.sessionDate)
        val dayName = RoutineDay.fromDayOfWeek(dayOfWeek).displayName
        val dateFormatted = TimeUtils.formatLocalDate(session.sessionDate, "dd MMM yyyy")
        val timeFormatted = "${TimeUtils.formatMinutesToTime(session.scheduledStartTime)} - ${TimeUtils.formatMinutesToTime(session.scheduledEndTime)}"

        return ClassSessionWithDetails(
            session = session,
            studentName = student?.name ?: "Unknown Student",
            studentPhone = student?.phone,
            subjectName = subject?.subjectName ?: "Tuition",
            timeFormatted = timeFormatted,
            dayFormatted = dayName,
            dateFormatted = dateFormatted,
            hasDiaryEntry = diaries.isNotEmpty(),
            diaryEntryId = diaries.firstOrNull()?.id,
            rescheduleReason = reschedule?.reason
        )
    }
}

class UpdateSessionStatusUseCase @Inject constructor(
    private val classSessionRepository: ClassSessionRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        status: SessionStatus,
        remarks: String? = null,
        actualStart: Int? = null,
        actualEnd: Int? = null,
        topicCovered: String? = null
    ): AppResult<Unit> {
        return try {
            val session = classSessionRepository.getSessionById(sessionId)
                ?: return AppResult.Error("Class session not found")

            val now = System.currentTimeMillis()
            val resolvedActualStart = actualStart ?: if (status == SessionStatus.COMPLETED && session.actualStartTime == null) session.scheduledStartTime else session.actualStartTime
            val resolvedActualEnd = actualEnd ?: if (status == SessionStatus.COMPLETED && session.actualEndTime == null) session.scheduledEndTime else session.actualEndTime

            classSessionRepository.updateStatus(
                id = sessionId,
                status = status,
                remarks = remarks ?: session.remarks,
                actualStart = resolvedActualStart,
                actualEnd = resolvedActualEnd
            )

            if (!topicCovered.isNullOrBlank()) {
                classSessionRepository.updateTopicCovered(sessionId, topicCovered)
            }

            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update session status")
        }
    }
}

class RescheduleSessionUseCase @Inject constructor(
    private val classSessionRepository: ClassSessionRepository,
    private val rescheduleRepository: RescheduleRepository,
    private val checkScheduleConflictUseCase: CheckScheduleConflictUseCase
) {
    suspend operator fun invoke(
        originalSessionId: String,
        targetDateEpochMs: Long,
        newStartTimeMinutes: Int,
        newEndTimeMinutes: Int,
        reason: String?
    ): AppResult<ClassSession> {
        val validation = RescheduleValidator.validate(
            originalSessionId = originalSessionId,
            newDateEpochMs = targetDateEpochMs,
            newStartTimeMinutes = newStartTimeMinutes,
            newEndTimeMinutes = newEndTimeMinutes,
            reason = reason
        )
        if (!validation.isValid) {
            return AppResult.Error((validation as ValidationResult.Invalid).errorMessage)
        }

        val originalSession = classSessionRepository.getSessionById(originalSessionId)
            ?: return AppResult.Error("Original class session not found")

        val targetNormalizedDate = TimeUtils.getStartOfDayEpochMs(targetDateEpochMs)
        val targetDayOfWeek = TimeUtils.getDayOfWeek(targetNormalizedDate)

        // Check for schedule conflict on target slot
        val conflictCheck = checkScheduleConflictUseCase(
            dayOfWeek = targetDayOfWeek,
            startTimeMinutes = newStartTimeMinutes,
            endTimeMinutes = newEndTimeMinutes
        )
        if (conflictCheck.hasConflict) {
            val first = conflictCheck.conflicts.first()
            return AppResult.Error(
                "Conflict on new time slot with ${first.studentName} (${first.subjectName}) on ${first.dayName} at ${first.timeFormatted}"
            )
        }

        return try {
            val now = System.currentTimeMillis()
            val newSessionId = UUID.randomUUID().toString()

            // 1. Mark original session as RESCHEDULED
            classSessionRepository.updateStatus(
                id = originalSessionId,
                status = SessionStatus.RESCHEDULED,
                remarks = reason ?: "Rescheduled to ${TimeUtils.formatLocalDate(targetNormalizedDate)}"
            )

            // 2. Create the new session
            val newSession = ClassSession(
                id = newSessionId,
                studentId = originalSession.studentId,
                scheduleId = originalSession.scheduleId,
                subjectId = originalSession.subjectId,
                sessionDate = targetNormalizedDate,
                scheduledStartTime = newStartTimeMinutes,
                scheduledEndTime = newEndTimeMinutes,
                actualStartTime = null,
                actualEndTime = null,
                status = SessionStatus.SCHEDULED,
                remarks = "Rescheduled from ${TimeUtils.formatLocalDate(originalSession.sessionDate)}: ${reason ?: "No reason provided"}",
                topicCovered = originalSession.topicCovered,
                createdAt = now,
                updatedAt = now
            )
            classSessionRepository.addSession(newSession)

            // 3. Create RescheduleRecord audit entity
            val rescheduleRecord = RescheduleRecord(
                id = UUID.randomUUID().toString(),
                originalSessionId = originalSessionId,
                newSessionId = newSessionId,
                rescheduledBy = "TUTOR",
                reason = reason,
                requestedAt = now,
                createdAt = now,
                updatedAt = now
            )
            rescheduleRepository.addRescheduleRecord(rescheduleRecord)

            AppResult.Success(newSession)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to reschedule session")
        }
    }
}

class UpdateSessionTopicUseCase @Inject constructor(
    private val classSessionRepository: ClassSessionRepository
) {
    suspend operator fun invoke(sessionId: String, topic: String): AppResult<Unit> {
        return try {
            classSessionRepository.updateTopicCovered(sessionId, topic)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update topic")
        }
    }
}

