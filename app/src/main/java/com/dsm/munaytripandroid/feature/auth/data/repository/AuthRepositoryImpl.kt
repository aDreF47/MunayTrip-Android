package com.dsm.munaytripandroid.feature.auth.data.repository

import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.auth.data.remote.FirebaseAuthDataSource
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
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {
        return try {
            val user = firebaseAuthDataSource.registerWithEmail(email, password, displayName)
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val user = firebaseAuthDataSource.loginWithEmail(email, password)
            Result.Success(user)
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
        return firebaseAuthDataSource.getCurrentUser()
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
