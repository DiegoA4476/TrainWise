package com.example.trainwise.data.models

import androidx.compose.ui.graphics.vector.ImageVector

data class Device(
    val id: String,
    val name: String,
    val type: String,
    val isConnected: Boolean,
    val icon: ImageVector,
    val isWearOS: Boolean = false
)
