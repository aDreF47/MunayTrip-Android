package com.dsm.munaytripandroid.feature.offer.domain.usecase

import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import com.dsm.munaytripandroid.feature.offer.domain.repository.OfferRepository
import kotlinx.coroutines.flow.Flow

class GetAllOffersUseCase(
    private val offerRepository: OfferRepository
) {
    operator fun invoke(): Flow<List<Offer>> {
        return offerRepository.getAllOffers()
    }
}