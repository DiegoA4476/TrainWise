package com.example.trainwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.trainwise.ui.navigation.AppNavigation
import com.example.trainwise.ui.theme.TrainWiseTheme
import com.example.trainwise.ui.viewmodels.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(true) }

            // ViewModels scoped to Activity
            val authViewModel: AuthViewModel = viewModel()
            val userViewModel: UserViewModel = viewModel()
            val mapViewModel: MapViewModel = viewModel()
            val biometricViewModel: BiometricViewModel = viewModel()
            val workoutViewModel: WorkoutViewModel = viewModel()

            val navController = rememberNavController()

            TrainWiseTheme(darkTheme = isDarkMode) {
                AppNavigation(
                    navController = navController,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { isDarkMode = !isDarkMode },
                    authViewModel = authViewModel,
                    userViewModel = userViewModel,
                    mapViewModel = mapViewModel,
                    biometricViewModel = biometricViewModel,
                    workoutViewModel = workoutViewModel
                )
            }
        }
    }
}
