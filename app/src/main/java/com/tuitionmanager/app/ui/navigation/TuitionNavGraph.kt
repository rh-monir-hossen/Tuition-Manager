package com.tuitionmanager.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tuitionmanager.app.ui.agenda.*
import com.tuitionmanager.app.ui.diary.*
import com.tuitionmanager.app.ui.exam.*
import com.tuitionmanager.app.ui.schedule.*
import com.tuitionmanager.app.ui.student.*

@Composable
fun TuitionNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.StudentList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 1. Student List Screen
        composable(route = Screen.StudentList.route) {
            val viewModel: StudentListViewModel = hiltViewModel()
            StudentListScreen(
                viewModel = viewModel,
                onStudentClick = { studentId ->
                    navController.navigate(Screen.StudentProfile.createRoute(studentId))
                },
                onAddStudentClick = {
                    navController.navigate(Screen.AddStudent.route)
                },
                onAddDiaryClick = { studentId ->
                    navController.navigate(Screen.AddDiaryEntry.createRoute(studentId = studentId))
                },
                onViewDiaryHistoryClick = { studentId ->
                    navController.navigate(Screen.StudentDiaryHistory.createRoute(studentId))
                },
                onRoutineClick = {
                    navController.navigate(Screen.WeeklyRoutine.route)
                },
                onAgendaClick = {
                    navController.navigate(Screen.DailyAgenda.createRoute())
                },
                onClassHistoryClick = {
                    navController.navigate(Screen.ClassHistory.createRoute())
                },
                onExamsClick = {
                    navController.navigate(Screen.ExamList.createRoute())
                }
            )
        }

        // 2. Add Student
        composable(route = Screen.AddStudent.route) {
            val viewModel: StudentFormViewModel = hiltViewModel()
            StudentFormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. Edit Student
        composable(
            route = Screen.EditStudent.route,
            arguments = listOf(
                navArgument("studentId") { type = NavType.StringType }
            )
        ) {
            val viewModel: StudentFormViewModel = hiltViewModel()
            StudentFormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. Student Profile Screen
        composable(
            route = Screen.StudentProfile.route,
            arguments = listOf(
                navArgument("studentId") { type = NavType.StringType }
            )
        ) {
            val viewModel: StudentProfileViewModel = hiltViewModel()
            StudentProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditStudent = { studentId ->
                    navController.navigate(Screen.EditStudent.createRoute(studentId))
                },
                onAddDiary = { studentId ->
                    navController.navigate(Screen.AddDiaryEntry.createRoute(studentId = studentId))
                },
                onDiaryClick = { diaryId ->
                    navController.navigate(Screen.DiaryDetail.createRoute(diaryId))
                },
                onViewAllDiaries = { studentId ->
                    navController.navigate(Screen.StudentDiaryHistory.createRoute(studentId))
                },
                onAddSchedule = { studentId ->
                    navController.navigate(Screen.AddSchedule.createRoute(studentId = studentId))
                },
                onEditSchedule = { scheduleId ->
                    navController.navigate(Screen.EditSchedule.createRoute(scheduleId = scheduleId))
                },
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.ClassSessionDetail.createRoute(sessionId = sessionId))
                },
                onViewAllSessions = { studentId ->
                    navController.navigate(Screen.ClassHistory.createRoute(studentId = studentId))
                },
                onAddExam = { studentId ->
                    navController.navigate(Screen.AddExam.createRoute(studentId = studentId))
                },
                onExamClick = { examId ->
                    navController.navigate(Screen.ExamDetail.createRoute(examId = examId))
                },
                onEnterResultClick = { examId ->
                    navController.navigate(Screen.EnterExamResult.createRoute(examId = examId))
                },
                onViewAllExams = { studentId ->
                    navController.navigate(Screen.ExamList.createRoute(studentId = studentId))
                }
            )
        }

        // 5. Student Diary History
        composable(
            route = Screen.StudentDiaryHistory.route,
            arguments = listOf(
                navArgument("studentId") { type = NavType.StringType }
            )
        ) {
            val viewModel: StudentDiaryViewModel = hiltViewModel()
            StudentDiaryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddDiaryClick = { studentId ->
                    navController.navigate(Screen.AddDiaryEntry.createRoute(studentId = studentId))
                },
                onDiaryClick = { diaryId ->
                    navController.navigate(Screen.DiaryDetail.createRoute(diaryId))
                }
            )
        }

        // 6. Add Diary Entry
        composable(
            route = Screen.AddDiaryEntry.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("classSessionId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            val viewModel: DiaryEntryViewModel = hiltViewModel()
            DiaryEntryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. Diary Detail
        composable(
            route = Screen.DiaryDetail.route,
            arguments = listOf(
                navArgument("diaryId") { type = NavType.StringType }
            )
        ) {
            val viewModel: DiaryDetailViewModel = hiltViewModel()
            DiaryDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { diaryId ->
                    navController.navigate(Screen.EditDiaryEntry.createRoute(diaryId))
                }
            )
        }

        // 8. Edit Diary Entry
        composable(
            route = Screen.EditDiaryEntry.route,
            arguments = listOf(
                navArgument("diaryId") { type = NavType.StringType }
            )
        ) {
            val viewModel: DiaryEntryViewModel = hiltViewModel()
            DiaryEntryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 9. Weekly Routine Screen (STEP 5)
        composable(route = Screen.WeeklyRoutine.route) {
            val viewModel: RoutineViewModel = hiltViewModel()
            RoutineScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddScheduleClick = {
                    navController.navigate(Screen.AddSchedule.createRoute())
                },
                onEditScheduleClick = { scheduleId ->
                    navController.navigate(Screen.EditSchedule.createRoute(scheduleId))
                },
                onStudentClick = { studentId ->
                    navController.navigate(Screen.StudentProfile.createRoute(studentId))
                }
            )
        }

        // 10. Add Schedule Slot (STEP 5)
        composable(
            route = Screen.AddSchedule.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            val viewModel: ScheduleFormViewModel = hiltViewModel()
            ScheduleFormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 11. Edit Schedule Slot (STEP 5)
        composable(
            route = Screen.EditSchedule.route,
            arguments = listOf(
                navArgument("scheduleId") { type = NavType.StringType }
            )
        ) {
            val viewModel: ScheduleFormViewModel = hiltViewModel()
            ScheduleFormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 12. Daily Agenda (STEP 5)
        composable(
            route = Screen.DailyAgenda.route,
            arguments = listOf(
                navArgument("dateEpochMs") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            val viewModel: DailyAgendaViewModel = hiltViewModel()
            DailyAgendaScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.ClassSessionDetail.createRoute(sessionId))
                },
                onStudentClick = { studentId ->
                    navController.navigate(Screen.StudentProfile.createRoute(studentId))
                },
                onCreateDiary = { studentId, classSessionId ->
                    navController.navigate(
                        Screen.AddDiaryEntry.createRoute(
                            studentId = studentId,
                            classSessionId = classSessionId
                        )
                    )
                },
                onViewDiary = { diaryId ->
                    navController.navigate(Screen.DiaryDetail.createRoute(diaryId))
                }
            )
        }

        // 13. Class Session Detail (STEP 5)
        composable(
            route = Screen.ClassSessionDetail.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) {
            val viewModel: ClassSessionDetailViewModel = hiltViewModel()
            ClassSessionDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onStudentClick = { studentId ->
                    navController.navigate(Screen.StudentProfile.createRoute(studentId))
                },
                onCreateDiaryClick = { studentId, classSessionId ->
                    navController.navigate(
                        Screen.AddDiaryEntry.createRoute(
                            studentId = studentId,
                            classSessionId = classSessionId
                        )
                    )
                },
                onViewDiaryClick = { diaryId ->
                    navController.navigate(Screen.DiaryDetail.createRoute(diaryId))
                }
            )
        }

        // 14. Class History (STEP 5)
        composable(
            route = Screen.ClassHistory.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            val viewModel: ClassHistoryViewModel = hiltViewModel()
            ClassHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.ClassSessionDetail.createRoute(sessionId))
                },
                onNavigateToDiaryEntry = { studentId, subjectId, classSessionId ->
                    navController.navigate(
                        Screen.AddDiaryEntry.createRoute(
                            studentId = studentId,
                            classSessionId = classSessionId
                        )
                    )
                },
                onNavigateToDiaryDetail = { diaryId ->
                    navController.navigate(Screen.DiaryDetail.createRoute(diaryId))
                }
            )
        }

        // 15. Exam List Screen (STEP 6)
        composable(
            route = Screen.ExamList.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            ExamListScreen(
                onNavigateBack = { navController.popBackStack() },
                onExamClick = { examId ->
                    navController.navigate(Screen.ExamDetail.createRoute(examId))
                },
                onAddExamClick = { studentId ->
                    navController.navigate(Screen.AddExam.createRoute(studentId))
                },
                onEnterResultClick = { examId ->
                    navController.navigate(Screen.EnterExamResult.createRoute(examId))
                }
            )
        }

        // 16. Add Exam Screen (STEP 6)
        composable(
            route = Screen.AddExam.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) {
            ExamFormScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 17. Edit Exam Screen (STEP 6)
        composable(
            route = Screen.EditExam.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType }
            )
        ) {
            ExamFormScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 18. Exam Detail Screen (STEP 6)
        composable(
            route = Screen.ExamDetail.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType }
            )
        ) {
            ExamDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditExamClick = { examId ->
                    navController.navigate(Screen.EditExam.createRoute(examId))
                },
                onEnterResultClick = { examId ->
                    navController.navigate(Screen.EnterExamResult.createRoute(examId))
                },
                onEditResultClick = { examId ->
                    navController.navigate(Screen.EnterExamResult.createRoute(examId))
                },
                onStudentClick = { studentId ->
                    navController.navigate(Screen.StudentProfile.createRoute(studentId))
                }
            )
        }

        // 19. Enter / Edit Exam Result Screen (STEP 6)
        composable(
            route = Screen.EnterExamResult.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType }
            )
        ) {
            ExamResultFormScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
