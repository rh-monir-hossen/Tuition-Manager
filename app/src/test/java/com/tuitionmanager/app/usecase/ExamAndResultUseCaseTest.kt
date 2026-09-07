package com.tuitionmanager.app.usecase

import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.repository.*
import com.tuitionmanager.app.domain.usecase.*
import com.tuitionmanager.app.utils.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class ExamAndResultUseCaseTest {

    private val examList = mutableListOf<Exam>()
    private val resultList = mutableListOf<ExamResult>()
    private val studentList = mutableListOf<Student>()
    private val subjectList = mutableListOf<StudentSubject>()

    private val fakeExamRepo = object : ExamRepository {
        override suspend fun insertExam(exam: Exam) { examList.add(exam) }
        override suspend fun updateExam(exam: Exam) {
            val idx = examList.indexOfFirst { it.id == exam.id }
            if (idx >= 0) examList[idx] = exam
        }
        override suspend fun softDeleteExam(id: String) {
            val idx = examList.indexOfFirst { it.id == id }
            if (idx >= 0) examList[idx] = examList[idx].copy(isDeleted = true, updatedAt = System.currentTimeMillis())
        }
        override suspend fun restoreExam(id: String) {
            val idx = examList.indexOfFirst { it.id == id }
            if (idx >= 0) examList[idx] = examList[idx].copy(isDeleted = false, updatedAt = System.currentTimeMillis())
        }
        override suspend fun getExamById(id: String): Exam? = examList.find { it.id == id }
        override fun observeExamById(id: String): Flow<Exam?> = flowOf(examList.find { it.id == id })
        override fun observeAllExams(): Flow<List<Exam>> =
            flowOf(examList.filter { !it.isDeleted })
        override fun observeExamsForStudent(studentId: String): Flow<List<Exam>> =
            flowOf(examList.filter { it.studentId == studentId && !it.isDeleted })
        override fun observeExamsForSubject(subjectId: String): Flow<List<Exam>> =
            flowOf(examList.filter { it.subjectId == subjectId && !it.isDeleted })
        override fun observeExamsByStatus(status: ExamStatus): Flow<List<Exam>> =
            flowOf(examList.filter { it.status == status && !it.isDeleted })
        override fun observeExamsByType(examType: ExamType): Flow<List<Exam>> =
            flowOf(examList.filter { it.examType == examType && !it.isDeleted })
        override fun observeDeletedExams(): Flow<List<Exam>> =
            flowOf(examList.filter { it.isDeleted })
        override fun observeExamsWithDetails(): Flow<List<ExamWithDetails>> =
            flowOf(
                examList.filter { !it.isDeleted }.map { exam ->
                    ExamWithDetails(
                        exam = exam,
                        studentName = studentList.find { it.id == exam.studentId }?.name ?: "Unknown Student",
                        subjectName = subjectList.find { it.id == exam.subjectId }?.subjectName ?: "Unknown Subject",
                        result = resultList.find { it.examId == exam.id }
                    )
                }
            )
        override fun observeExamsWithDetailsForStudent(studentId: String): Flow<List<ExamWithDetails>> =
            flowOf(
                examList.filter { it.studentId == studentId && !it.isDeleted }.map { exam ->
                    ExamWithDetails(
                        exam = exam,
                        studentName = studentList.find { it.id == exam.studentId }?.name ?: "Unknown Student",
                        subjectName = subjectList.find { it.id == exam.subjectId }?.subjectName ?: "Unknown Subject",
                        result = resultList.find { it.examId == exam.id }
                    )
                }
            )
    }

    private val fakeResultRepo = object : ExamResultRepository {
        override suspend fun insertResult(result: ExamResult) { resultList.add(result) }
        override suspend fun updateResult(result: ExamResult) {
            val idx = resultList.indexOfFirst { it.id == result.id }
            if (idx >= 0) resultList[idx] = result
        }
        override suspend fun deleteResult(id: String) {
            resultList.removeAll { it.id == id }
        }
        override suspend fun getResultById(id: String): ExamResult? = resultList.find { it.id == id }
        override suspend fun getResultForExam(examId: String): ExamResult? = resultList.find { it.examId == examId }
        override fun observeResultForExam(examId: String): Flow<ExamResult?> =
            flowOf(resultList.find { it.examId == examId })
    }

    private lateinit var planExamUseCase: PlanExamUseCase
    private lateinit var updateExamUseCase: UpdateExamUseCase
    private lateinit var deleteExamUseCase: DeleteExamUseCase
    private lateinit var restoreExamUseCase: RestoreExamUseCase
    private lateinit var recordExamResultUseCase: RecordExamResultUseCase
    private lateinit var updateExamResultUseCase: UpdateExamResultUseCase
    private lateinit var observeExamsUseCase: ObserveExamsUseCase
    private lateinit var observeExamsForStudentUseCase: ObserveExamsForStudentUseCase

    @Before
    fun setUp() {
        examList.clear()
        resultList.clear()
        studentList.clear()
        subjectList.clear()

        studentList.add(Student(id = "s-1", name = "Tanvir Hasan", phone = "01711111111"))
        subjectList.add(StudentSubject(id = "sub-1", studentId = "s-1", subjectName = "Physics"))

        planExamUseCase = PlanExamUseCase(fakeExamRepo)
        updateExamUseCase = UpdateExamUseCase(fakeExamRepo)
        deleteExamUseCase = DeleteExamUseCase(fakeExamRepo)
        restoreExamUseCase = RestoreExamUseCase(fakeExamRepo)
        recordExamResultUseCase = RecordExamResultUseCase(fakeExamRepo, fakeResultRepo)
        updateExamResultUseCase = UpdateExamResultUseCase(fakeExamRepo, fakeResultRepo)
        observeExamsUseCase = ObserveExamsUseCase(fakeExamRepo)
        observeExamsForStudentUseCase = ObserveExamsForStudentUseCase(fakeExamRepo)
    }

    @Test
    fun testPlanExamSuccessful() = runTest {
        val result = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "Mechanics Chapter Test",
            syllabusTopic = "Newton's Laws of Motion",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 60,
            totalMarks = 50.0,
            passingMarks = 20.0,
            examType = ExamType.CHAPTER_TEST,
            notes = "Bring calculator"
        )

        assertTrue(result is AppResult.Success)
        val exam = (result as AppResult.Success).data
        assertEquals("Mechanics Chapter Test", exam.title)
        assertEquals(50.0, exam.totalMarks, 0.001)
        assertEquals(ExamStatus.PLANNED, exam.status)
        assertEquals(1, examList.size)
        assertFalse(exam.isDeleted)
    }

    @Test
    fun testPlanExamValidationRejectsInvalidTotalMarks() = runTest {
        val result = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "Test",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 60,
            totalMarks = 0.0 // Invalid
        )

        assertTrue(result is AppResult.Error)
        assertEquals(0, examList.size)
    }

    @Test
    fun testPlanExamValidationRejectsPassingMarksExceedingTotalMarks() = runTest {
        val result = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "Test",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 60,
            totalMarks = 50.0,
            passingMarks = 60.0 // Invalid: > totalMarks
        )

        assertTrue(result is AppResult.Error)
        assertEquals(0, examList.size)
    }

    @Test
    fun testPlanExamValidationRejectsEmptyTitle() = runTest {
        val result = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "   ",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 60,
            totalMarks = 50.0
        )

        assertTrue(result is AppResult.Error)
    }

    @Test
    fun testUpdateExamDetails() = runTest {
        val planRes = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "Initial Title",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 60,
            totalMarks = 50.0
        )
        val exam = (planRes as AppResult.Success).data

        val updatedExam = exam.copy(
            title = "Updated Title",
            totalMarks = 75.0,
            status = ExamStatus.COMPLETED
        )
        val updateRes = updateExamUseCase(updatedExam)

        assertTrue(updateRes is AppResult.Success)
        val retrieved = fakeExamRepo.getExamById(exam.id)
        assertNotNull(retrieved)
        assertEquals("Updated Title", retrieved?.title)
        assertEquals(75.0, retrieved?.totalMarks ?: 0.0, 0.001)
        assertEquals(ExamStatus.COMPLETED, retrieved?.status)
    }

    @Test
    fun testSoftDeleteAndRestoreExam() = runTest {
        val planRes = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "Exam to delete",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 60,
            totalMarks = 50.0
        )
        val exam = (planRes as AppResult.Success).data

        // Soft Delete
        deleteExamUseCase(exam.id)
        val afterDelete = fakeExamRepo.getExamById(exam.id)
        assertTrue(afterDelete?.isDeleted == true)

        // Verify it is excluded from active flow
        val activeExams = fakeExamRepo.observeAllExams().first()
        assertTrue(activeExams.none { it.id == exam.id })

        // Restore
        restoreExamUseCase(exam.id)
        val afterRestore = fakeExamRepo.getExamById(exam.id)
        assertFalse(afterRestore?.isDeleted == true)

        val restoredActiveExams = fakeExamRepo.observeAllExams().first()
        assertTrue(restoredActiveExams.any { it.id == exam.id })
    }

    @Test
    fun testRecordExamResultPass() = runTest {
        val planRes = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "Midterm Exam",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 60,
            totalMarks = 100.0,
            passingMarks = 40.0
        )
        val exam = (planRes as AppResult.Success).data

        val recordRes = recordExamResultUseCase(
            examId = exam.id,
            actualExamDate = 1725148800000L,
            marksObtained = 82.5,
            studentStrengths = "Strong grasp of mechanics",
            studentWeaknesses = "Minor arithmetic slips in question 3",
            recommendations = "Practice timed speed drills",
            teacherRemarks = "Excellent performance"
        )

        assertTrue(recordRes is AppResult.Success)
        val result = (recordRes as AppResult.Success).data
        assertEquals(82.5, result.marksObtained, 0.001)
        assertTrue(result.isPassed)
        assertEquals(1, resultList.size)

        // Exam status should automatically become COMPLETED
        val updatedExam = fakeExamRepo.getExamById(exam.id)
        assertEquals(ExamStatus.COMPLETED, updatedExam?.status)
    }

    @Test
    fun testRecordExamResultFail() = runTest {
        val planRes = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "Weekly Quiz",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 30,
            totalMarks = 20.0,
            passingMarks = 10.0
        )
        val exam = (planRes as AppResult.Success).data

        val recordRes = recordExamResultUseCase(
            examId = exam.id,
            actualExamDate = 1725148800000L,
            marksObtained = 8.0,
            teacherRemarks = "Needs more revision"
        )

        assertTrue(recordRes is AppResult.Success)
        val result = (recordRes as AppResult.Success).data
        assertEquals(8.0, result.marksObtained, 0.001)
        assertFalse(result.isPassed)
    }

    @Test
    fun testRecordExamResultRejectsMarksExceedingTotalMarks() = runTest {
        val planRes = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "Test",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 60,
            totalMarks = 50.0
        )
        val exam = (planRes as AppResult.Success).data

        val recordRes = recordExamResultUseCase(
            examId = exam.id,
            actualExamDate = 1725148800000L,
            marksObtained = 55.0 // Invalid: > 50
        )

        assertTrue(recordRes is AppResult.Error)
        assertEquals(0, resultList.size)
    }

    @Test
    fun testRecordExamResultRejectsNegativeMarks() = runTest {
        val planRes = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "Test",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 60,
            totalMarks = 50.0
        )
        val exam = (planRes as AppResult.Success).data

        val recordRes = recordExamResultUseCase(
            examId = exam.id,
            actualExamDate = 1725148800000L,
            marksObtained = -5.0 // Invalid
        )

        assertTrue(recordRes is AppResult.Error)
        assertEquals(0, resultList.size)
    }

    @Test
    fun testCannotRecordDuplicateResultForSameExam() = runTest {
        val planRes = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "Single Result Exam",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 60,
            totalMarks = 50.0
        )
        val exam = (planRes as AppResult.Success).data

        // First record
        val firstRes = recordExamResultUseCase(
            examId = exam.id,
            actualExamDate = 1725148800000L,
            marksObtained = 40.0
        )
        assertTrue(firstRes is AppResult.Success)

        // Second record should fail because a result already exists (must use Update)
        val secondRes = recordExamResultUseCase(
            examId = exam.id,
            actualExamDate = 1725148800000L,
            marksObtained = 45.0
        )
        assertTrue(secondRes is AppResult.Error)
        assertEquals(1, resultList.size)
    }

    @Test
    fun testUpdateExamResult() = runTest {
        val planRes = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "Correction Exam",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 60,
            totalMarks = 50.0,
            passingMarks = 20.0
        )
        val exam = (planRes as AppResult.Success).data

        val recordRes = recordExamResultUseCase(
            examId = exam.id,
            actualExamDate = 1725148800000L,
            marksObtained = 18.0 // initially failed
        )
        val originalResult = (recordRes as AppResult.Success).data
        assertFalse(originalResult.isPassed)

        // Teacher re-checks paper: marks corrected to 24.0 (now passed)
        val updated = originalResult.copy(marksObtained = 24.0)
        val updateRes = updateExamResultUseCase(updated)
        assertTrue(updateRes is AppResult.Success)

        val retrievedResult = fakeResultRepo.getResultById(originalResult.id)
        assertNotNull(retrievedResult)
        assertEquals(24.0, retrievedResult?.marksObtained ?: 0.0, 0.001)
        assertTrue(retrievedResult?.isPassed == true)
    }

    @Test
    fun testExamWithDetailsCalculatesPercentage() = runTest {
        val planRes = planExamUseCase(
            studentId = "s-1",
            subjectId = "sub-1",
            title = "Final Physics",
            plannedDate = 1725148800000L,
            startTimeMinutes = 600,
            durationMinutes = 120,
            totalMarks = 80.0
        )
        val exam = (planRes as AppResult.Success).data

        recordExamResultUseCase(
            examId = exam.id,
            actualExamDate = 1725148800000L,
            marksObtained = 64.0
        )

        val examsWithDetails = fakeExamRepo.observeExamsWithDetailsForStudent("s-1").first()
        assertEquals(1, examsWithDetails.size)
        val item = examsWithDetails.first()

        assertEquals("Tanvir Hasan", item.studentName)
        assertEquals("Physics", item.subjectName)
        assertNotNull(item.result)
        assertEquals(80.0, item.percentage ?: 0.0, 0.001)
        assertTrue(item.isPassed == true)
        assertEquals("A+ (Outstanding)", item.gradeLevel)
    }
}
