package com.example.trainwise.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trainwise.data.models.Workout
import com.google.firebase.firestore.Query


class WorkoutViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _workouts = mutableStateOf<List<Workout>>(emptyList())
    val workouts: State<List<Workout>> = _workouts

    var isLoading = mutableStateOf(true)

    init {
        fetchWorkouts()
    }

    private fun fetchWorkouts() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("workouts")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = snapshot.toObjects(Workout::class.java)
                    _workouts.value = list
                    isLoading.value = false
                }
            }
    }

}