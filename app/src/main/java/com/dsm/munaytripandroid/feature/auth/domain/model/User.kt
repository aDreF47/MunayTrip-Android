package com.dsm.munaytripandroid.feature.auth.domain.model

import com.google.firebase.Timestamp

/**
 * Modelo de usuario del DOMINIO
 * - Independiente de Firebase
 * - Solo contiene lo que la app necesita
 */
data class User(
    val uid: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val userType: String = "client",      // "client" o "provider"
    val estado: String = "activo",         // "activo", "inactivo", "suspendido"
    val profileImageUrl: String? = null,   // Foto de perfil
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)