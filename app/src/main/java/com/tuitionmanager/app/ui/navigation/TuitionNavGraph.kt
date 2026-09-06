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
import com.tuitionmanager.app.ui.diary.*
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
    }
}
