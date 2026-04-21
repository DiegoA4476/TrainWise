package com.example.trainwise.ui.viewmodels

import android.location.Location
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.trainwise.network.PlacesApiService
import com.example.trainwise.data.models.Gym

class MapViewModel : ViewModel() {
    var gyms by mutableStateOf<List<Gym>>(emptyList())
    var userLocation by mutableStateOf<LatLng?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // For selected gym details
    var selectedGym by mutableStateOf<Gym?>(null)

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(PlacesApiService::class.java)

    fun selectGym(gym: Gym?) {
        selectedGym = gym
    }

    fun fetchNearbyGyms(latLng: LatLng, apiKey: String) {
        Log.d("MapViewModel", "Fetching gyms for: ${latLng.latitude}, ${latLng.longitude}")
        userLocation = latLng
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            try {
                val response = apiService.getNearbyGyms(
                    location = "${latLng.latitude},${latLng.longitude}",
                    apiKey = apiKey,
                    radius = 5000,
                    keyword = "gym",
                    type = "gym"
                )
                
                Log.d("MapViewModel", "API Status: ${response.status}")
                if (response.status == "OK") {
                    val fetchedGyms = response.results.map {
                        val distance = calculateDistance(latLng, LatLng(it.geometry.location.lat, it.geometry.location.lng))
                        Gym(
                            id = it.place_id,
                            name = it.name,
                            rating = it.rating ?: 0.0,
                            address = it.vicinity,
                            location = LatLng(it.geometry.location.lat, it.geometry.location.lng),
                            distance = distance,
                            photoReference = it.photos?.firstOrNull()?.photo_reference
                        )
                    }
                    gyms = fetchedGyms.sortedBy { it.distance }
                } else {
                    errorMessage = when (response.status) {
                        "ZERO_RESULTS" -> "No gyms found near you."
                        "OVER_QUERY_LIMIT" -> "Pay problems"
                        "REQUEST_DENIED" -> "Error: ${response.error_message ?: "Check your API key permissions"}"
                        "INVALID_REQUEST" -> "Invalid request"
                        else -> "Error : ${response.status}"
                    }
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching gyms", e)
                errorMessage = "Error in the net: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0]
    }
}