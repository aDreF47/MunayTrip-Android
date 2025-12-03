package com.dsm.munaytripandroid.feature.auth.domain.repository

import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface del Repository (DOMINIO)
 * Define QUÉ operaciones están disponibles, no CÓMO se hacen
 */
interface AuthRepository {

    /**
     * Registra un nuevo usuario con email y contraseña
     */
    suspend fun register(email: String, password: String, displayName: String, userType: String, username: String): Result<User>

    /**
     * Inicia sesión con email y contraseña
     */
    suspend fun login(email: String, password: String): Result<User>

    /**
     * Cierra la sesión del usuario actual
     */
    suspend fun logout(): Result<Unit>

    /**
     * Obtiene el usuario actualmente autenticado
     */
    suspend fun getCurrentUser(): User?

    /**
     * Observa cambios en el estado de autenticación
     */
    fun observeAuthState(): Flow<User?>

    /**
     * Verifica si hay un usuario con sesión activa
     */
    suspend fun isUserLoggedIn(): Boolean

    /**
     * Envía email de verificación
     */
    suspend fun sendEmailVerification(): Result<Unit>

    /**
     * Restablece contraseña
     */
    suspend fun resetPassword(email: String): Result<Unit>
}