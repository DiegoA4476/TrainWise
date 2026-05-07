package com.example.trainwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.trainwise.ui.screens.*
import com.example.trainwise.ui.theme.TrainWiseTheme
import com.example.trainwise.ui.viewmodels.MapViewModel
import com.google.firebase.auth.FirebaseAuth

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
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) "home" else "login"
    
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = startDestination) {
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
                onNavigateToCreateWorkout = { navController.navigate("create_workout") },
                onNavigateToActiveWorkout = { workoutId -> 
                    navController.navigate("active_workout/$workoutId")
                }
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
                onNavigateToTrainingHistory = { navController.navigate("training_history") },
                onSignOut = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
        composable(
            route = "active_workout/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId") ?: return@composable
            ActiveWorkoutScreen(
                workoutId = workoutId,
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
