package com.example.trainwise.ui.screens

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainwise.data.models.CompletedExercise
import com.example.trainwise.data.models.SelectedExercise
import com.example.trainwise.data.models.Workout
import com.example.trainwise.ui.theme.*
import com.example.trainwise.ui.viewmodels.BiometricViewModel
import com.example.trainwise.ui.viewmodels.WorkoutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    workoutId: String,
    onNavigateBack: () -> Unit,
    workoutViewModel: WorkoutViewModel = viewModel(),
    biometricViewModel: BiometricViewModel = viewModel()
) {
    val workouts by workoutViewModel.workouts
    val workout = workouts.find { it.id == workoutId }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (workout == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Orange)
        }
        return
    }

    var isStarted by remember { mutableStateOf(false) }
    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var currentSet by remember { mutableIntStateOf(1) }
    var isResting by remember { mutableStateOf(false) }
    var restTimeLeft by remember { mutableIntStateOf(workout.restTime) }
    var isWaitingForBiometrics by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf<Instant?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val exerciseWeights = remember { mutableStateMapOf<Int, MutableList<Double>>() }

    val currentExercise = workout.exercises[currentExerciseIndex]
    val totalExercises = workout.exercises.size

    val suggestedWeight = remember(currentExerciseIndex, currentSet) {
        val currentExerciseName = currentExercise.exercise.name

        fun format(w: Double): String = if (w == w.toInt().toDouble()) w.toInt().toString() else w.toString()

        val currentWeights = exerciseWeights[currentExerciseIndex]
        if (!currentWeights.isNullOrEmpty()) {
            val last = currentWeights.last()
            return@remember if (last > 0) format(last) else ""
        }

        for (i in (currentExerciseIndex - 1) downTo 0) {
            if (workout.exercises[i].exercise.name == currentExerciseName) {
                val weights = exerciseWeights[i]
                if (!weights.isNullOrEmpty()) {
                    val last = weights.last()
                    return@remember if (last > 0) format(last) else ""
                }
            }
        }
        ""
    }

    DisposableEffect(isStarted) {
        val activity = context as? Activity
        if (isStarted) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun triggerVibrationAndNotification(startingRest: Boolean) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(600)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "workout_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Workout Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alerts for workout rest intervals"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(if (startingRest) "Rest Started" else "Rest Finished!")
            .setContentText(if (startingRest) "Time to recover." else "Back to work!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2, notification)
    }

    LaunchedEffect(Unit) {
        biometricViewModel.updateDeviceConnectionStatus(context)
    }

    LaunchedEffect(isResting) {
        if (isResting) {
            triggerVibrationAndNotification(true)
            restTimeLeft = workout.restTime
            while (restTimeLeft > 0) {
                delay(1000)
                restTimeLeft--
            }
            triggerVibrationAndNotification(false)
            isResting = false

            if (currentSet < currentExercise.sets) {
                currentSet++
            } else {
                if (currentExerciseIndex < totalExercises - 1) {
                    currentExerciseIndex++
                    currentSet = 1
                } else {
                    isWaitingForBiometrics = true
                }
            }
        }
    }

    fun finishWorkout() {
        scope.launch {
            isSaving = true
            val endTime = Instant.now()
            val durationMinutes = startTime?.let {
                java.time.Duration.between(it, endTime).toMinutes().toInt()
            }?.coerceAtLeast(1) ?: 1

            val hrSamples = startTime?.let {
                biometricViewModel.fetchHeartRateSamples(context, it, endTime)
            } ?: emptyList()

            val summary = startTime?.let {
                biometricViewModel.fetchWorkoutSummary(context, it, endTime)
            } ?: Pair(0, 0)

            val completedExercises = workout.exercises.mapIndexed { index, ex ->
                CompletedExercise(
                    name = ex.exercise.name,
                    muscleGroup = ex.exercise.muscleGroup,
                    sets = ex.sets,
                    reps = ex.reps,
                    weights = exerciseWeights[index] ?: List(ex.sets) { 0.0 }
                )
            }

            val success = workoutViewModel.saveDetailedCompletedWorkout(
                workout = workout,
                durationMinutes = durationMinutes,
                caloriesBurned = summary.second,
                avgBpm = summary.first,
                heartRateSamples = hrSamples,
                exercises = completedExercises
            )
            isSaving = false
            if (success) {
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
        Box(modifier = Modifier.padding(padding)) {
            if (isWaitingForBiometrics) {
                BiometricFinishScreen(
                    isAnyDeviceConnected = biometricViewModel.isAnyDeviceConnected.value,
                    isSaving = isSaving,
                    onFinish = { finishWorkout() }
                )
            } else if (!isStarted) {
                WorkoutOverview(
                    workout = workout,
                    onStart = {
                        biometricViewModel.resetSessionData()
                        exerciseWeights.clear()
                        startTime = Instant.now()
                        isStarted = true
                    }
                )
            } else {
                ActiveWorkoutContent(
                    currentExercise = currentExercise,
                    currentExerciseIndex = currentExerciseIndex,
                    totalExercises = totalExercises,
                    isResting = isResting,
                    restTimeLeft = restTimeLeft,
                    currentSet = currentSet,
                    suggestedWeight = suggestedWeight,
                    onFinishSet = { weight ->
                        val list = exerciseWeights.getOrPut(currentExerciseIndex) { mutableListOf() }
                        list.add(weight)
                        isResting = true
                    }
                )
            }
        }
    }
}

@Composable
fun BiometricFinishScreen(
    isAnyDeviceConnected: Boolean,
    isSaving: Boolean,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Watch,
            null,
            tint = Orange,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Almost Done!",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isAnyDeviceConnected) {
                "Please finish the workout on your connected biometric device to ensure all data is synced correctly."
            } else {
                "Make sure your workout data is synced to Health Connect before finishing."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "For a full sync, the workout should be saved around 30 seconds after the workout in the watch is finished.",
            color = Orange,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFinish,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("FINISH AND SAVE", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun ActiveWorkoutContent(
    currentExercise: SelectedExercise,
    currentExerciseIndex: Int,
    totalExercises: Int,
    isResting: Boolean,
    restTimeLeft: Int,
    currentSet: Int,
    suggestedWeight: String,
    onFinishSet: (Double) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WorkoutProgressHeader(currentIndex = currentExerciseIndex, total = totalExercises)

        Spacer(modifier = Modifier.height(32.dp))

        if (isResting) {
            RestTimerDisplay(timeLeft = restTimeLeft)
        } else {
            ExerciseDisplayWithWeight(
                exercise = currentExercise,
                currentSet = currentSet,
                suggestedWeight = suggestedWeight,
                onFinishSet = onFinishSet
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FitnessCenter, null, tint = Orange)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(currentExercise.exercise.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text(currentExercise.exercise.muscleGroup, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ExerciseDisplayWithWeight(
    exercise: SelectedExercise,
    currentSet: Int,
    suggestedWeight: String,
    onFinishSet: (Double) -> Unit
) {
    var weightText by remember(exercise, currentSet) { mutableStateOf(suggestedWeight) }
    val setsRemaining = exercise.sets - currentSet + 1

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("TARGET: ${exercise.reps} REPS", color = Orange, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = weightText,
            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) weightText = it },
            label = { Text("Weight (kg)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(0.6f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = Orange,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                cursorColor = Orange
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.size(180.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Orange.copy(alpha = 0.2f), Color.Transparent))).border(4.dp, Orange, CircleShape).clickable {
            onFinishSet(weightText.toDoubleOrNull() ?: 0.0)
        }, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(setsRemaining.toString(), color = MaterialTheme.colorScheme.onSurface, fontSize = 64.sp, fontWeight = FontWeight.Black)
                Text(if (setsRemaining == 1) "SET LEFT" else "SETS LEFT", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { onFinishSet(weightText.toDoubleOrNull() ?: 0.0) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("FINISH SET", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun WorkoutOverview(workout: Workout, onStart: () -> Unit) {
    val totalSets = workout.exercises.sumOf { it.sets }
    val estimatedDuration = if (workout.duration > 0) workout.duration else (workout.exercises.size * 5)

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "Routine Overview", color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryItem(label = "Category", value = workout.category, icon = Icons.Default.Category, modifier = Modifier.weight(1f))
            SummaryItem(label = "Total Sets", value = "$totalSets Sets", icon = Icons.Default.Layers, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryItem(label = "Estimated", value = "$estimatedDuration min", icon = Icons.Default.Timer, modifier = Modifier.weight(1f))
            SummaryItem(label = "Rest Time", value = "${workout.restTime}s", icon = Icons.Default.PauseCircle, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Exercise List", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(workout.exercises) { selectedExercise ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FitnessCenter, null, tint = Orange, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = selectedExercise.exercise.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(text = selectedExercise.exercise.muscleGroup, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "${selectedExercise.sets} sets", color = Orange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "${selectedExercise.reps} reps", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange), shape = RoundedCornerShape(16.dp)) {
            Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.onPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("START WORKOUT", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Orange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                Text(text = value, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WorkoutProgressHeader(currentIndex: Int, total: Int) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Exercise ${currentIndex + 1} of $total", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text("${(currentIndex.toFloat() / total * 100).toInt()}%", color = Orange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { currentIndex.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = Orange,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun RestTimerDisplay(timeLeft: Int) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("REST TIME", color = Orange, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier.size(200.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
            Text(timeLeft.toString(), color = MaterialTheme.colorScheme.onSurface, fontSize = 64.sp, fontWeight = FontWeight.Black)
        }
    }
}
