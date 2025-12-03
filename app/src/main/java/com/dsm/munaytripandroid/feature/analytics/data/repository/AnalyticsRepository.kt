package com.dsm.munaytripandroid.feature.analytics.data.repository

import com.dsm.munaytripandroid.feature.analytics.domain.model.ProviderAnalytics
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

    // ✅ Incrementar vistas (llamar desde OfferDetailViewModel)
    override suspend fun incrementView(offerId: String, searchTerm: String?) {
        try {
            val analyticsDoc = analyticsCollection
                .whereEqualTo("offer_id", offerId)
                .get()
                .await()
                .documents
                .firstOrNull()

            if (analyticsDoc != null) {
                val currentViews = analyticsDoc.getLong("total_views") ?: 0
                val currentSearchTerms = analyticsDoc.get("top_search_terms") as? List<String> ?: emptyList()

                val updates = mutableMapOf<String, Any>(
                    "total_views" to (currentViews + 1),
                    "last_updated" to com.google.firebase.Timestamp.now()
                )

                // Agregar término de búsqueda si existe
                if (searchTerm != null && searchTerm.isNotBlank()) {
                    val updatedTerms = (currentSearchTerms + searchTerm).takeLast(10)
                    updates["top_search_terms"] = updatedTerms
                }

                analyticsDoc.reference.update(updates).await()
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    // ✅ Incrementar clics
    override suspend fun incrementClick(offerId: String) {
        try {
            val analyticsDoc = analyticsCollection
                .whereEqualTo("offer_id", offerId)
                .get()
                .await()
                .documents
                .firstOrNull()

            if (analyticsDoc != null) {
                val currentClicks = analyticsDoc.getLong("total_clicks") ?: 0
                analyticsDoc.reference.update(
                    mapOf(
                        "total_clicks" to (currentClicks + 1),
                        "last_updated" to com.google.firebase.Timestamp.now()
                    )
                ).await()
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    // ✅ Incrementar reservas
    override suspend fun incrementBooking(offerId: String) {
        try {
            val analyticsDoc = analyticsCollection
                .whereEqualTo("offer_id", offerId)
                .get()
                .await()
                .documents
                .firstOrNull()

            if (analyticsDoc != null) {
                val currentBookings = analyticsDoc.getLong("total_bookings") ?: 0
                val currentViews = analyticsDoc.getLong("total_views") ?: 1

                val newBookings = currentBookings + 1
                val conversionRate = (newBookings.toDouble() / currentViews) * 100

                analyticsDoc.reference.update(
                    mapOf(
                        "total_bookings" to newBookings,
                        "conversion_rate" to conversionRate,
                        "last_updated" to com.google.firebase.Timestamp.now()
                    )
                ).await()
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}