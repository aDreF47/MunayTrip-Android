package com.dsm.munaytripandroid.feature.auth.data.remote

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TouristFirestoreDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createTourist(
        touristId: String,
        nombre: String,
        username: String? = null
    ) {
        val touristDoc = mapOf(
            "tourist_id" to touristId,
            "nombre" to nombre,
            "username" to username,       // 👈 Opcional: si quieres referenciarlo
            "preferencias" to emptyList<String>(),
            "puntos" to 0,
            "nivel" to 1,
            "badges" to emptyList<String>(),
            "avatar_url" to null,
            "ubicacion_actual" to null,
            "created_at" to FieldValue.serverTimestamp()
        )

        firestore.collection("tourists")
            .document(touristId)
            .set(touristDoc)
            .await()
    }
}