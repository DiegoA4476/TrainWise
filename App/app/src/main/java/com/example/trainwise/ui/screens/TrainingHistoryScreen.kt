package com.example.trainwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Timer
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

data class TrainingSession(
    val id: Int,
    val title: String,
    val date: String,
    val duration: String,
    val calories: String,
    val type: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingHistoryScreen(
    onNavigateBack: () -> Unit
) {
    val history = listOf(
        TrainingSession(1, "Full Body Workout", "Oct 24, 2023", "45 min", "320 kcal", "Strength"),
        TrainingSession(2, "Morning Run", "Oct 22, 2023", "30 min", "250 kcal", "Cardio"),
        TrainingSession(3, "Leg Day", "Oct 20, 2023", "50 min", "400 kcal", "Strength"),
        TrainingSession(4, "Yoga Session", "Oct 18, 2023", "60 min", "150 kcal", "Flexibility"),
        TrainingSession(5, "HIIT Cardio", "Oct 15, 2023", "25 min", "350 kcal", "Cardio"),
        TrainingSession(6, "Upper Body", "Oct 12, 2023", "40 min", "280 kcal", "Strength")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Training History", color = White, fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }
            
            items(history) { session ->
                HistoryCard(session)
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun HistoryCard(session: TrainingSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.title,
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = Orange.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = session.type,
                        color = Orange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HistoryDetailItem(Icons.Outlined.CalendarToday, session.date)
                HistoryDetailItem(Icons.Outlined.Timer, session.duration)
                HistoryDetailItem(Icons.Outlined.FitnessCenter, session.calories)
            }
        }
    }
}

@Composable
fun HistoryDetailItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = GrayText,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = GrayText,
            fontSize = 13.sp
        )
    }
}
