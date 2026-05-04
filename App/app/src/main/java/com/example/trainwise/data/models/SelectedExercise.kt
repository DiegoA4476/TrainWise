package com.example.trainwise.data.models

data class SelectedExercise(
    val exercise: Exercise = Exercise(),
    val reps: Int = 10,
    val sets: Int = 3
)
