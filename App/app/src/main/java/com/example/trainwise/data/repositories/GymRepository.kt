package com.example.trainwise.data.repositories

import com.example.trainwise.data.models.Gym
import com.example.trainwise.data.models.Review
import com.example.trainwise.network.PlacesApiService
import com.google.android.gms.maps.model.LatLng
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GymRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(PlacesApiService::class.java)

    suspend fun getNearbyGyms(location: LatLng, apiKey: String): List<Gym> {
        val response = apiService.getNearbyGyms(
            location = "${location.latitude},${location.longitude}",
            apiKey = apiKey
        )
        
        if (response.status != "OK") {
            throw Exception(response.error_message ?: "Error from Google Places API: ${response.status}")
        }

        return response.results.map {
            Gym(
                id = it.place_id,
                name = it.name,
                rating = it.rating ?: 0.0,
                address = it.vicinity,
                location = LatLng(it.geometry.location.lat, it.geometry.location.lng),
                photoReference = it.photos?.firstOrNull()?.photo_reference
            )
        }
    }

    suspend fun getGymReviews(placeId: String, apiKey: String): List<Review> {
        val response = apiService.getPlaceDetails(placeId, apiKey = apiKey)
        if (response.status != "OK") return emptyList()
        
        return response.result.reviews?.map {
            Review(
                authorName = it.author_name,
                rating = it.rating,
                text = it.text,
                timeAgo = it.relative_time_description,
                profilePhotoUrl = it.profile_photo_url
            )
        } ?: emptyList()
    }
}
