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
    CANCELLED_BY_STUDENT,
    CANCELLED_BY_TUTOR,
    RESCHEDULED
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
