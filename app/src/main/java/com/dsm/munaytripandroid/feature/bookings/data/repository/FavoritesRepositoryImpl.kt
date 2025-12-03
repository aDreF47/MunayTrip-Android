package com.dsm.munaytripandroid.feature.bookings.data.repository

import com.dsm.munaytripandroid.feature.bookings.domain.model.Favorite
import com.dsm.munaytripandroid.feature.bookings.domain.repository.FavoritesRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoritesRepositoryImpl : FavoritesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val favoritesCollection = db.collection("favorites")

    override suspend fun addToFavorites(
        userId: String,
        offerId: String,
        offer: com.dsm.munaytripandroid.feature.offer.domain.model.Offer
    ) {
        val favorite = Favorite(
            id = "$userId-$offerId",
            userId = userId,
            offerId = offerId,
            offer = offer
        )

        favoritesCollection
            .document(favorite.id)
            .set(favorite)
            .await()
    }

    override suspend fun removeFromFavorites(userId: String, offerId: String) {
        favoritesCollection
            .document("$userId-$offerId")
            .delete()
            .await()
    }

    override suspend fun getUserFavorites(userId: String): List<Favorite> {
        return try {
            val result = favoritesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            result.documents.mapNotNull { document ->
                try {
                    document.toObject(Favorite::class.java)
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.addedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun isFavorite(userId: String, offerId: String): Boolean {
        return try {
            val document = favoritesCollection
                .document("$userId-$offerId")
                .get()
                .await()
            document.exists()
        } catch (e: Exception) {
            false
        }
    }
}