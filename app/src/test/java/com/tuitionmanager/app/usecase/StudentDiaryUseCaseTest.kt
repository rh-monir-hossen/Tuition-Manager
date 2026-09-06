package com.tuitionmanager.app.usecase

import com.tuitionmanager.app.domain.model.*
import com.tuitionmanager.app.domain.repository.StudentDiaryRepository
import com.tuitionmanager.app.domain.repository.StudentRepository
import com.tuitionmanager.app.domain.repository.StudentSubjectRepository
import com.tuitionmanager.app.domain.usecase.*
import com.tuitionmanager.app.utils.AppResult
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class StudentDiaryUseCaseTest {

    private val studentList = mutableListOf<Student>()
    private val subjectList = mutableListOf<StudentSubject>()
    private val diaryList = mutableListOf<StudentDiary>()

    private val fakeStudentRepo = object : StudentRepository {
        override suspend fun insert(student: Student) { studentList.add(student) }
        override suspend fun update(student: Student) {
            val idx = studentList.indexOfFirst { it.id == student.id }
            if (idx >= 0) studentList[idx] = student
        }
        override suspend fun delete(studentId: String) {
            val idx = studentList.indexOfFirst { it.id == studentId }
            if (idx >= 0) studentList[idx] = studentList[idx].copy(isDeleted = true, deletedAt = System.currentTimeMillis())
        }
        override suspend fun restore(studentId: String) {
            val idx = studentList.indexOfFirst { it.id == studentId }
            if (idx >= 0) studentList[idx] = studentList[idx].copy(isDeleted = false, deletedAt = null)
        }
        override suspend fun updateActiveStatus(studentId: String, isActive: Boolean) {
            val idx = studentList.indexOfFirst { it.id == studentId }
            if (idx >= 0) studentList[idx] = studentList[idx].copy(isActive = isActive)
        }
        override suspend fun getById(id: String): Student? = studentList.find { it.id == id }
        override fun observeAll(): kotlinx.coroutines.flow.Flow<List<Student>> = flowOf(studentList)
        override fun observeActive(): kotlinx.coroutines.flow.Flow<List<Student>> =
            flowOf(studentList.filter { it.isActive && !it.isDeleted })
        override fun observeAllWithDeleted(): kotlinx.coroutines.flow.Flow<List<Student>> = flowOf(studentList)
        override fun observeById(id: String): kotlinx.coroutines.flow.Flow<Student?> =
            flowOf(studentList.find { it.id == id })
        override fun search(query: String, includeInactive: Boolean): kotlinx.coroutines.flow.Flow<List<Student>> =
            flowOf(studentList.filter { it.name.contains(query, ignoreCase = true) })
    }

    private val fakeSubjectRepo = object : StudentSubjectRepository {
        override suspend fun insert(subject: StudentSubject) { subjectList.add(subject) }
        override suspend fun insertAll(subjects: List<StudentSubject>) { subjectList.addAll(subjects) }
        override suspend fun delete(subjectId: String) { subjectList.removeAll { it.id == subjectId } }
        override fun observeByStudentId(studentId: String): kotlinx.coroutines.flow.Flow<List<StudentSubject>> =
            flowOf(subjectList.filter { it.studentId == studentId })
        override suspend fun getByStudentId(studentId: String): List<StudentSubject> =
            subjectList.filter { it.studentId == studentId }
    }

    private val fakeDiaryRepo = object : StudentDiaryRepository {
        override suspend fun insert(diary: StudentDiary) { diaryList.add(diary) }
        override suspend fun update(diary: StudentDiary) {
            val idx = diaryList.indexOfFirst { it.id == diary.id }
            if (idx >= 0) diaryList[idx] = diary
        }
        override suspend fun delete(diaryId: String) {
            val idx = diaryList.indexOfFirst { it.id == diaryId }
            if (idx >= 0) diaryList[idx] = diaryList[idx].copy(isDeleted = true, deletedAt = System.currentTimeMillis())
        }
        override suspend fun restore(diaryId: String) {
            val idx = diaryList.indexOfFirst { it.id == diaryId }
            if (idx >= 0) diaryList[idx] = diaryList[idx].copy(isDeleted = false, deletedAt = null)
        }
        override suspend fun getById(id: String): StudentDiary? = diaryList.find { it.id == id }
        override fun observeById(id: String): kotlinx.coroutines.flow.Flow<StudentDiary?> =
            flowOf(diaryList.find { it.id == id })
        override fun observeHistory(studentId: String?): kotlinx.coroutines.flow.Flow<List<StudentDiary>> =
            flowOf(diaryList.filter { (studentId == null || it.studentId == studentId) && !it.isDeleted })
        override fun search(studentId: String?, query: String): kotlinx.coroutines.flow.Flow<List<StudentDiary>> =
            flowOf(diaryList.filter { it.topicTitle.contains(query, ignoreCase = true) && !it.isDeleted })
        override fun filter(studentId: String?, subjectId: String?, fromDate: Long, toDate: Long): kotlinx.coroutines.flow.Flow<List<StudentDiary>> =
            flowOf(diaryList.filter { !it.isDeleted })
    }

    private lateinit var addStudentUseCase: AddStudentUseCase
    private lateinit var addDiaryEntryUseCase: AddDiaryEntryUseCase
    private lateinit var deleteStudentUseCase: DeleteStudentUseCase

    @Before
    fun setup() {
        studentList.clear()
        subjectList.clear()
        diaryList.clear()
        addStudentUseCase = AddStudentUseCase(fakeStudentRepo, fakeSubjectRepo)
        addDiaryEntryUseCase = AddDiaryEntryUseCase(fakeDiaryRepo)
        deleteStudentUseCase = DeleteStudentUseCase(fakeStudentRepo)
    }

    @Test
    fun testAddStudentWithInitialSubjects() = runTest {
        val student = Student(
            id = "s-1",
            name = "Test Student",
            institution = "Test School",
            classGrade = "10",
            monthlyFeeAmount = 5000.0,
            billingCycleDay = 1,
            isActive = true,
            joinedDate = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val result = addStudentUseCase(student, listOf("Math", "Physics"))
        assertTrue(result is AppResult.Success)
        assertEquals(1, studentList.size)
        assertEquals(2, subjectList.size)
        assertEquals("Math", subjectList[0].subjectName)
        assertEquals("Physics", subjectList[1].subjectName)
    }

    @Test
    fun testAddStudentValidationFailure() = runTest {
        val student = Student(
            id = "s-2",
            name = "", // Blank name is invalid
            monthlyFeeAmount = 0.0,
            billingCycleDay = 1,
            isActive = true,
            joinedDate = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val result = addStudentUseCase(student)
        assertTrue(result is AppResult.Error)
        assertEquals(0, studentList.size)
    }

    @Test
    fun testAddDiaryEntry() = runTest {
        val diary = StudentDiary(
            id = "d-1",
            studentId = "s-1",
            subjectId = "sub-1",
            date = System.currentTimeMillis(),
            topicTitle = "Linear Equations",
            whatWasTaught = "Explained graphing and solving systems",
            homeworkStatus = HomeworkStatus.ASSIGNED,
            studentUnderstanding = StudentUnderstanding.EXCELLENT,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val result = addDiaryEntryUseCase(diary)
        assertTrue(result is AppResult.Success)
        assertEquals(1, diaryList.size)
        assertEquals("Linear Equations", diaryList[0].topicTitle)
    }

    @Test
    fun testSoftDeleteStudent() = runTest {
        val student = Student(
            id = "s-1",
            name = "John Doe",
            monthlyFeeAmount = 4000.0,
            billingCycleDay = 1,
            isActive = true,
            joinedDate = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        fakeStudentRepo.insert(student)

        deleteStudentUseCase("s-1")
        val deleted = fakeStudentRepo.getById("s-1")
        assertNotNull(deleted)
        assertTrue(deleted!!.isDeleted)
        assertNotNull(deleted.deletedAt)
    }
}
