package com.dsm.munaytripandroid.feature.auth.data.remote

import com.dsm.munaytripandroid.feature.auth.domain.model.AuthException
import com.dsm.munaytripandroid.feature.auth.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Data Source de Firebase Auth
 * - Acceso directo a Firebase
 * - Convierte FirebaseUser a User del dominio
 * - Maneja excepciones de Firebase
 */
class FirebaseAuthDataSource(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /**
     * Registra usuario con email y contraseña
     */
    suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): User {
        try {
            // Crear usuario en Firebase
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw AuthException.Unknown("Usuario nulo")

            // Actualizar nombre de usuario
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Enviar verificación de email
            // firebaseUser.sendEmailVerification().await()

            return firebaseUser.toDomainUser()

        } catch (e: FirebaseAuthException) {
            throw e.toAuthException()
        } catch (e: Exception) {
            throw AuthException.Unknown(e.message ?: "Error desconocido")
        }
    }

    /**
     * Inicia sesión con email y contraseña
     */
    suspend fun loginWithEmail(email: String, password: String): User {
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw AuthException.Unknown("Usuario nulo")
            return firebaseUser.toDomainUser()

        } catch (e: FirebaseAuthException) {
            throw e.toAuthException()
        } catch (e: Exception) {
            throw AuthException.Unknown(e.message ?: "Error desconocido")
        }
    }

    /**
     * Cierra sesión
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Obtiene usuario actual
     */
    fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.toDomainUser()
    }

    /**
     * Observa cambios en autenticación
     */
    fun observeAuthState(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toDomainUser())
        }
        firebaseAuth.addAuthStateListener(listener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    /**
     * Envía email de verificación
     */
    suspend fun sendEmailVerification() {
        try {
            firebaseAuth.currentUser?.sendEmailVerification()?.await()
                ?: throw AuthException.Unknown("Usuario no autenticado")
        } catch (e: Exception) {
            throw AuthException.Unknown(e.message ?: "Error al enviar verificación")
        }
    }

    /**
     * Restablece contraseña
     */
    suspend fun resetPassword(email: String) {
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
        } catch (e: FirebaseAuthException) {
            throw e.toAuthException()
        } catch (e: Exception) {
            throw AuthException.Unknown(e.message ?: "Error desconocido")
        }
    }

    // ========== CONVERSIONES ==========

    /**
     * Convierte FirebaseUser a User del dominio
     */
    private fun FirebaseUser.toDomainUser(): User {
        return User(
            uid = uid,
            email = email ?: "",
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isEmailVerified = isEmailVerified
        )
    }

    /**
     * Convierte excepciones de Firebase a excepciones del dominio
     */
    private fun FirebaseAuthException.toAuthException(): AuthException {
        return when (errorCode) {
            "ERROR_INVALID_EMAIL" -> AuthException.InvalidEmail
            "ERROR_WEAK_PASSWORD" -> AuthException.WeakPassword
            "ERROR_EMAIL_ALREADY_IN_USE" -> AuthException.EmailAlreadyInUse
            "ERROR_USER_NOT_FOUND" -> AuthException.UserNotFound
            "ERROR_WRONG_PASSWORD" -> AuthException.WrongPassword
            "ERROR_NETWORK_REQUEST_FAILED" -> AuthException.NetworkError
            "ERROR_TOO_MANY_REQUESTS" -> AuthException.TooManyRequests
            else -> AuthException.Unknown(message ?: "Error desconocido")
        }
    }
}