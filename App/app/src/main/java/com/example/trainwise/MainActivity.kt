package com.example.trainwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trainwise.ui.screens.*
import com.example.trainwise.ui.theme.TrainWiseTheme
import com.example.trainwise.ui.viewmodels.MapViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(true) }
            val mapViewModel: MapViewModel = viewModel()
            
            TrainWiseTheme(darkTheme = isDarkMode) {
                AppNavigation(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { isDarkMode = !isDarkMode },
                    mapViewModel = mapViewModel
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    mapViewModel: MapViewModel
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate("signup") },
                onLoginSuccess = { 
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("signup") {
            SignUpScreen(onNavigateToLogin = { navController.popBackStack() })
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
                onNavigateGuide = {  navController.navigate("guide") },
                onNavigateProfile = {navController.navigate("profile") },
                onNavigateToCreateWorkout = { navController.navigate("create_workout") }
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
                onNavigateToTrainingHistory = { navController.navigate("training_history") }
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
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("biometrics") {
            BiometricDevicesScreen(
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
        composable("training_history") {
            TrainingHistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("create_workout") {
            CreateWorkoutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("gym_details") {
            val selectedGym = mapViewModel.selectedGym
            if (selectedGym != null) {
                GymDetailsScreen(
                    gym = selectedGym,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
