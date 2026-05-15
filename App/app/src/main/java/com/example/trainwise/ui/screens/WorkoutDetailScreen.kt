package com.example.trainwise.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainwise.data.models.CompletedExercise
import com.example.trainwise.data.models.CompletedWorkout
import com.example.trainwise.data.models.HeartRateSample
import com.example.trainwise.ui.theme.*
import com.example.trainwise.ui.viewmodels.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    completedWorkoutId: String,
    onNavigateBack: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    val history by viewModel.completedWorkouts
    val workout = history.find { it.id == completedWorkoutId }

    if (workout == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Orange)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                WorkoutHeader(workout)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailStatCard(
                        label = "Duration",
                        value = "${workout.durationMinutes}m",
                        icon = Icons.Default.Timer,
                        color = Orange,
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatCard(
                        label = "Calories",
                        value = "${workout.caloriesBurned}",
                        icon = Icons.Default.LocalFireDepartment,
                        color = Color(0xFFFFAB40),
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatCard(
                        label = "Avg BPM",
                        value = "${workout.avgBpm}",
                        icon = Icons.Default.Favorite,
                        color = Color(0xFFFF5252),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (workout.heartRateSamples.isNotEmpty()) {
                item {
                    Text(
                        "Heart Rate Analysis", 
                        color = MaterialTheme.colorScheme.onBackground, 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HeartRateChart(samples = workout.heartRateSamples)
                }
            }

            item {
                Text(
                    "Exercises & Weights", 
                    color = MaterialTheme.colorScheme.onBackground, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
            }

            items(workout.exercises) { exercise ->
                CompletedExerciseCard(exercise)
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun WorkoutHeader(workout: CompletedWorkout) {
    val sdf = SimpleDateFormat("EEEE, MMM dd 'at' HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(workout.timestamp))

    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            workout.title, 
            color = MaterialTheme.colorScheme.onBackground, 
            fontSize = 28.sp, 
            fontWeight = FontWeight.Black
        )
        Text(
            dateString, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = Orange.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                workout.category,
                color = Orange,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DetailStatCard(
    label: String, 
    value: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    color: Color, 
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value, 
                color = MaterialTheme.colorScheme.onSurface, 
                fontSize = 18.sp, 
                fontWeight = FontWeight.Bold
            )
            Text(
                label, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun HeartRateChart(samples: List<HeartRateSample>) {
    val maxBpm = samples.maxOf { it.bpm }.toFloat().coerceAtLeast(100f)
    val minBpm = samples.minOf { it.bpm }.toFloat().coerceAtMost(60f)
    val range = maxBpm - minBpm

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val spaceBetween = width / (samples.size - 1).coerceAtLeast(1)

                val path = Path()
                samples.forEachIndexed { index, sample ->
                    val x = index * spaceBetween
                    val y = height - ((sample.bpm - minBpm) / range * height)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = Color(0xFFFF5252),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Fill under the curve
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF5252).copy(alpha = 0.3f), Color.Transparent)
                    )
                )
            }
            
            // Labels
            Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                Text("${maxBpm.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                Text("${((maxBpm + minBpm) / 2).toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                Text("${minBpm.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun CompletedExerciseCard(exercise: CompletedExercise) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FitnessCenter, 
                        null, 
                        tint = Orange, 
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        exercise.name, 
                        color = MaterialTheme.colorScheme.onSurface, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        exercise.muscleGroup, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                exercise.weights.forEachIndexed { index, weight ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "SET ${index + 1}", 
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${weight}kg", 
                            color = MaterialTheme.colorScheme.onSurface, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${exercise.sets} sets x ${exercise.reps} reps",
                color = Orange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
