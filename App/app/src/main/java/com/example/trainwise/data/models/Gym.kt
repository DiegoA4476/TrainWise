package com.example.trainwise.data.models

import com.google.android.gms.maps.model.LatLng

data class Gym(
    val id: String,
    val name: String,
    val rating: Double,
    val address: String,
    val location: LatLng,
    val isOpenNow: Boolean = true,
    val distance: Float? = null
)