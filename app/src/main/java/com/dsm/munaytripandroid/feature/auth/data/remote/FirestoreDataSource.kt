package com.dsm.munaytripandroid.feature.auth.data.remote

import com.dsm.munaytripandroid.feature.auth.domain.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Data Source para operaciones con Firestore
 * - Crear usuarios
 * - Obtener usuarios
 * - Actualizar usuarios
 */
class FirestoreDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Crear usuario en Firestore
     */
    suspend fun createUser(
        uid: String,
        email: String,
        username: String
    ) {
        val userDoc = hashMapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to username,
            "estado" to "activo",
            "profileImageUrl" to null,
            "photoUrl" to null,
            "isEmailVerified" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .set(userDoc)
            .await()
    }

    /**
     * Obtener usuario desde Firestore
     */
    suspend fun getUser(uid: String): User? {
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            if (doc.exists()) {
                User(
                    uid = doc.getString("uid") ?: "",
                    email = doc.getString("email") ?: "",
                    displayName = doc.getString("username"),
                    photoUrl = doc.getString("photoUrl"),
                    isEmailVerified = doc.getBoolean("isEmailVerified") ?: false,
                    userType = doc.getString("userType") ?: "client",
                    estado = doc.getString("estado") ?: "activo",
                    profileImageUrl = doc.getString("profileImageUrl"),
                    createdAt = doc.getTimestamp("createdAt"),
                    updatedAt = doc.getTimestamp("updatedAt")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Actualizar usuario en Firestore
     */
    suspend fun updateUser(
        uid: String,
        updates: Map<String, Any>
    ) {
        val updatesWithTimestamp = updates.toMutableMap().apply {
            put("updatedAt", FieldValue.serverTimestamp())
        }

        firestore.collection(USERS_COLLECTION)
            .document(uid)
            .update(updatesWithTimestamp)
            .await()
    }

    /**
     * Verificar si usuario existe en Firestore
     */
    suspend fun userExists(uid: String): Boolean {
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
}