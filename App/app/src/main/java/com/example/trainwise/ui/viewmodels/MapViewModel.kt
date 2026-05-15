package com.example.trainwise.ui.viewmodels

import android.location.Location
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import com.example.trainwise.data.models.Gym
import com.example.trainwise.data.repositories.GymRepository

class MapViewModel(
    private val repository: GymRepository = GymRepository()
) : ViewModel() {
    var gyms by mutableStateOf<List<Gym>>(emptyList())
    var userLocation by mutableStateOf<LatLng?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    var selectedGym by mutableStateOf<Gym?>(null)

    fun selectGym(gym: Gym?) {
        selectedGym = gym
    }

    fun fetchNearbyGyms(latLng: LatLng, apiKey: String) {
        userLocation = latLng
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            try {
                val fetchedGyms = repository.getNearbyGyms(latLng, apiKey)
                gyms = fetchedGyms.map { gym ->
                    val distance = calculateDistance(latLng, gym.location)
                    gym.copy(distance = distance)
                }.sortedBy { it.distance }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching gyms", e)
                errorMessage = e.localizedMessage ?: "Error fetching gyms"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchGymDetails(placeId: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val reviews = repository.getGymReviews(placeId, apiKey)
                
                selectedGym = selectedGym?.copy(reviews = reviews)
                
                gyms = gyms.map {
                    if (it.id == placeId) it.copy(reviews = reviews) else it
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching place details", e)
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
