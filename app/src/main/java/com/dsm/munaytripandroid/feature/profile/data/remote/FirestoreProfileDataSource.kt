// feature/profile/data/remote/FirestoreProfileDataSource.kt
package com.dsm.munaytripandroid.feature.profile.data.remote

import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.auth.data.remote.FirestoreDataSource
import com.dsm.munaytripandroid.feature.profile.domain.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreProfileDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val userDataSource: FirestoreDataSource = FirestoreDataSource()
) {

    suspend fun getProfile(userId: String): Result<UserProfile> {
        return try {
            // 1. Obtener usuario base
            val user = userDataSource.getUser(userId) ?: throw Exception("Usuario no encontrado")

            // 2. Obtener datos específicos según tipo
            val (fullName, avatarUrl) = when (user.userType) {
                "client" -> {
                    val clientDoc = firestore.collection("clients").document(userId).get().await()
                    val name = clientDoc.getString("nombre") ?: user.displayName ?: ""
                    val avatar = clientDoc.getString("avatar_url")
                    name to avatar
                }
                "provider" -> {
                    val providerDoc = firestore.collection("providers").document(userId).get().await()
                    val name = providerDoc.getString("nombre") ?: user.displayName ?: ""
                    val avatar = providerDoc.getString("logo_url")
                    name to avatar
                }
                else -> {
                    (user.displayName ?: "") to null
                }
            }

            Result.Success(
                UserProfile(
                    user = user,
                    fullName = fullName,
                    userType = user.userType,
                    avatarUrl = avatarUrl
                )
            )
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateProfileName(userId: String, userType: String, newName: String): Result<Unit> {
        return try {
            val collection = when (userType) {
                "client" -> "clients"
                "provider" -> "providers"
                else -> throw IllegalArgumentException("Tipo de usuario no válido")
            }

            firestore.collection(collection)
                .document(userId)
                .update("nombre", newName)
                .await()

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}