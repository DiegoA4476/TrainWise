package com.example.trainwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
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



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrainWiseTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
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
                onNavigateProfile = {navController.navigate("profile") }
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToWorkouts = { navController.navigate("workouts") },
                onNavigateToGuide = { navController.navigate("guide") }
            )
        }
        composable("guide") {
            GuideScreen(
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToWorkouts = { navController.navigate("workouts") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }
    }
}
