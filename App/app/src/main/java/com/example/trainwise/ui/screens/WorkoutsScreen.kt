package com.example.trainwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainwise.ui.theme.*
import com.example.trainwise.data.models.Workout
import com.example.trainwise.ui.viewmodels.WorkoutViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Delete

@Composable
fun WorkoutsScreen(
    onNavigateHome: () -> Unit,
    onNavigateGuide: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateToCreateWorkout: () -> Unit,
    onNavigateToActiveWorkout: (String) -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    val categories = listOf("All", "Strength", "Cardio", "Yoga", "HIIT", "Flexibility")
    var selectedCategory by remember { mutableStateOf("All") }

    val workouts by viewModel.workouts
    val isLoading by viewModel.isLoading

    val filteredWorkouts = if (selectedCategory == "All") {
        workouts
    } else {
        workouts.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = { WorkoutsTopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateWorkout,
                containerColor = Orange,
                contentColor = White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Workout")
            }
        },
        bottomBar = {
            WorkoutsBottomNavigationBar(
                onHomeClick = onNavigateHome,
                onGuideClick = onNavigateGuide,
                onProfileClick = onNavigateProfile
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Selector de Categorías
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(categories) { category ->
                            CategoryChip(
                                category = category,
                                isSelected = selectedCategory == category,
                                onSelect = { selectedCategory = category }
                            )
                        }
                    }
                }

                if (workouts.isEmpty()) {
                    item {
                        EmptyStateView(onNavigateToCreateWorkout)
                    }
                } else {
                    item {
                        Text(
                            text = if (selectedCategory == "All") "My Routines" else "$selectedCategory Plan",
                            color = White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        )
                    }

                    items(filteredWorkouts) { workout ->
                        WorkoutListItem(
                            workout = workout,
                            onDelete = {
                                viewModel.deleteWorkout(workout.id)
                            },
                            onClick = {
                                onNavigateToActiveWorkout(workout.id)
                            },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsTopBar() {
    TopAppBar(
        title = {
            Text(
                "Workouts",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = White
            )
        },
        actions = {
            IconButton(onClick = { /* Search */ }) {
                Icon(Icons.Default.Search, "Search", tint = White)
            }
            IconButton(onClick = { /* Notifications */ }) {
                Icon(Icons.Outlined.Notifications, "Notifications", tint = White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkBackground
        )
    )
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onSelect() },
        color = if (isSelected) Orange else CardBackground,
        shape = RoundedCornerShape(24.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, GrayText.copy(alpha = 0.3f))
    ) {
        Text(
            text = category,
            color = if (isSelected) White else GrayText,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun WorkoutListItem(
    workout: Workout,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = CardBackground,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.FitnessCenter, null, tint = Orange, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = workout.title,
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${workout.exercises.size} Exercises • ${workout.category}",
                        color = GrayText,
                        fontSize = 13.sp
                    )
                }
            }

            // Botón de Eliminar
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Workout",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No workouts yet", color = GrayText, fontSize = 16.sp)
        TextButton(onClick = onAction) {
            Text("Create your first one", color = Orange)
        }
    }
}

@Composable
fun WorkoutsBottomNavigationBar(
    onHomeClick: () -> Unit,
    onGuideClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.Black,
        contentColor = White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = { Icon(Icons.Outlined.Home, null) },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = White,
                unselectedTextColor = White,
                indicatorColor = Orange
            )
        )
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Outlined.FitnessCenter, null) },
            label = { Text("Workouts") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = White,
                selectedTextColor = White,
                indicatorColor = Orange,
                unselectedIconColor = White,
                unselectedTextColor = White
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onGuideClick,
            icon = { Icon(Icons.Outlined.MenuBook, null) },
            label = { Text("Guide") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = White,
                unselectedTextColor = White,
                indicatorColor = Orange
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Outlined.Person, null) },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = White,
                unselectedTextColor = White,
                indicatorColor = Orange
            )
        )
    }
}
