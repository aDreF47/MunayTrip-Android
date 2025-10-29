package com.dsm.munaytripandroid.feature.bookings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.feature.bookings.data.repository.BookingRepositoryImpl
import com.dsm.munaytripandroid.feature.bookings.domain.repository.BookingRepository
import com.dsm.munaytripandroid.feature.offer.domain.model.OfferType
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingsViewModel(
    private val bookingRepository: BookingRepository = BookingRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingsUiState())
    val uiState: StateFlow<BookingsUiState> = _uiState.asStateFlow()

    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun loadBookings() {
        viewModelScope.launch {
            val user = currentUser
            if (user == null) {
                _uiState.value = _uiState.value.copy(
                    error = "Debes iniciar sesión para ver tus reservas",
                    isLoading = false
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val bookings = bookingRepository.getUserBookings(user.uid)
                _uiState.value = _uiState.value.copy(
                    bookings = bookings,
                    filteredBookings = bookings,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar reservas"
                )
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                bookingRepository.cancelBooking(bookingId)
                loadBookings()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al cancelar reserva: ${e.message}"
                )
            }
        }
    }

    fun filterByType(offerType: OfferType?) {
        val filtered = if (offerType == null) {
            _uiState.value.bookings
        } else {
            _uiState.value.bookings.filter { it.offer.tipoOferta == offerType }
        }

        _uiState.value = _uiState.value.copy(
            filteredBookings = filtered,
            selectedType = offerType
        )
    }

    fun filterByStatus(status: String?) {
        val filtered = if (status == null) {
            _uiState.value.bookings
        } else {
            _uiState.value.bookings.filter { it.status == status }
        }

        _uiState.value = _uiState.value.copy(
            filteredBookings = filtered,
            selectedStatus = status
        )
    }

    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            filteredBookings = _uiState.value.bookings,
            selectedType = null,
            selectedStatus = null
        )
    }
}

data class BookingsUiState(
    val bookings: List<com.dsm.munaytripandroid.feature.bookings.domain.model.Booking> = emptyList(),
    val filteredBookings: List<com.dsm.munaytripandroid.feature.bookings.domain.model.Booking> = emptyList(),
    val selectedType: com.dsm.munaytripandroid.feature.offer.domain.model.OfferType? = null,
    val selectedStatus: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)