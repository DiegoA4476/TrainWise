package com.example.trainwise.data.models

data class Workout(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "Strength",
    val exercises: List<SelectedExercise> = listOf(),
    val duration: Int = 0,
    val restTime: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
