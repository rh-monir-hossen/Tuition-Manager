package com.tuitionmanager.app.engine

import com.tuitionmanager.app.domain.engine.ExamEvaluationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExamEvaluationEngineTest {

    private lateinit var engine: ExamEvaluationEngine

    @Before
    fun setUp() {
        engine = ExamEvaluationEngine()
    }

    @Test
    fun testPercentageCalculationExact() {
        val pct = engine.calculatePercentage(marksObtained = 42.0, totalMarks = 50.0)
        assertEquals(84.0, pct, 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPercentageMarksExceedTotalThrows() {
        engine.calculatePercentage(marksObtained = 55.0, totalMarks = 50.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testPercentageNegativeMarksThrows() {
        engine.calculatePercentage(marksObtained = -5.0, totalMarks = 50.0)
    }

    @Test
    fun testGradeLevels() {
        assertEquals("A+ (Outstanding)", engine.determineGradeLevel(85.0))
        assertEquals("A (Excellent)", engine.determineGradeLevel(72.5))
        assertEquals("A- (Very Good)", engine.determineGradeLevel(64.0))
        assertEquals("B (Satisfactory)", engine.determineGradeLevel(53.0))
        assertEquals("C (Pass)", engine.determineGradeLevel(40.0))
        assertEquals("F (Needs Attention)", engine.determineGradeLevel(39.9))
    }

    @Test
    fun testPassDeterminationWithExplicitPassingMarks() {
        assertTrue(engine.determineIsPassed(marksObtained = 25.0, totalMarks = 50.0, passingMarks = 20.0))
        assertFalse(engine.determineIsPassed(marksObtained = 18.0, totalMarks = 50.0, passingMarks = 20.0))
    }
}
