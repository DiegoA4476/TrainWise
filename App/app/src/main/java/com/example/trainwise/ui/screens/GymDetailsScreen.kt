package com.example.trainwise.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.trainwise.data.models.Gym
import com.example.trainwise.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymDetailsScreen(
    gym: Gym,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val apiKey = "AIzaSyBL8O8dVwzdkqhNTT6kU3xQmKiGzVlyq-M" // Consider moving this to a config file later

    val photoUrl = if (gym.photoReference != null) {
        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=800&photoreference=${gym.photoReference}&key=$apiKey"
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gym.name, color = White, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Gym Image from Google Places
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = gym.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Orange.copy(alpha = 0.8f), DarkBackground)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.FitnessCenter,
                            contentDescription = null,
                            tint = Orange,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                
                // Bottom Gradient for better text readability if needed
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, DarkBackground.copy(alpha = 0.7f)),
                                startY = 300f
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .offset(y = (-30).dp)
            ) {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = gym.name,
                            color = White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, null, tint = Orange, modifier = Modifier.size(18.dp))
                            Text(
                                text = " ${gym.rating} • Gym & Fitness Center",
                                color = GrayText,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(24.dp))

                        InfoRow(Icons.Outlined.LocationOn, "Address", gym.address)
                        Spacer(modifier = Modifier.height(20.dp))
                        InfoRow(Icons.Outlined.History, "Hours", "07:00 AM - 10:00 PM")
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        val distanceText = if (gym.distance != null) {
                            if (gym.distance < 1000) "${gym.distance.toInt()}m away" else String.format("%.1fkm away", gym.distance / 1000)
                        } else "Distance unknown"
                        InfoRow(Icons.Outlined.DirectionsRun, "Proximity", distanceText)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Button(
                    onClick = {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${gym.name} ${gym.address}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Outlined.Directions, null, tint = White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Directions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { /* Call gym logic */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Orange),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Orange)
                ) {
                    Icon(Icons.Outlined.Phone, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contact Gym", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = SurfaceColor,
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Orange, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, color = GrayText, fontSize = 12.sp)
            Text(value, color = White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
