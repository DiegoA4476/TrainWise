package com.example.trainwise.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainwise.data.models.SelectedExercise
import com.example.trainwise.ui.theme.*
import com.example.trainwise.ui.viewmodels.WorkoutViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    workoutId: String,
    onNavigateBack: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    val workouts by viewModel.workouts
    val workout = workouts.find { it.id == workoutId }

    if (workout == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Orange)
        }
        return
    }

    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var currentSet by remember { mutableIntStateOf(1) }
    var isResting by remember { mutableStateOf(false) }
    var restTimeLeft by remember { mutableIntStateOf(workout.restTime) }
    var workoutCompleted by remember { mutableStateOf(false) }
    
    // Track start time to calculate duration
    val startTime = remember { System.currentTimeMillis() }

    val currentExercise = workout.exercises[currentExerciseIndex]
    val totalExercises = workout.exercises.size

    LaunchedEffect(isResting) {
        if (isResting) {
            restTimeLeft = workout.restTime
            while (restTimeLeft > 0) {
                delay(1000)
                restTimeLeft--
            }
            isResting = false
            if (currentSet < currentExercise.sets) {
                currentSet++
            } else {
                if (currentExerciseIndex < totalExercises - 1) {
                    currentExerciseIndex++
                    currentSet = 1
                } else {
                    // WORKOUT FINISHED
                    val endTime = System.currentTimeMillis()
                    val durationMinutes = ((endTime - startTime) / 60000).toInt().coerceAtLeast(1)
                    viewModel.saveCompletedWorkout(workout, durationMinutes)
                    workoutCompleted = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout.title, color = White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        if (workoutCompleted) {
            WorkoutCompletionScreen(workout.title, onNavigateBack)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Header
                WorkoutProgressHeader(
                    currentIndex = currentExerciseIndex,
                    total = totalExercises,
                    exerciseName = currentExercise.exercise.name
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (isResting) {
                    RestTimerDisplay(timeLeft = restTimeLeft)
                } else {
                    ExerciseDisplay(
                        exercise = currentExercise,
                        currentSet = currentSet,
                        onFinishSet = {
                            isResting = true
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Exercise Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FitnessCenter, null, tint = Orange)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(currentExercise.exercise.name, color = White, fontWeight = FontWeight.Bold)
                            Text(currentExercise.exercise.muscleGroup, color = GrayText, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutProgressHeader(currentIndex: Int, total: Int, exerciseName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Exercise ${currentIndex + 1} of $total",
                color = GrayText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${((currentIndex + 1).toFloat() / total * 100).toInt()}%",
                color = Orange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / total },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = Orange,
            trackColor = CardBackground
        )
    }
}

@Composable
fun ExerciseDisplay(
    exercise: SelectedExercise,
    currentSet: Int,
    onFinishSet: () -> Unit
) {
    val setsRemaining = exercise.sets - currentSet + 1

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "TARGET: ${exercise.reps} REPS",
            color = Orange,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Orange.copy(alpha = 0.2f), Color.Transparent)))
                .border(4.dp, Orange, CircleShape)
                .clickable { onFinishSet() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    setsRemaining.toString(),
                    color = White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    if (setsRemaining == 1) "SET LEFT" else "SETS LEFT",
                    color = GrayText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Tap the circle or button to finish set", color = GrayText, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onFinishSet,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("FINISH SET", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun RestTimerDisplay(timeLeft: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "REST TIME",
            color = Orange,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(CardBackground)
                .border(2.dp, GrayText.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                timeLeft.toString(),
                color = White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Black
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text("Take a deep breath...", color = White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Text("Next set starts automatically", color = GrayText, fontSize = 14.sp)
    }
}

@Composable
fun WorkoutCompletionScreen(workoutTitle: String, onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Orange,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "WORKOUT COMPLETE!",
            color = White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "You just finished $workoutTitle. Great job keeping the momentum!",
            color = GrayText,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("FINISH", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
