package com.example.trainwise.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trainwise.data.models.Workout
import com.example.trainwise.data.models.CompletedWorkout
import com.google.firebase.firestore.Query


class WorkoutViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _workouts = mutableStateOf<List<Workout>>(emptyList())
    val workouts: State<List<Workout>> = _workouts

    private val _completedWorkouts = mutableStateOf<List<CompletedWorkout>>(emptyList())
    val completedWorkouts: State<List<CompletedWorkout>> = _completedWorkouts

    var isLoading = mutableStateOf(true)

    init {
        fetchWorkouts()
        fetchHistory()
    }

    fun fetchWorkouts() {
        val userId = auth.currentUser?.uid ?: return
        isLoading.value = true
        
        db.collection("workouts")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.toObjects(Workout::class.java)
                    _workouts.value = list.sortedByDescending { it.createdAt }
                    isLoading.value = false
                }
            }
    }

    fun fetchHistory() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("completed_workouts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    _completedWorkouts.value = snapshot.toObjects(CompletedWorkout::class.java)
                }
            }
    }

    fun deleteWorkout(workoutId: String) {
        if (workoutId.isEmpty()) return

        db.collection("workouts").document(workoutId)
            .delete()
            .addOnSuccessListener {
                _workouts.value = _workouts.value.filter { it.id != workoutId }
            }
    }

    fun saveCompletedWorkout(workout: Workout, durationMinutes: Int) {
        val userId = auth.currentUser?.uid ?: return
        val id = db.collection("completed_workouts").document().id
        
        // Simple calorie calculation: ~7 kcal per minute for strength training
        val calories = durationMinutes * 7

        val completed = CompletedWorkout(
            id = id,
            userId = userId,
            workoutId = workout.id,
            title = workout.title,
            category = workout.category,
            durationMinutes = durationMinutes,
            caloriesBurned = calories,
            timestamp = System.currentTimeMillis()
        )

        db.collection("completed_workouts").document(id).set(completed)
    }
}
