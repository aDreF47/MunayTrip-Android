package com.dsm.munaytripandroid.feature.offer.domain.usecase

import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import com.dsm.munaytripandroid.feature.offer.domain.repository.OfferRepository
import com.dsm.munaytripandroid.core.util.Result

class CreateOfferUseCase(
    private val offerRepository: OfferRepository
) {
    suspend operator fun invoke(offer: Offer, providerId: String): Result<String> {
        // Validaciones
        if (offer.titulo.isBlank()) {
            return Result.Error(Exception("El título es obligatorio"))
        }
        if (offer.descripcionCorta.isBlank()) {
            return Result.Error(Exception("La descripción es obligatoria"))
        }
        if (offer.precio < 0) {
            return Result.Error(Exception("El precio no puede ser negativo"))
        }

        return offerRepository.createOffer(offer, providerId)
    }
}