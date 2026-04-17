package com.example.trainwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainwise.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToWorkouts: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToAccountDetails: () -> Unit,
    onNavigateToBiometrics: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToTrainingHistory: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(userId) {
        userId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        username = document.getString("username") ?: ""
                        email = document.getString("email") ?: ""
                    }
                }
        }
    }

    Scaffold(
        bottomBar = {
            ProfileBottomNavigationBar(
                onHomeClick = onNavigateToHome,
                onWorkoutsClick = onNavigateToWorkouts,
                onGuideClick = onNavigateToGuide
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))


                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable { onNavigateToAccountDetails() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(CardBackground)
                            .border(2.dp, Orange, CircleShape)
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            tint = GrayText
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Orange,
                        modifier = Modifier.size(32.dp).border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    ) {
                        Icon(Icons.Outlined.Edit, null, tint = White, modifier = Modifier.padding(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(username, color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(email, color = GrayText, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f).clickable { onNavigateToTrainingHistory() }, 
                        icon = Icons.Outlined.History, 
                        value = "24", 
                        label = "Sessions"
                    )
                    StatCard(Modifier.weight(1f), Icons.Outlined.EmojiEvents, "12", "Badges")
                    StatCard(Modifier.weight(1f), Icons.Outlined.MonitorWeight, "78kg", "Weight")
                }

                Spacer(modifier = Modifier.height(32.dp))


                Text(
                    "Settings",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            item { 
                ProfileMenuItem(
                    Icons.Outlined.PersonOutline, 
                    "Account Details",
                    onClick = onNavigateToAccountDetails
                ) 
            }
            item { 
                ProfileMenuItem(
                    Icons.Outlined.History, 
                    "Training History",
                    onClick = onNavigateToTrainingHistory
                ) 
            }
            item { 
                ProfileMenuItem(
                    Icons.Outlined.Watch, 
                    "Biometric Devices",
                    onClick = onNavigateToBiometrics
                ) 
            }
            item { 
                ProfileMenuItem(
                    Icons.Outlined.Notifications, 
                    "Notifications",
                    onClick = onNavigateToNotifications
                ) 
            }
            item { 
                ProfileMenuItem(
                    Icons.Outlined.Shield, 
                    "Privacy & Security",
                    onClick = onNavigateToSecurity
                ) 
            }
            item {
                ProfileMenuItem(
                    Icons.Outlined.Logout,
                    "Sign Out",
                    textColor = Color.Red,
                    iconColor = Color.Red
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Orange, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = GrayText, fontSize = 12.sp)
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    textColor: Color = White,
    iconColor: Color = Orange,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, color = if (textColor == White) MaterialTheme.colorScheme.onBackground else textColor, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, null, tint = GrayText)
        }
    }
}

@Composable
fun ProfileBottomNavigationBar(
    onHomeClick: () -> Unit,
    onWorkoutsClick: () -> Unit,
    onGuideClick: () -> Unit
) {
    NavigationBar(containerColor = Color.Black, contentColor = White) {
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = { Icon(Icons.Outlined.Home, null) },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = White, unselectedTextColor = White)
        )
        NavigationBarItem(
            selected = false,
            onClick = onWorkoutsClick,
            icon = { Icon(Icons.Outlined.FitnessCenter, null) },
            label = { Text("Workouts") },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = White, unselectedTextColor = White)
        )
        NavigationBarItem(
            selected = false,
            onClick = onGuideClick,
            icon = { Icon(Icons.Outlined.MenuBook, null) },
            label = { Text("Guide") },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = White, unselectedTextColor = White)
        )
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Outlined.Person, null) },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = White,
                indicatorColor = Orange,
                selectedTextColor = White
            )
        )
    }
}
