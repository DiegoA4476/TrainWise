package com.example.trainwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainwise.ui.theme.*

data class Workout(
    val title: String,
    val exercises: Int,
    val duration: Int,
    val difficulty: String,
    val category: String,
    val progress: Float = 0f
)

@Composable
fun WorkoutsScreen(
    onNavigateHome: () -> Unit,
    onNavigateGuide: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val categories = listOf("All", "Strength", "Cardio", "Yoga", "HIIT")
    var selectedCategory by remember { mutableStateOf("All") }

    val allWorkouts = listOf(
        Workout("Morning Full Body", 12, 45, "Intermediate", "Strength", 0.6f),
        Workout("Upper Body Power", 8, 30, "Advanced", "Strength"),
        Workout("Quick HIIT Blast", 6, 15, "Beginner", "HIIT", 0.2f),
        Workout("Core Stability", 10, 20, "Intermediate", "Yoga"),
        Workout("Evening Run Prep", 5, 10, "Beginner", "Cardio")
    )

    val filteredWorkouts = if (selectedCategory == "All") {
        allWorkouts
    } else {
        allWorkouts.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = { WorkoutsTopBar() },
        bottomBar = {
            WorkoutsBottomNavigationBar(
                onHomeClick = onNavigateHome,
                onGuideClick = onNavigateGuide,
                onProfileClick = onNavigateProfile
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Create Workout */ },
                containerColor = Orange,
                contentColor = White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Workout")
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Category Selector
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
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

            // Featured Workout Card
            item {
                FeaturedWorkoutCard(
                    workout = allWorkouts[0],
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            // Section Title
            item {
                Text(
                    text = "Browse $selectedCategory",
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            // Workout List
            items(filteredWorkouts) { workout ->
                WorkoutListItem(
                    workout = workout,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
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
fun FeaturedWorkoutCard(workout: Workout, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Orange, Orange.copy(alpha = 0.7f))
                )
            )
            .clickable { /* Start Workout */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Featured",
                        color = White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(Icons.Outlined.Timer, null, tint = White, modifier = Modifier.size(24.dp))
            }

            Column {
                Text(
                    text = workout.title,
                    color = White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${workout.exercises} Exercises • ${workout.duration} min • ${workout.difficulty}",
                    color = White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun WorkoutListItem(workout: Workout, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* Workout details */ },
        color = CardBackground,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon/Image Placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    tint = Orange,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.title,
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${workout.duration} min • ${workout.difficulty}",
                    color = GrayText,
                    fontSize = 13.sp
                )
                
                if (workout.progress > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { workout.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = Orange,
                        trackColor = SurfaceColor,
                    )
                }
            }
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
