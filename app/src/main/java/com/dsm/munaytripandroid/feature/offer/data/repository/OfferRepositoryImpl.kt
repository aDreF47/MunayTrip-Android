package com.dsm.munaytripandroid.feature.offer.data.repository

import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.offer.data.remote.OfferFirestoreDataSource
import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import com.dsm.munaytripandroid.feature.offer.domain.repository.OfferRepository
import kotlinx.coroutines.flow.Flow

class OfferRepositoryImpl(
    private val offerDataSource: OfferFirestoreDataSource
) : OfferRepository {

    override suspend fun createOffer(offer: Offer, providerId: String): Result<String> {
        return try {
            val offerId = offerDataSource.createOffer(offer, providerId)
            Result.Success(offerId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getProviderOffers(providerId: String): Flow<List<Offer>> {
        return offerDataSource.getProviderOffers(providerId)
    }

    override fun getAllOffers(): Flow<List<Offer>> {
        return offerDataSource.getAllOffers()
    }

    override fun getOffersByCategory(category: String): Flow<List<Offer>> {
        return offerDataSource.getOffersByCategory(category)
    }

    override suspend fun getOfferById(offerId: String): Offer? {
        return offerDataSource.getOfferById(offerId)
    }

    override suspend fun updateOffer(offerId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            offerDataSource.updateOffer(offerId, updates)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteOffer(offerId: String): Result<Unit> {
        return try {
            offerDataSource.deleteOffer(offerId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun searchOffers(query: String): List<Offer> {
        return offerDataSource.searchOffers(query)
    }
}
