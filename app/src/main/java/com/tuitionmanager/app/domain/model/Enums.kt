package com.tuitionmanager.app.domain.model

enum class HomeworkStatus {
    NONE,
    ASSIGNED,
    SUBMITTED_COMPLETE,
    SUBMITTED_PARTIAL,
    NOT_SUBMITTED
}

enum class StudentUnderstanding {
    EXCELLENT,
    GOOD,
    AVERAGE,
    NEEDS_ATTENTION
}

enum class ExamStatus {
    PLANNED,
    COMPLETED,
    MISSED,
    CANCELLED,
    RESCHEDULED
}

enum class ExamType {
    WEEKLY_QUIZ,
    CHAPTER_TEST,
    MONTHLY_ASSESSMENT,
    MID_TERM,
    FINAL_MODEL_TEST,
    SURPRISE_TEST
}

enum class SessionStatus {
    SCHEDULED,
    COMPLETED,
    MISSED,
    CANCELLED,
    CANCELLED_BY_STUDENT,
    CANCELLED_BY_TUTOR,
    RESCHEDULED
}

enum class RoutineDay(val dayOfWeek: Int, val displayName: String, val shortName: String) {
    SATURDAY(6, "Saturday", "Sat"),
    SUNDAY(7, "Sunday", "Sun"),
    MONDAY(1, "Monday", "Mon"),
    TUESDAY(2, "Tuesday", "Tue"),
    WEDNESDAY(3, "Wednesday", "Wed"),
    THURSDAY(4, "Thursday", "Thu"),
    FRIDAY(5, "Friday", "Fri");

    companion object {
        fun fromDayOfWeek(dayOfWeek: Int): RoutineDay {
            return values().firstOrNull { it.dayOfWeek == dayOfWeek } ?: SATURDAY
        }

        val ORDERED_DAYS = listOf(
            SATURDAY,
            SUNDAY,
            MONDAY,
            TUESDAY,
            WEDNESDAY,
            THURSDAY,
            FRIDAY
        )
    }
}


enum class GuardianPreferredChannel {
    WHATSAPP,
    SMS,
    MESSENGER,
    EMAIL,
    OTHER
}

enum class GuardianReportLanguage {
    EN,
    BN
}

enum class GuardianReportFormat {
    TEXT_SUMMARY,
    DETAILED_PDF
}
