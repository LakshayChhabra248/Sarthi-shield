package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class RiderLocation(
    val latitude: Double = 28.6139, // Default New Delhi / urban benchmark
    val longitude: Double = 77.2090,
    val speedKmh: Float = 36.5f,
    val altitude: Double = 215.0,
    val accuracy: Float = 4.2f,
    val timestamp: Long = System.currentTimeMillis()
)

class LocationTracker(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow(RiderLocation())
    val currentLocation: StateFlow<RiderLocation> = _currentLocation.asStateFlow()

    private var isTracking = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val last = result.lastLocation ?: return
            _currentLocation.value = RiderLocation(
                latitude = last.latitude,
                longitude = last.longitude,
                speedKmh = (last.speed * 3.6f).coerceAtLeast(0f),
                altitude = last.altitude,
                accuracy = last.accuracy,
                timestamp = last.time
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (isTracking) return
        try {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(500L)
                .build()

            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
            isTracking = true

            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    _currentLocation.value = RiderLocation(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        speedKmh = (loc.speed * 3.6f).coerceAtLeast(0f),
                        altitude = loc.altitude,
                        accuracy = loc.accuracy,
                        timestamp = loc.time
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isTracking = false
    }

    /**
     * Calculates geodesic distance in meters using Haversine formula
     */
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun updateSimulatedPosition(lat: Double, lng: Double, speed: Float) {
        _currentLocation.value = RiderLocation(
            latitude = lat,
            longitude = lng,
            speedKmh = speed,
            timestamp = System.currentTimeMillis()
        )
    }
}
