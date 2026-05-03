package com.example.trainwise.data.models

data class Workout(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "Strength",
    val exercises: List<SelectedExercise> = listOf(),
    val createdAt: Long = System.currentTimeMillis()
)