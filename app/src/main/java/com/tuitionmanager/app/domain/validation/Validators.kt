package com.tuitionmanager.app.domain.validation

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errorMessage: String) : ValidationResult() {
        val isValid: Boolean get() = false
    }

    val isValid: Boolean get() = this is Valid
}

object StudentValidator {
    fun validate(name: String): ValidationResult {
        if (name.trim().isEmpty()) {
            return ValidationResult.Invalid("Student name cannot be empty")
        }
        return ValidationResult.Valid
    }
}

object DiaryValidator {
    fun validate(
        studentId: String,
        subjectId: String,
        topicTitle: String,
        whatWasTaught: String
    ): ValidationResult {
        if (studentId.isBlank()) return ValidationResult.Invalid("Student reference is required")
        if (subjectId.isBlank()) return ValidationResult.Invalid("Subject reference is required")
        if (topicTitle.trim().isEmpty()) return ValidationResult.Invalid("Topic title cannot be empty")
        if (whatWasTaught.trim().isEmpty()) return ValidationResult.Invalid("Content of what was taught is required")
        return ValidationResult.Valid
    }
}

object ExamValidator {
    fun validate(
        studentId: String,
        subjectId: String,
        title: String,
        totalMarks: Double,
        durationMinutes: Int,
        startTimeMinutes: Int,
        passingMarks: Double? = null,
        plannedDate: Long = 1L
    ): ValidationResult {
        if (studentId.isBlank()) return ValidationResult.Invalid("Student reference is required")
        if (subjectId.isBlank()) return ValidationResult.Invalid("Subject reference is required")
        if (title.trim().isEmpty()) return ValidationResult.Invalid("Exam title cannot be empty")
        if (totalMarks <= 0.0) return ValidationResult.Invalid("Total marks must be greater than 0")
        if (passingMarks != null && (passingMarks < 0.0 || passingMarks > totalMarks)) {
            return ValidationResult.Invalid("Passing marks must be between 0 and total marks ($totalMarks)")
        }
        if (durationMinutes <= 0) return ValidationResult.Invalid("Duration must be greater than 0 minutes")
        if (startTimeMinutes !in 0..1439) return ValidationResult.Invalid("Start time minutes must be between 0 and 1439")
        if (plannedDate <= 0L) return ValidationResult.Invalid("Valid planned date is required")
        return ValidationResult.Valid
    }
}

object ExamResultValidator {
    fun validate(
        marksObtained: Double,
        totalMarks: Double,
        actualExamDate: Long
    ): ValidationResult {
        if (marksObtained < 0.0) return ValidationResult.Invalid("Marks obtained cannot be negative")
        if (marksObtained > totalMarks) return ValidationResult.Invalid("Marks obtained ($marksObtained) cannot exceed total marks ($totalMarks)")
        if (actualExamDate <= 0L) return ValidationResult.Invalid("Valid actual exam date is required")
        return ValidationResult.Valid
    }
}

object ScheduleValidator {
    fun validate(
        studentId: String,
        subjectId: String,
        dayOfWeek: Int,
        startTimeMinutes: Int,
        endTimeMinutes: Int
    ): ValidationResult {
        if (studentId.isBlank()) return ValidationResult.Invalid("Please select a student")
        if (subjectId.isBlank()) return ValidationResult.Invalid("Please select a subject")
        if (dayOfWeek !in 1..7) return ValidationResult.Invalid("Valid day of week (1 to 7) is required")
        if (startTimeMinutes !in 0..1439) return ValidationResult.Invalid("Start time must be within a valid 24-hour range")
        if (endTimeMinutes !in 0..1439) return ValidationResult.Invalid("End time must be within a valid 24-hour range")
        if (startTimeMinutes >= endTimeMinutes) return ValidationResult.Invalid("Start time must be strictly before end time")
        return ValidationResult.Valid
    }
}

object RescheduleValidator {
    fun validate(
        originalSessionId: String,
        newDateEpochMs: Long,
        newStartTimeMinutes: Int,
        newEndTimeMinutes: Int,
        reason: String?
    ): ValidationResult {
        if (originalSessionId.isBlank()) return ValidationResult.Invalid("Original session reference is required")
        if (newDateEpochMs <= 0L) return ValidationResult.Invalid("Valid target reschedule date is required")
        if (newStartTimeMinutes !in 0..1439) return ValidationResult.Invalid("Start time must be within a valid 24-hour range")
        if (newEndTimeMinutes !in 0..1439) return ValidationResult.Invalid("End time must be within a valid 24-hour range")
        if (newStartTimeMinutes >= newEndTimeMinutes) return ValidationResult.Invalid("Start time must be strictly before end time")
        return ValidationResult.Valid
    }
}

