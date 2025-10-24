package com.dsm.munaytripandroid.feature.offer.domain.repository

import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import kotlinx.coroutines.flow.Flow

interface OfferRepository {
    suspend fun createOffer(offer: Offer, providerId: String): Result<String>
    fun getProviderOffers(providerId: String): Flow<List<Offer>>
    fun getAllOffers(): Flow<List<Offer>>
    fun getOffersByCategory(category: String): Flow<List<Offer>>
    suspend fun getOfferById(offerId: String): Offer?
    suspend fun updateOffer(offerId: String, updates: Map<String, Any>): Result<Unit>
    suspend fun deleteOffer(offerId: String): Result<Unit>
    suspend fun searchOffers(query: String): List<Offer>
}
