package com.dsm.munaytripandroid.feature.auth.data.remote

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProviderFirestoreDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createProvider(
        providerId: String,
        nombre: String,
        username: String? = null
    ) {
        val providerDoc = mapOf(
            "provider_id" to providerId,
            "nombre" to nombre,
            "username" to username,
            "tipo" to null,
            "categoria" to null,
            "ubicacion" to null,
            "contacto" to null,
            "logo_url" to null,
            "banner_url" to null,
            "created_at" to FieldValue.serverTimestamp()
        )

        firestore.collection("providers")
            .document(providerId)
            .set(providerDoc)
            .await()
    }
}