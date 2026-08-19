package com.example.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path

// This interface connects to the Python backend
interface SarthiBackendService {
    
    @POST("/api/telemetry")
    suspend fun sendTelemetry(@Body telemetryData: TelemetryData)
    
    @POST("/api/sos")
    suspend fun triggerSOS(@Body sosData: SosRequest)
    
    @GET("/api/profile/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): UserProfile
}

data class TelemetryData(
    val userId: String,
    val gForce: Float,
    val tilt: Float,
    val timestamp: Long
)

data class SosRequest(
    val userId: String,
    val location: LocationData,
    val timestamp: Long
)

data class LocationData(
    val lat: Double,
    val lng: Double
)

data class UserProfile(
    val id: String,
    val name: String,
    val emergencyContact: String,
    val bloodGroup: String
)
