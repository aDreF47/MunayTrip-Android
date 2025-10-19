package com.dsm.munaytripandroid.feature.auth.data.repository

import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.auth.data.remote.FirebaseAuthDataSource
import com.dsm.munaytripandroid.feature.auth.data.remote.FirestoreDataSource
import com.dsm.munaytripandroid.feature.auth.data.remote.ProviderFirestoreDataSource
import com.dsm.munaytripandroid.feature.auth.data.remote.TouristFirestoreDataSource
import com.dsm.munaytripandroid.feature.auth.domain.model.User
import com.dsm.munaytripandroid.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del Repository
 * - Delega operaciones al DataSource
 * - Convierte resultados a Result<T>
 * - Capa entre dominio y datos
 */
class AuthRepositoryImpl(
    private val firebaseAuthDataSource: FirebaseAuthDataSource,
    private val firestoreDataSource: FirestoreDataSource, // ⬅ NUEVO PARÁMETRO
    private val touristDataSource: TouristFirestoreDataSource,   // NUEVO
    private val providerDataSource: ProviderFirestoreDataSource // NUEVO
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,     // 👈 Nombre completo (para turista/proveedor)
        userType: String,
        username: String      // 👈 Nombre de usuario único (para users/)
    ): Result<User> {
        return try {
            // 1. Crear en Firebase Auth
            val user = firebaseAuthDataSource.registerWithEmail(email, password, username)

            // 2. Crear en Firestore ⬅ NUEVO
            firestoreDataSource.createUser(
                uid = user.uid,
                email = email,
                username = username
            )

            // 3. Crear en tourists/ o providers/ según el tipo
            when (userType) {
                "tourist" -> {
                    touristDataSource.createTourist(user.uid, fullName, username)
                }
                "provider" -> {
                    providerDataSource.createProvider(user.uid, fullName, username)
                }
                else -> {
                    throw IllegalArgumentException("Tipo de usuario no válido: $userType")
                }
            }

            // 4. Actualizar users/ con el userType correcto
            firestoreDataSource.updateUser(user.uid, mapOf("userType" to userType))

            // 5. Devolver usuario actualizado
            val updatedUser = firestoreDataSource.getUser(user.uid)

            Result.Success(updatedUser ?: user.copy(userType = userType))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            // 1. Login en Firebase Auth
            val user = firebaseAuthDataSource.loginWithEmail(email, password)

            // 2. Obtener datos de Firestore ⬅ NUEVO
            val firestoreUser = firestoreDataSource.getUser(user.uid)

            // 3. Si no existe en Firestore, crearlo (migración)
            if (firestoreUser == null) {
                val username = user.displayName ?: email.substringBefore("@") // fallback seguro
                firestoreDataSource.createUser(
                    uid = user.uid,
                    email = user.email,
                    username = username)
                val newFirestoreUser = firestoreDataSource.getUser(user.uid)
                Result.Success(newFirestoreUser ?: user)
            } else {
                Result.Success(firestoreUser)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuthDataSource.logout()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        val authUser = firebaseAuthDataSource.getCurrentUser() ?: return null

        // Intentar obtener de Firestore primero ⬅ NUEVO
        val firestoreUser = firestoreDataSource.getUser(authUser.uid)
        return firestoreUser ?: authUser
    }

    override fun observeAuthState(): Flow<User?> {
        return firebaseAuthDataSource.observeAuthState()
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return firebaseAuthDataSource.getCurrentUser() != null
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            firebaseAuthDataSource.sendEmailVerification()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuthDataSource.resetPassword(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}