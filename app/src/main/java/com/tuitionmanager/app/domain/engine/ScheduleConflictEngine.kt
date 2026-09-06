package com.tuitionmanager.app.domain.engine

import com.tuitionmanager.app.domain.model.Schedule

data class ConflictResult(
    val hasConflict: Boolean,
    val conflictingSchedules: List<Schedule>
)

class ScheduleConflictEngine {

    /**
     * Checks if a proposed schedule slot conflicts with any existing schedules.
     * Overlap Rule: newStart < existingEnd AND newEnd > existingStart (on the same dayOfWeek).
     */
    fun checkConflict(
        proposedDayOfWeek: Int,
        proposedStartMinutes: Int,
        proposedEndMinutes: Int,
        existingSchedules: List<Schedule>,
        ignoreScheduleId: String? = null
    ): ConflictResult {
        require(proposedStartMinutes < proposedEndMinutes) {
            "Start time ($proposedStartMinutes) must be strictly before end time ($proposedEndMinutes)"
        }

        val conflicts = existingSchedules.filter { existing ->
            if (existing.id == ignoreScheduleId || !existing.isActive || existing.isDeleted) {
                false
            } else if (existing.dayOfWeek != proposedDayOfWeek) {
                false
            } else {
                // Overlap condition
                proposedStartMinutes < existing.endTimeMinutes && proposedEndMinutes > existing.startTimeMinutes
            }
        }

        return ConflictResult(
            hasConflict = conflicts.isNotEmpty(),
            conflictingSchedules = conflicts
        )
    }
}
