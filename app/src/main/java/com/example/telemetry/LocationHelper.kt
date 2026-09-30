package com.example.telemetry

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

data class CurrentLocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: String
)

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    fun formatUtcTimestamp(date: Date = Date()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    fun isValidLocation(latitude: Double, longitude: Double, accuracy: Float): Boolean {
        return latitude in -90.0..90.0 &&
                longitude in -180.0..180.0 &&
                accuracy >= 0f
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): CurrentLocationData? {
        if (!hasLocationPermission(context)) return null

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            var loc: Location? = suspendCancellableCoroutine { continuation ->
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { result ->
                        if (continuation.isActive) continuation.resume(result)
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            }

            if (loc == null) {
                loc = suspendCancellableCoroutine { continuation ->
                    fusedClient.lastLocation
                        .addOnSuccessListener { result ->
                            if (continuation.isActive) continuation.resume(result)
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }
                }
            }

            if (loc == null) {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val gpsLoc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val netLoc = lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                loc = gpsLoc ?: netLoc
            }

            if (loc != null) {
                val lat = loc.latitude
                val lng = loc.longitude
                val acc = loc.accuracy

                if (isValidLocation(lat, lng, acc)) {
                    val date = if (loc.time > 0) Date(loc.time) else Date()
                    return CurrentLocationData(
                        latitude = lat,
                        longitude = lng,
                        accuracy = acc,
                        timestamp = formatUtcTimestamp(date)
                    )
                }
            }
        } catch (e: Exception) {
            // Permission or hardware unavailable
        }
        return null
    }
}
