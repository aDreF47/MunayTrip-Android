package com.dsm.munaytripandroid.feature.bookings.domain.repository

import com.dsm.munaytripandroid.feature.bookings.domain.model.Favorite


interface FavoritesRepository {
    suspend fun addToFavorites(
        userId: String,
        offerId: String,
        offer: com.dsm.munaytripandroid.feature.offer.domain.model.Offer
    )

    suspend fun removeFromFavorites(userId: String, offerId: String)
    suspend fun getUserFavorites(userId: String): List<Favorite>
    suspend fun isFavorite(userId: String, offerId: String): Boolean
}