package com.example.trainwise.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.trainwise.ui.screens.*
import com.example.trainwise.ui.viewmodels.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    mapViewModel: MapViewModel,
    biometricViewModel: BiometricViewModel,
    workoutViewModel: WorkoutViewModel
) {
    val startDestination = if (authViewModel.currentUser != null) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate("signup") },
                onLoginSuccess = { 
                    userViewModel.loadUserProfile()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }
        composable("signup") {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }
        composable("home") {
            HomeScreen(
                viewModel = mapViewModel,
                onNavigateToWorkouts = { navController.navigate("workouts") },
                onNavigateToGuide = { navController.navigate("guide") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToGymDetails = { navController.navigate("gym_details") }
            )
        }
        composable("workouts") {
            WorkoutsScreen(
                onNavigateHome = { navController.navigate("home") },
                onNavigateGuide = { navController.navigate("guide") },
                onNavigateProfile = { navController.navigate("profile") },
                onNavigateToCreateWorkout = { navController.navigate("create_workout") },
                onNavigateToActiveWorkout = { workoutId -> 
                    navController.navigate("active_workout/$workoutId")
                },
                viewModel = workoutViewModel
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToWorkouts = { navController.navigate("workouts") },
                onNavigateToGuide = { navController.navigate("guide") },
                onNavigateToAccountDetails = { navController.navigate("account_details") },
                onNavigateToBiometrics = { navController.navigate("biometrics") },
                onNavigateToSecurity = { navController.navigate("security") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToTrainingHistory = { navController.navigate("workout_history") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = userViewModel,
                workoutViewModel = workoutViewModel
            )
        }
        composable("guide") {
            GuideScreen(
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToWorkouts = { navController.navigate("workouts") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }
        composable("account_details") {
            AccountDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = userViewModel
            )
        }
        composable("biometrics") {
            BiometricDevicesScreen(
                viewModel = biometricViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("security") {
            SecurityScreen(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("notifications") {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("workout_history") {
            WorkoutHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate("workout_detail/$id") },
                viewModel = workoutViewModel
            )
        }
        composable(
            route = "workout_detail/{completedWorkoutId}",
            arguments = listOf(navArgument("completedWorkoutId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("completedWorkoutId") ?: return@composable
            WorkoutDetailScreen(
                completedWorkoutId = id,
                onNavigateBack = { navController.popBackStack() },
                viewModel = workoutViewModel
            )
        }
        composable("create_workout") {
            CreateWorkoutScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = workoutViewModel
            )
        }
        composable(
            route = "active_workout/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId") ?: return@composable
            ActiveWorkoutScreen(
                workoutId = workoutId,
                biometricViewModel = biometricViewModel,
                workoutViewModel = workoutViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("gym_details") {
            val selectedGym = mapViewModel.selectedGym
            if (selectedGym != null) {
                GymDetailsScreen(
                    gym = selectedGym,
                    viewModel = mapViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
