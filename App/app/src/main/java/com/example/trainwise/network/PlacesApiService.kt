package com.example.trainwise.network

import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyGyms(
        @Query("location") location: String,
        @Query("radius") radius: Int = 5000,
        @Query("type") type: String? = "gym",
        @Query("keyword") keyword: String? = "gym",
        @Query("key") apiKey: String
    ): PlacesResponse
}

data class PlacesResponse(
    val results: List<PlaceResult>,
    val status: String,
    val error_message: String?
)

data class PlaceResult(
    val place_id: String,
    val name: String,
    val rating: Double?,
    val vicinity: String,
    val geometry: Geometry
)

data class Geometry(val location: LocationData)
data class LocationData(val lat: Double, val lng: Double)