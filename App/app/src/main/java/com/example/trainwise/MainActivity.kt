package com.example.trainwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trainwise.ui.screens.HomeScreen
import com.example.trainwise.ui.screens.LoginScreen
import com.example.trainwise.ui.screens.SignUpScreen
import com.example.trainwise.ui.screens.WorkoutsScreen
import com.example.trainwise.ui.theme.TrainWiseTheme
import com.example.trainwise.ui.screens.ProfileScreen
import com.example.trainwise.ui.screens.GuideScreen
import com.example.trainwise.ui.screens.BiometricDevicesScreen
import com.example.trainwise.ui.screens.SecurityScreen
import com.example.trainwise.ui.screens.NotificationScreen
import com.example.trainwise.ui.screens.AccountDetailsScreen
import com.example.trainwise.ui.screens.TrainingHistoryScreen
import com.example.trainwise.ui.screens.CreateWorkoutScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(true) }
            
            TrainWiseTheme(darkTheme = isDarkMode) {
                AppNavigation(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { isDarkMode = !isDarkMode }
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
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
                onNavigateToWorkouts = { navController.navigate("workouts") },
                onNavigateToGuide = { navController.navigate("guide") },
                onNavigateToProfile = { navController.navigate("profile") }
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
    }
}
