package com.example.climadavidver.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@OptIn(UnstableApi::class)
@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(context: Context): Location? {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    return suspendCancellableCoroutine { cont ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                Log.d("LocationDebug", "Ubicación obtenida: lat=${location?.latitude}, lon=${location?.longitude}")
                cont.resume(location)
            }
            .addOnFailureListener {
                Log.e("LocationDebug", "Error al obtener la ubicación: ${it.message}", it)
                cont.resume(null)
            }
    }
}