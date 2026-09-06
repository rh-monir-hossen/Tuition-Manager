package com.tuitionmanager.app.engine

import com.tuitionmanager.app.domain.engine.ScheduleConflictEngine
import com.tuitionmanager.app.domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScheduleConflictEngineTest {

    private lateinit var engine: ScheduleConflictEngine

    @Before
    fun setUp() {
        engine = ScheduleConflictEngine()
    }

    private fun createSchedule(
        id: String,
        dayOfWeek: Int,
        startMinutes: Int,
        endMinutes: Int,
        isActive: Boolean = true
    ): Schedule {
        return Schedule(
            id = id,
            studentId = "student-1",
            subjectId = "subject-1",
            dayOfWeek = dayOfWeek,
            startTimeMinutes = startMinutes,
            endTimeMinutes = endMinutes,
            effectiveStartDate = 1725148800000L,
            effectiveEndDate = null,
            isActive = isActive,
            createdAt = 1725148800000L,
            updatedAt = 1725148800000L
        )
    }

    @Test
    fun testNoConflictDifferentDays() {
        val existing = listOf(
            createSchedule("s1", dayOfWeek = 1, startMinutes = 600, endMinutes = 660) // Mon 10:00 - 11:00
        )
        val result = engine.checkConflict(
            proposedDayOfWeek = 2, // Tuesday
            proposedStartMinutes = 600,
            proposedEndMinutes = 660,
            existingSchedules = existing
        )
        assertFalse(result.hasConflict)
    }

    @Test
    fun testOverlapSameDayDetectsConflict() {
        val existing = listOf(
            createSchedule("s1", dayOfWeek = 1, startMinutes = 600, endMinutes = 660) // Mon 10:00 - 11:00
        )
        // Overlap: 10:30 - 11:30
        val result = engine.checkConflict(
            proposedDayOfWeek = 1,
            proposedStartMinutes = 630,
            proposedEndMinutes = 690,
            existingSchedules = existing
        )
        assertTrue(result.hasConflict)
        assertEquals(1, result.conflictingSchedules.size)
        assertEquals("s1", result.conflictingSchedules[0].id)
    }

    @Test
    fun testAdjacentBackToBackSlotsDoNotConflict() {
        val existing = listOf(
            createSchedule("s1", dayOfWeek = 1, startMinutes = 600, endMinutes = 660) // 10:00 - 11:00
        )
        // Exactly back to back: 11:00 - 12:00
        val result = engine.checkConflict(
            proposedDayOfWeek = 1,
            proposedStartMinutes = 660,
            proposedEndMinutes = 720,
            existingSchedules = existing
        )
        assertFalse(result.hasConflict)
    }

    @Test
    fun testInactiveOrDeletedScheduleIgnored() {
        val existing = listOf(
            createSchedule("s1", dayOfWeek = 1, startMinutes = 600, endMinutes = 660, isActive = false)
        )
        val result = engine.checkConflict(
            proposedDayOfWeek = 1,
            proposedStartMinutes = 600,
            proposedEndMinutes = 660,
            existingSchedules = existing
        )
        assertFalse(result.hasConflict)
    }
}
