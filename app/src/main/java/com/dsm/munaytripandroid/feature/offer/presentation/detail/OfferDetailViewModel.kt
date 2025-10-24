package com.dsm.munaytripandroid.feature.offer.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.feature.offer.data.remote.OfferFirestoreDataSource
import com.dsm.munaytripandroid.feature.offer.data.repository.OfferRepositoryImpl
import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import com.dsm.munaytripandroid.feature.offer.domain.repository.OfferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OfferDetailViewModel : ViewModel() {

    private val offerRepository: OfferRepository = OfferRepositoryImpl(
        OfferFirestoreDataSource()
    )

    private val _uiState = MutableStateFlow(OfferDetailUiState())
    val uiState: StateFlow<OfferDetailUiState> = _uiState.asStateFlow()

    fun loadOffer(offerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val offer = offerRepository.getOfferById(offerId)
                if (offer != null) {
                    _uiState.value = _uiState.value.copy(
                        offer = offer,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Oferta no encontrada"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar oferta"
                )
            }
        }
    }

    fun addToFavorites() {
        // TODO: Implementar en siguiente fase
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isFavorite = !_uiState.value.isFavorite
            )
        }
    }

    fun bookOffer() {
        // TODO: Implementar sistema de reservas en siguiente fase
    }
}

data class OfferDetailUiState(
    val offer: Offer? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false
)