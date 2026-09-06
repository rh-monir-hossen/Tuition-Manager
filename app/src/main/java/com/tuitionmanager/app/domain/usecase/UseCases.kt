package com.tuitionmanager.app.domain.usecase

import com.tuitionmanager.app.domain.engine.ExamEvaluationEngine
import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.repository.*
import com.tuitionmanager.app.domain.validation.*
import com.tuitionmanager.app.utils.AppResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// ----------------- Student Use Cases -----------------

class AddStudentUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    suspend operator fun invoke(student: Student): AppResult<Unit> {
        val validation = StudentValidator.validate(student.name)
        if (!validation.isValid) {
            return AppResult.Error((validation as ValidationResult.Invalid).errorMessage)
        }
        return try {
            studentRepository.addStudent(student)
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

class ObserveStudentsUseCase @Inject constructor(
    private val studentRepository: StudentRepository
) {
    operator fun invoke(): Flow<List<Student>> {
        return studentRepository.observeActiveStudents()
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

class SearchDiaryUseCase @Inject constructor(
    private val diaryRepository: StudentDiaryRepository
) {
    operator fun invoke(studentId: String, query: String): Flow<List<StudentDiary>> {
        return diaryRepository.searchDiaryEntries(studentId, query)
    }
}

class FilterDiaryHistoryUseCase @Inject constructor(
    private val diaryRepository: StudentDiaryRepository
) {
    operator fun invoke(
        studentId: String,
        subjectId: String?,
        startDateEpochMs: Long,
        endDateEpochMs: Long
    ): Flow<List<StudentDiary>> {
        return diaryRepository.filterDiaryEntries(studentId, subjectId, startDateEpochMs, endDateEpochMs)
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
