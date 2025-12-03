package com.dsm.munaytripandroid.feature.analytics.data.repository

import android.util.Log
import com.dsm.munaytripandroid.feature.analytics.domain.model.ImageEngagement
import com.dsm.munaytripandroid.feature.analytics.domain.model.ProviderAnalytics
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

interface AnalyticsRepository {
    suspend fun getProviderAnalytics(providerId: String, offerId: String): ProviderAnalytics?
    fun getProviderAnalyticsFlow(providerId: String, offerId: String): Flow<ProviderAnalytics?>
    suspend fun getAllProviderAnalytics(providerId: String): List<ProviderAnalytics>
    suspend fun updateAnalytics(analyticsId: String, updates: Map<String, Any>)
    suspend fun incrementView(offerId: String, searchTerm: String?)
    suspend fun incrementClick(offerId: String)
    suspend fun incrementBooking(offerId: String)
}

class AnalyticsRepositoryImpl : AnalyticsRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val analyticsCollection = firestore.collection("provider_analytics")

    override suspend fun getProviderAnalytics(
        providerId: String,
        offerId: String
    ): ProviderAnalytics? {
        return try {
            val snapshot = analyticsCollection
                .whereEqualTo("provider_id", providerId)
                .whereEqualTo("offer_id", offerId)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(ProviderAnalytics::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun getProviderAnalyticsFlow(
        providerId: String,
        offerId: String
    ): Flow<ProviderAnalytics?> = flow {
        analyticsCollection
            .whereEqualTo("provider_id", providerId)
            .whereEqualTo("offer_id", offerId)
            .addSnapshotListener { snapshot, _ ->
                val analytics = snapshot?.documents?.firstOrNull()
                    ?.toObject(ProviderAnalytics::class.java)
                // emit(analytics) - necesitarías un CallbackFlow para esto
            }
    }

    override suspend fun getAllProviderAnalytics(
        providerId: String
    ): List<ProviderAnalytics> {
        return try {
            val snapshot = analyticsCollection
                .whereEqualTo("provider_id", providerId)
                .get()
                .await()

            snapshot.toObjects(ProviderAnalytics::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateAnalytics(
        analyticsId: String,
        updates: Map<String, Any>
    ) {
        try {
            analyticsCollection.document(analyticsId)
                .update(updates)
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }


    // ✅ NUEVO: Crear documento de analytics si no existe
    private suspend fun getOrCreateAnalyticsDoc(offerId: String): String {
        return try {
            // 1️⃣ Buscar documento existente
            val existingDoc = analyticsCollection
                .whereEqualTo("offer_id", offerId)
                .limit(1) // ✅ AGREGAR LIMIT
                .get()
                .await()
                .documents
                .firstOrNull()

            if (existingDoc != null) {
                Log.d("Analytics", "✅ Documento encontrado: ${existingDoc.id}")
                return existingDoc.id
            }

            // 2️⃣ Si no existe, crear uno nuevo
            Log.d("Analytics", "⚠️ No existe documento, creando uno nuevo para: $offerId")

            val offerDoc = firestore.collection("offers")
                .document(offerId)
                .get()
                .await()

            val providerId = offerDoc.getString("providerId") ?: "unknown"

            val newAnalytics = hashMapOf(
                "analytics_id" to "",
                "provider_id" to providerId,
                "offer_id" to offerId,
                "total_views" to 0,
                "total_clicks" to 0,
                "total_bookings" to 0,
                "total_attendance" to 0,
                "total_interest" to 0,
                "conversion_rate" to 0.0,
                "image_engagement" to hashMapOf(
                    "total_image_views" to 0,
                    "avg_time_per_image" to 0.0,
                    "most_viewed_image" to ""
                ),
                "top_search_terms" to emptyList<String>(),
                "peak_hours" to emptyList<Int>(),
                "period" to "monthly",
                "last_updated" to Timestamp.now()
            )

            val docRef = analyticsCollection.add(newAnalytics).await()
            analyticsCollection.document(docRef.id).update("analytics_id", docRef.id).await()

            Log.d("Analytics", "✅ Documento creado: ${docRef.id} para oferta $offerId")

            docRef.id

        } catch (e: Exception) {
            Log.e("Analytics", "❌ Error: ${e.message}")
            throw e
        }
    }




    // ✅ ACTUALIZADO: Incrementar vistas
    override suspend fun incrementView(offerId: String, searchTerm: String?) {
        try {
            // Obtener o crear el documento
            val docId = getOrCreateAnalyticsDoc(offerId)
            val analyticsDoc = analyticsCollection.document(docId).get().await()

            val currentViews = analyticsDoc.getLong("total_views") ?: 0
            val currentSearchTerms = analyticsDoc.get("top_search_terms") as? List<String> ?: emptyList()

            val updates = mutableMapOf<String, Any>(
                "total_views" to (currentViews + 1),
                "last_updated" to Timestamp.now()
            )

            // Agregar término de búsqueda si existe
            if (!searchTerm.isNullOrBlank()) {
                // Agregar y contar frecuencia
                val updatedTerms = (currentSearchTerms + searchTerm)
                    .groupBy { it }
                    .mapValues { it.value.size }
                    .entries
                    .sortedByDescending { it.value }
                    .take(10)
                    .map { it.key }

                updates["top_search_terms"] = updatedTerms
            }

            analyticsCollection.document(docId).update(updates).await()

            Log.d("Analytics", "✅ Vista incrementada: $offerId (total: ${currentViews + 1})")

        } catch (e: Exception) {
            Log.e("Analytics", "❌ Error al incrementar vista: ${e.message}")
        }
    }

    // ✅ ACTUALIZADO: Incrementar clics
    override suspend fun incrementClick(offerId: String) {
        try {
            val docId = getOrCreateAnalyticsDoc(offerId)
            val analyticsDoc = analyticsCollection.document(docId).get().await()

            val currentClicks = analyticsDoc.getLong("total_clicks") ?: 0

            analyticsCollection.document(docId).update(
                mapOf(
                    "total_clicks" to (currentClicks + 1),
                    "last_updated" to Timestamp.now()
                )
            ).await()

            Log.d("Analytics", "✅ Clic incrementado: $offerId (total: ${currentClicks + 1})")

        } catch (e: Exception) {
            Log.e("Analytics", "❌ Error al incrementar clic: ${e.message}")
        }
    }

    // ✅ ACTUALIZADO: Incrementar reservas
    override suspend fun incrementBooking(offerId: String) {
        try {
            val docId = getOrCreateAnalyticsDoc(offerId)
            val analyticsDoc = analyticsCollection.document(docId).get().await()

            val currentBookings = analyticsDoc.getLong("total_bookings") ?: 0
            val currentViews = analyticsDoc.getLong("total_views") ?: 1

            val newBookings = currentBookings + 1
            val conversionRate = (newBookings.toDouble() / currentViews) * 100

            analyticsCollection.document(docId).update(
                mapOf(
                    "total_bookings" to newBookings,
                    "conversion_rate" to conversionRate,
                    "last_updated" to Timestamp.now()
                )
            ).await()

            Log.d("Analytics", "✅ Reserva incrementada: $offerId (conversión: ${String.format("%.1f", conversionRate)}%)")

        } catch (e: Exception) {
            Log.e("Analytics", "❌ Error al incrementar reserva: ${e.message}")
        }
    }


}