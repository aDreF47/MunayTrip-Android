package com.dsm.munaytripandroid.feature.client.data.repository

import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.client.domain.model.ClientLocation
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

class ClientRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val CLIENTS_COLLECTION = "clients"
    }

    /**
     * Actualiza la ubicación del cliente en Firestore
     * NO afecta la autenticación ni el documento de users
     */
    suspend fun updateClientLocation(userId: String, location: ClientLocation): Result<Unit> {
        return try {
            val locationMap = hashMapOf(
                "lat" to location.lat,
                "lng" to location.lng,
                "ciudad" to location.ciudad,
                "pais" to location.pais,
                "lastUpdated" to FieldValue.serverTimestamp()
            )

            // Actualizar o crear documento en clients/{userId}
            firestore.collection(CLIENTS_COLLECTION)
                .document(userId)
                .set(
                    mapOf(
                        "clientId" to userId,
                        "ubicacionActual" to locationMap,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    com.google.firebase.firestore.SetOptions.merge() // Merge para no sobrescribir otros campos
                )
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Obtiene la ubicación guardada del cliente
     */
    suspend fun getClientLocation(userId: String): ClientLocation? {
        return try {
            val doc = firestore.collection(CLIENTS_COLLECTION)
                .document(userId)
                .get()
                .await()

            val ubicacionMap = doc.get("ubicacionActual") as? Map<*, *>
            if (ubicacionMap != null) {
                ClientLocation(
                    lat = (ubicacionMap["lat"] as? Double) ?: 0.0,
                    lng = (ubicacionMap["lng"] as? Double) ?: 0.0,
                    ciudad = (ubicacionMap["ciudad"] as? String) ?: "",
                    pais = (ubicacionMap["pais"] as? String) ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}


/**
 * ESTRUCTURA EN FIRESTORE:
 *
 * clients/{userId}/
 * ├─ clientId: userId
 * ├─ nombre: string (opcional)
 * ├─ ubicacionActual: {
 * │   ├─ lat: number
 * │   ├─ lng: number
 * │   ├─ ciudad: string
 * │   ├─ pais: string
 * │   └─ lastUpdated: timestamp
 * └─ updatedAt: timestamp
 *
 * NOTA: Este documento es INDEPENDIENTE de users/{userId}
 * No afecta la autenticación
 */