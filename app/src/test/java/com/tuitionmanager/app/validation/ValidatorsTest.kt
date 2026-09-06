package com.tuitionmanager.app.validation

import com.tuitionmanager.app.domain.validation.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun testStudentValidator() {
        assertFalse(StudentValidator.validate("").isValid)
        assertFalse(StudentValidator.validate("   ").isValid)
        assertTrue(StudentValidator.validate("Rahim Ahmed").isValid)
    }

    @Test
    fun testDiaryValidator() {
        assertFalse(
            DiaryValidator.validate(
                studentId = "",
                subjectId = "sub-1",
                topicTitle = "Trigonometry",
                whatWasTaught = "Formulas"
            ).isValid
        )
        assertFalse(
            DiaryValidator.validate(
                studentId = "s-1",
                subjectId = "sub-1",
                topicTitle = "",
                whatWasTaught = "Formulas"
            ).isValid
        )
        assertTrue(
            DiaryValidator.validate(
                studentId = "s-1",
                subjectId = "sub-1",
                topicTitle = "Quadratic Equations",
                whatWasTaught = "Factorization Method"
            ).isValid
        )
    }

    @Test
    fun testExamValidator() {
        assertFalse(
            ExamValidator.validate(
                studentId = "s-1",
                subjectId = "sub-1",
                title = "Chapter 4 Test",
                totalMarks = 0.0, // Invalid
                durationMinutes = 60,
                startTimeMinutes = 600
            ).isValid
        )
        assertFalse(
            ExamValidator.validate(
                studentId = "s-1",
                subjectId = "sub-1",
                title = "Chapter 4 Test",
                totalMarks = 50.0,
                durationMinutes = -10, // Invalid
                startTimeMinutes = 600
            ).isValid
        )
        assertTrue(
            ExamValidator.validate(
                studentId = "s-1",
                subjectId = "sub-1",
                title = "Chapter 4 Test",
                totalMarks = 50.0,
                durationMinutes = 60,
                startTimeMinutes = 600
            ).isValid
        )
    }

    @Test
    fun testExamResultValidator() {
        assertFalse(
            ExamResultValidator.validate(
                marksObtained = -1.0,
                totalMarks = 50.0,
                actualExamDate = 1725148800000L
            ).isValid
        )
        assertFalse(
            ExamResultValidator.validate(
                marksObtained = 55.0,
                totalMarks = 50.0,
                actualExamDate = 1725148800000L
            ).isValid
        )
        assertTrue(
            ExamResultValidator.validate(
                marksObtained = 42.0,
                totalMarks = 50.0,
                actualExamDate = 1725148800000L
            ).isValid
        )
    }
}
