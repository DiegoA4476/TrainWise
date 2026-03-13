package com.example.trainwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainwise.ui.theme.*

data class Gym(
    val name: String,
    val rating: Double,
    val distance: String,
    val tags: List<String>,
    val price: Int
)

@Composable
fun HomeScreen(
    onNavigateToWorkouts: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val gyms = listOf(
        Gym("FitZone Premium", 4.8, "0.5 km", listOf("CrossFit", "Yoga", "Spinning"), 850),
        Gym("PowerGym Centro", 4.6, "1.2 km", listOf("Strength", "Functional", "Boxing"), 1200)
    )

    Scaffold(
        bottomBar = { 
            HomeBottomNavigationBar(
                onWorkoutsClick = onNavigateToWorkouts,
                onGuideClick = onNavigateToGuide,
                onProfileClick = onNavigateToProfile
            ) 
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Map Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(CardBackground, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Orange, modifier = Modifier.size(40.dp))
                    Text("Map View Placeholder", color = White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(Modifier.weight(1f), Icons.Outlined.LocationOn, "5", "Gyms")
                StatCard(Modifier.weight(1f), Icons.Outlined.Navigation, "0.5 km", "Nearest")
                StatCard(Modifier.weight(1f), Icons.Outlined.Star, "4.7", "Average")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, tint = Orange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nearby Gyms", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(gyms) { gym ->
                    GymCard(gym)
                }
            }
        }
    }
}

@Composable
fun HomeBottomNavigationBar(
    onWorkoutsClick: () -> Unit,
    onGuideClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.Black,
        contentColor = White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Outlined.Home, null) },
            label = { Text("Home") },
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
            onClick = onWorkoutsClick,
            icon = { Icon(Icons.Outlined.FitnessCenter, null) },
            label = { Text("Workouts") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = White,
                unselectedTextColor = White,
                indicatorColor = Orange
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

@Composable
fun StatCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Orange, modifier = Modifier.size(24.dp))
            Text(text = value, color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = GrayText, fontSize = 12.sp)
        }
    }
}

@Composable
fun GymCard(gym: Gym) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = gym.name, color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = Orange, modifier = Modifier.size(16.dp))
                        Text(text = " ${gym.rating}  •  ${gym.distance}", color = LightGray, fontSize = 14.sp)
                    }
                }
                Surface(
                    color = Color(0xFF2C1C14),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${gym.price}",
                        color = Orange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                gym.tags.forEach { tag ->
                    Surface(
                        color = SurfaceColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = tag,
                            color = LightGray,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
