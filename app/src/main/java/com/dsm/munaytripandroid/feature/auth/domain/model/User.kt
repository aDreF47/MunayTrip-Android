package com.dsm.munaytripandroid.feature.auth.domain.model

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
    val isEmailVerified: Boolean
)