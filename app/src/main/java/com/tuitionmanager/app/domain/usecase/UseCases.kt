package com.tuitionmanager.app.domain.usecase

import com.tuitionmanager.app.data.local.dao.ClassSessionDao
import com.tuitionmanager.app.data.local.dao.ExamResultDao
import com.tuitionmanager.app.data.local.dao.StudentDiaryDao
import com.tuitionmanager.app.domain.engine.ExamEvaluationEngine
import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.repository.*
import com.tuitionmanager.app.domain.validation.*
import com.tuitionmanager.app.utils.AppResult
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
