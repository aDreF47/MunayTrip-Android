package com.dsm.munaytripandroid.feature.client.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

/**
 * Modelo de Cliente (Client)
 * Extiende la información del User base
 */
data class Client(
    val clientId: String = "",
    val nombre: String = "",
    val preferencias: List<String> = emptyList(),
    val puntos: Int = 0,
    val nivel: Int = 1,
    val badges: List<String> = emptyList(),
    val avatarUrl: String? = null,
    val ubicacionActual: ClientLocation? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class ClientLocation(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val ciudad: String = "",
    val pais: String = "",
    val lastUpdated: Timestamp? = null
)

/**
 * NOTAS:
 * - ubicacionActual es OPCIONAL (nullable)
 * - Se puede agregar después del registro
 * - Se actualiza cada vez que el usuario permita
 * - lastUpdated para saber si está desactualizada
 */