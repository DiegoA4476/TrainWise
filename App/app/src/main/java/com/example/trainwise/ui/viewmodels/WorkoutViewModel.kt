package com.example.trainwise.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainwise.data.models.Workout
import com.example.trainwise.data.models.CompletedWorkout
import com.example.trainwise.data.models.CompletedExercise
import com.example.trainwise.data.models.Exercise
import com.example.trainwise.data.models.HeartRateSample
import com.example.trainwise.data.repositories.WorkoutRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class WorkoutViewModel(
    private val repository: WorkoutRepository = WorkoutRepository()
) : ViewModel() {

    private val _workouts = mutableStateOf<List<Workout>>(emptyList())
    val workouts: State<List<Workout>> = _workouts

    private val _completedWorkouts = mutableStateOf<List<CompletedWorkout>>(emptyList())
    val completedWorkouts: State<List<CompletedWorkout>> = _completedWorkouts

    private val _isLoadingWorkouts = mutableStateOf(false)
    val isLoadingWorkouts: State<Boolean> = _isLoadingWorkouts

    private val _isLoadingHistory = mutableStateOf(false)
    val isLoadingHistory: State<Boolean> = _isLoadingHistory

    init {
        fetchWorkouts()
        fetchHistory()
    }

    fun fetchWorkouts() {
        viewModelScope.launch {
            _isLoadingWorkouts.value = true
            repository.getWorkouts().collectLatest { list ->
                _workouts.value = list
                _isLoadingWorkouts.value = false
            }
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _isLoadingHistory.value = true
            repository.getCompletedWorkouts().collectLatest { list ->
                _completedWorkouts.value = list
                _isLoadingHistory.value = false
            }
        }
    }

    fun deleteWorkout(workoutId: String) {
        viewModelScope.launch {
            _workouts.value = _workouts.value.filterNot { it.id == workoutId }
            repository.deleteWorkout(workoutId)
        }
    }

    fun deleteCompletedWorkouts(workoutIds: Set<String>) {
        viewModelScope.launch {
            _completedWorkouts.value = _completedWorkouts.value.filterNot { it.id in workoutIds }
            repository.deleteCompletedWorkouts(workoutIds)
        }
    }

    fun saveWorkout(workout: Workout, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val workoutToSave = if (workout.id.isEmpty()) {
                workout.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis(), userId = repository.userId ?: "")
            } else {
                workout
            }

            val oldWorkouts = _workouts.value
            _workouts.value = (listOf(workoutToSave) + _workouts.value.filter { it.id != workoutToSave.id })
                .sortedByDescending { it.createdAt }

            try {
                repository.saveWorkout(workoutToSave)
                onSuccess()
            } catch (e: Exception) {
                _workouts.value = oldWorkouts
                onError(e.message ?: "Failed to save workout")
            }
        }
    }

    suspend fun saveDetailedCompletedWorkout(
        workout: Workout,
        durationMinutes: Int,
        caloriesBurned: Int,
        avgBpm: Int,
        heartRateSamples: List<HeartRateSample>,
        exercises: List<CompletedExercise>
    ): Boolean {
        val completed = CompletedWorkout(
            id = UUID.randomUUID().toString(),
            userId = repository.userId ?: "",
            workoutId = workout.id,
            title = workout.title,
            category = workout.category,
            durationMinutes = durationMinutes,
            caloriesBurned = caloriesBurned,
            avgBpm = avgBpm,
            heartRateSamples = heartRateSamples,
            exercises = exercises,
            timestamp = System.currentTimeMillis()
        )

        val oldHistory = _completedWorkouts.value
        _completedWorkouts.value = (listOf(completed) + oldHistory).sortedByDescending { it.timestamp }

        return try {
            repository.saveCompletedWorkout(completed)
            true
        } catch (e: Exception) {
            _completedWorkouts.value = oldHistory
            e.printStackTrace()
            false
        }
    }

    fun getAllExercises(): List<Exercise> {
        return repository.getAllExercises()
    }
}
