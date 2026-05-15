package com.example.trainwise.data.models

data class CompletedWorkout(
    val id: String = "",
    val userId: String = "",
    val workoutId: String = "",
    val title: String = "",
    val category: String = "",
    val durationMinutes: Int = 0,
    val caloriesBurned: Int = 0,
    val avgBpm: Int = 0,
    val heartRateSamples: List<HeartRateSample> = listOf(),
    val exercises: List<CompletedExercise> = listOf(),
    val timestamp: Long = System.currentTimeMillis()
)

data class HeartRateSample(
    val bpm: Int = 0,
    val timestamp: Long = 0
)

data class CompletedExercise(
    val name: String = "",
    val muscleGroup: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weights: List<Double> = listOf()
)
