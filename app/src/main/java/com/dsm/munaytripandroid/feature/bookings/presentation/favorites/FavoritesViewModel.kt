package com.dsm.munaytripandroid.feature.bookings.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.feature.bookings.data.repository.FavoritesRepositoryImpl
import com.dsm.munaytripandroid.feature.bookings.domain.repository.FavoritesRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository = FavoritesRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun loadFavorites() {
        viewModelScope.launch {
            val user = currentUser
            if (user == null) {
                _uiState.value = _uiState.value.copy(
                    error = "Debes iniciar sesión para ver tus favoritos",
                    isLoading = false
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val favorites = favoritesRepository.getUserFavorites(user.uid)
                _uiState.value = _uiState.value.copy(
                    favorites = favorites,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar favoritos"
                )
            }
        }
    }

    fun removeFromFavorites(offerId: String) {
        viewModelScope.launch {
            val user = currentUser
            if (user != null) {
                try {
                    favoritesRepository.removeFromFavorites(user.uid, offerId)
                    loadFavorites()
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al quitar de favoritos: ${e.message}"
                    )
                }
            }
        }
    }
}

data class FavoritesUiState(
    val favorites: List<com.dsm.munaytripandroid.feature.bookings.domain.model.Favorite> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)