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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainwise.data.models.Exercise
import com.example.trainwise.data.models.SelectedExercise
import com.example.trainwise.data.models.Workout
import com.example.trainwise.ui.theme.Orange
import com.example.trainwise.ui.viewmodels.WorkoutViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(
    onNavigateBack: () -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val allExercises = remember { viewModel.getAllExercises() }
    val trainingTypes = remember { listOf("Strength", "Cardio", "Yoga", "HIIT", "Flexibility") }
    val muscleGroups = remember { listOf("All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core") }
    
    var selectedTrainingType by remember { mutableStateOf("Strength") }
    var selectedMuscleGroup by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var workoutName by remember { mutableStateOf("") }
    var restTime by remember { mutableIntStateOf(60) }
    var selectedExercises by remember { mutableStateOf(listOf<SelectedExercise>()) }

    val handleSaveWorkout = {
        val currentUser = auth.currentUser
        if (currentUser != null && workoutName.isNotBlank() && selectedExercises.isNotEmpty()) {
            val newWorkout = Workout(
                userId = currentUser.uid,
                title = workoutName,
                category = selectedTrainingType,
                exercises = selectedExercises,
                restTime = restTime
            )

            viewModel.saveWorkout(
                workout = newWorkout,
                onSuccess = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Workout '$workoutName' saved successfully!")
                        onNavigateBack()
                    }
                },
                onError = { error ->
                    scope.launch {
                        snackbarHostState.showSnackbar("Error: $error")
                    }
                }
            )
        }
    }

    val filteredExercises = allExercises.filter {
        (selectedMuscleGroup == "All" || it.muscleGroup == selectedMuscleGroup) &&
                it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = Orange,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text("Create Workout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val canSave = workoutName.isNotBlank() && selectedExercises.isNotEmpty()
                    TextButton(
                        onClick = { handleSaveWorkout() },
                        enabled = canSave
                    ) {
                        Text(
                            "Save",
                            color = if (canSave) Orange else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = workoutName,
                            onValueChange = { workoutName = it },
                            label = { Text("Workout Name", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = Orange,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                cursorColor = Orange
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Text(
                            "Training Type", 
                            color = MaterialTheme.colorScheme.onBackground, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Medium
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(trainingTypes) { type ->
                                FilterChip(
                                    selected = selectedTrainingType == type,
                                    onClick = { selectedTrainingType = type },
                                    label = { Text(type) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Orange,
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = null
                                )
                            }
                        }

                        Text(
                            "Rest Time Between Sets", 
                            color = MaterialTheme.colorScheme.onBackground, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Medium
                        )
                        CounterItem(
                            label = "Seconds",
                            value = restTime,
                            onValueChange = { restTime = it },
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Add Exercises", 
                                color = MaterialTheme.colorScheme.onSurface, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { 
                                    Text(
                                        "Search...", 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), 
                                        fontSize = 14.sp
                                    ) 
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                leadingIcon = { 
                                    Icon(
                                        Icons.Outlined.Search, 
                                        null, 
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                                        modifier = Modifier.size(20.dp)
                                    ) 
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = Orange,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(muscleGroups) { group ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (selectedMuscleGroup == group) Orange.copy(alpha = 0.2f) 
                                                else MaterialTheme.colorScheme.secondaryContainer
                                            )
                                            .border(
                                                1.dp, 
                                                if (selectedMuscleGroup == group) Orange else Color.Transparent, 
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedMuscleGroup = group }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            group, 
                                            color = if (selectedMuscleGroup == group) Orange else MaterialTheme.colorScheme.onSurfaceVariant, 
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.height(60.dp)
                            ) {
                                items(filteredExercises) { exercise ->
                                    Box(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .clickable {
                                                if (!selectedExercises.any { it.exercise.id == exercise.id }) {
                                                    selectedExercises = selectedExercises + SelectedExercise(exercise)
                                                }
                                            }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    exercise.name, 
                                                    color = MaterialTheme.colorScheme.onSurface, 
                                                    fontSize = 12.sp, 
                                                    fontWeight = FontWeight.Bold, 
                                                    maxLines = 1
                                                )
                                                Text(
                                                    exercise.muscleGroup, 
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Icon(
                                                Icons.Default.Add, 
                                                null, 
                                                tint = Orange, 
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your Workout Plan", 
                            color = MaterialTheme.colorScheme.onBackground, 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            color = Orange.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Text(
                                "${selectedExercises.size} items",
                                color = Orange,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                items(selectedExercises) { selected ->
                    SelectedExerciseCard(
                        selected = selected,
                        onUpdate = { updated ->
                            selectedExercises = selectedExercises.map {
                                if (it.exercise.id == updated.exercise.id) updated else it
                            }
                        },
                        onRemove = {
                            selectedExercises = selectedExercises.filter { it.exercise.id != selected.exercise.id }
                        }
                    )
                }
                
                if (selectedExercises.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No exercises added yet", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedExerciseCard(
    selected: SelectedExercise,
    onUpdate: (SelectedExercise) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        selected.exercise.name, 
                        color = MaterialTheme.colorScheme.onSurface, 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        selected.exercise.muscleGroup, 
                        color = Orange, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.1f))
                ) {
                    Icon(
                        Icons.Default.Remove, 
                        null, 
                        tint = Color.Red, 
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CounterItem(
                    label = "Sets",
                    value = selected.sets,
                    onValueChange = { onUpdate(selected.copy(sets = it)) },
                    modifier = Modifier.weight(1f)
                )
                CounterItem(
                    label = "Reps",
                    value = selected.reps,
                    onValueChange = { onUpdate(selected.copy(reps = it)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CounterItem(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            label, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            fontSize = 12.sp, 
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { if (value > 1) onValueChange(value - 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Remove, 
                    null, 
                    tint = MaterialTheme.colorScheme.onSurface, 
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Text(
                text = value.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            
            IconButton(
                onClick = { onValueChange(value + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Add, 
                    null, 
                    tint = MaterialTheme.colorScheme.onSurface, 
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
