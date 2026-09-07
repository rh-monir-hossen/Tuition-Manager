package com.tuitionmanager.app.ui.navigation

sealed class Screen(val route: String) {
    object StudentList : Screen("students")
    object AddStudent : Screen("student/add")
    object EditStudent : Screen("student/edit/{studentId}") {
        fun createRoute(studentId: String) = "student/edit/$studentId"
    }
    object StudentProfile : Screen("student/{studentId}") {
        fun createRoute(studentId: String) = "student/$studentId"
    }
    object StudentDiaryHistory : Screen("student/{studentId}/diary") {
        fun createRoute(studentId: String) = "student/$studentId/diary"
    }
    object AddDiaryEntry : Screen("diary/add?studentId={studentId}&classSessionId={classSessionId}") {
        fun createRoute(studentId: String? = null, classSessionId: String? = null): String {
            val sParam = studentId ?: ""
            val cParam = classSessionId ?: ""
            return "diary/add?studentId=$sParam&classSessionId=$cParam"
        }
    }
    object DiaryDetail : Screen("diary/{diaryId}") {
        fun createRoute(diaryId: String) = "diary/$diaryId"
    }
    object EditDiaryEntry : Screen("diary/{diaryId}/edit") {
        fun createRoute(diaryId: String) = "diary/$diaryId/edit"
    }

    // STEP 5: Routine & Agenda Screens
    object WeeklyRoutine : Screen("routine")

    object AddSchedule : Screen("schedule/add?studentId={studentId}") {
        fun createRoute(studentId: String? = null): String {
            val sParam = studentId ?: ""
            return "schedule/add?studentId=$sParam"
        }
    }

    object EditSchedule : Screen("schedule/edit/{scheduleId}") {
        fun createRoute(scheduleId: String) = "schedule/edit/$scheduleId"
    }

    object DailyAgenda : Screen("agenda?dateEpochMs={dateEpochMs}") {
        fun createRoute(dateEpochMs: Long? = null): String {
            val dParam = dateEpochMs?.toString() ?: ""
            return "agenda?dateEpochMs=$dParam"
        }
    }

    object ClassSessionDetail : Screen("session/{sessionId}") {
        fun createRoute(sessionId: String) = "session/$sessionId"
    }

    object ClassHistory : Screen("class_history?studentId={studentId}") {
        fun createRoute(studentId: String? = null): String {
            val sParam = studentId ?: ""
            return "class_history?studentId=$sParam"
        }
    }
}


