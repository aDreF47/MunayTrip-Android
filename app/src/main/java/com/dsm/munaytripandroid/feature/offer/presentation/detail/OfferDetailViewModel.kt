package com.dsm.munaytripandroid.feature.offer.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.feature.bookings.data.repository.BookingRepositoryImpl
import com.dsm.munaytripandroid.feature.bookings.data.repository.FavoritesRepositoryImpl
import com.dsm.munaytripandroid.feature.bookings.domain.repository.BookingRepository
import com.dsm.munaytripandroid.feature.bookings.domain.repository.FavoritesRepository
import com.dsm.munaytripandroid.feature.offer.data.remote.OfferFirestoreDataSource
import com.dsm.munaytripandroid.feature.offer.data.repository.OfferRepositoryImpl
import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import com.dsm.munaytripandroid.feature.offer.domain.repository.OfferRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OfferDetailViewModel : ViewModel() {

    private val offerRepository: OfferRepository = OfferRepositoryImpl(
        OfferFirestoreDataSource()
    )

    private val bookingRepository: BookingRepository = BookingRepositoryImpl()
    private val favoritesRepository: FavoritesRepository = FavoritesRepositoryImpl()

    private val _uiState = MutableStateFlow(OfferDetailUiState())
    val uiState: StateFlow<OfferDetailUiState> = _uiState.asStateFlow()

    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun loadOffer(offerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val offer = offerRepository.getOfferById(offerId)
                if (offer != null) {
                    // Verificar en paralelo si es favorito y si está reservado
                    val user = currentUser
                    val (isFavorite, isBooked) = if (user != null) {
                        val favoriteDeferred = viewModelScope.async {
                            favoritesRepository.isFavorite(user.uid, offerId)
                        }
                        val bookedDeferred = viewModelScope.async {
                            checkIfBooked(user.uid, offerId)
                        }
                        Pair(favoriteDeferred.await(), bookedDeferred.await())
                    } else {
                        Pair(false, false)
                    }

                    _uiState.value = _uiState.value.copy(
                        offer = offer,
                        isLoading = false,
                        isFavorite = isFavorite,
                        isBooked = isBooked
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

    private suspend fun checkIfBooked(userId: String, offerId: String): Boolean {
        return try {
            val bookings = bookingRepository.getUserBookings(userId)
            bookings.any { it.offerId == offerId && it.status == "confirmed" }
        } catch (e: Exception) {
            false
        }
    }

    fun addToFavorites() {
        viewModelScope.launch {
            val offer = _uiState.value.offer
            val user = currentUser

            if (offer != null && user != null) {
                try {
                    if (_uiState.value.isFavorite) {
                        favoritesRepository.removeFromFavorites(user.uid, offer.offerId)
                        _uiState.value = _uiState.value.copy(isFavorite = false)
                    } else {
                        favoritesRepository.addToFavorites(user.uid, offer.offerId, offer)
                        _uiState.value = _uiState.value.copy(isFavorite = true)
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al ${if (_uiState.value.isFavorite) "quitar de" else "agregar a"} favoritos: ${e.message}"
                    )
                }
            } else if (user == null) {
                _uiState.value = _uiState.value.copy(
                    error = "Debes iniciar sesión para agregar a favoritos"
                )
            }
        }
    }

    fun bookOffer(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val offer = _uiState.value.offer
            val user = currentUser

            if (offer != null && user != null) {
                try {
                    if (offer.cuposDisponibles <= 0) {
                        _uiState.value = _uiState.value.copy(
                            error = "No hay cupos disponibles para esta oferta"
                        )
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(isLoading = true)

                    bookingRepository.createBooking(
                        userId = user.uid,
                        offerId = offer.offerId,
                        offer = offer,
                        status = "confirmed"
                    )

                    val updatedOffer = offer.copy(
                        cuposDisponibles = offer.cuposDisponibles - 1
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isBooked = true,
                        offer = updatedOffer
                    )

                    onSuccess()

                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al realizar reserva: ${e.message}"
                    )
                }
            } else if (user == null) {
                _uiState.value = _uiState.value.copy(
                    error = "Debes iniciar sesión para reservar"
                )
            }
        }
    }
}

data class OfferDetailUiState(
    val offer: Offer? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val isBooked: Boolean = false
)