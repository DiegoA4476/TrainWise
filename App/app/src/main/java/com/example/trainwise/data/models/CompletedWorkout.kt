package com.example.trainwise.data.models

data class CompletedWorkout(
    val id: String = "",
    val userId: String = "",
    val workoutId: String = "",
    val title: String = "",
    val category: String = "",
    val durationMinutes: Int = 0,
    val caloriesBurned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
