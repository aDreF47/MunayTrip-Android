package com.dsm.munaytripandroid.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.dsm.munaytripandroid.feature.client.domain.model.ClientLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.Timestamp
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Manager para obtener ubicación del usuario
 * Usa Google Play Services (FusedLocationProviderClient)
 */
class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Verifica si tenemos permisos de ubicación
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Obtiene la ubicación actual del usuario
     * @return ClientLocation con lat, lng, ciudad y país
     */
    suspend fun getCurrentLocation(): ClientLocation? {
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            val location = getCurrentLocationInternal()
            if (location != null) {
                val address = getAddressFromLocation(location.latitude, location.longitude)
                ClientLocation(
                    lat = location.latitude,
                    lng = location.longitude,
                    ciudad = address.first,
                    pais = address.second,
                    lastUpdated = Timestamp.now()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Obtiene la ubicación usando FusedLocationProvider
     */
    private suspend fun getCurrentLocationInternal(): Location? = suspendCancellableCoroutine { continuation ->
        try {
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                continuation.resume(location)
            }.addOnFailureListener {
                continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        } catch (e: SecurityException) {
            continuation.resume(null)
        }
    }

    /**
     * Convierte coordenadas a nombre de ciudad y país
     * Usa Geocoder (funciona offline con caché)
     */
    private suspend fun getAddressFromLocation(lat: Double, lng: Double): Pair<String, String> =
        suspendCancellableCoroutine { cont ->
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val ciudad = address.locality ?: address.subAdminArea ?: "Desconocido"
                            val pais = address.countryName ?: "Desconocido"
                            cont.resume(Pair(ciudad, pais))
                        } else {
                            cont.resume(Pair("Desconocido", "Desconocido"))
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val ciudad = address.locality ?: address.subAdminArea ?: "Desconocido"
                        val pais = address.countryName ?: "Desconocido"
                        cont.resume(Pair(ciudad, pais))
                        Log.d("LocationManager", "📍 Ciudad: $ciudad | País: $pais")
                    } else {
                        cont.resume(Pair("Desconocido", "Desconocido"))
                    }
                }
            } catch (e: Exception) {
                cont.resume(Pair("Desconocido", "Desconocido"))
            }
        }


}


/**
 * PERMISOS NECESARIOS EN AndroidManifest.xml:
 *
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 *
 * DEPENDENCIA NECESARIA EN build.gradle:
 *
 * implementation("com.google.android.gms:play-services-location:21.0.1")
 */