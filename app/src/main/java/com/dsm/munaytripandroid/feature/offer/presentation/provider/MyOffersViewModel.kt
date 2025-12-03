package com.dsm.munaytripandroid.feature.offer.presentation.provider

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.offer.data.remote.OfferFirestoreDataSource
import com.dsm.munaytripandroid.feature.offer.data.repository.OfferRepositoryImpl
import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import com.dsm.munaytripandroid.feature.offer.domain.model.PointsToken
import com.dsm.munaytripandroid.feature.offer.domain.repository.OfferRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MyOffersViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    var isLoading by mutableStateOf(false)

    /**
     * Función para que el Proveedor comparta el link de puntos.
     */
    fun generateAndShareLink(offer: Offer, context: android.content.Context) {
        viewModelScope.launch {
            try {
                isLoading = true
                val providerId = auth.currentUser?.uid ?: return@launch

                // Generamos un ID único corto para el link
                val tokenId = UUID.randomUUID().toString().substring(0, 8)

                val tokenData = PointsToken(
                    id = tokenId,
                    providerId = providerId,
                    offerId = offer.offerId, // CAMBIO: offer.id -> offer.offerId
                    pointsAmount = offer.pointsReward,
                    status = "PENDING",
                    createdAt = FieldValue.serverTimestamp()
                )

                // Guardamos el token en Firestore
                db.collection("puntos_tokens").document(tokenId).set(tokenData).await()

                // Construimos el link
                val deepLink = "munaytrip://redeem?code=$tokenId"

                // Intent para compartir (WhatsApp, Telegram, etc.)
                // CAMBIO: offer.title -> offer.titulo
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "¡Gracias por elegir ${offer.titulo}! \n\nGana ${offer.pointsReward} puntos canjeando este código: $deepLink")
                    type = "text/plain"
                }

                context.startActivity(Intent.createChooser(shareIntent, "Enviar puntos al cliente"))

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    private val offerRepository: OfferRepository = OfferRepositoryImpl(
        OfferFirestoreDataSource()
    )

    private val _uiState = MutableStateFlow(MyOffersUiState())
    val uiState: StateFlow<MyOffersUiState> = _uiState.asStateFlow()

    fun loadMyOffers(providerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            offerRepository.getProviderOffers(providerId).collect { offers ->
                _uiState.value = _uiState.value.copy(
                    offers = offers,
                    filteredOffers = filterOffers(offers, _uiState.value.selectedFilter),
                    isLoading = false
                )
            }
        }
    }

    fun filterOffers(filter: OfferFilter) {
        _uiState.value = _uiState.value.copy(
            selectedFilter = filter,
            filteredOffers = filterOffers(_uiState.value.offers, filter)
        )
    }

    private fun filterOffers(offers: List<Offer>, filter: OfferFilter): List<Offer> {
        return when (filter) {
            OfferFilter.ALL -> offers
            OfferFilter.ACTIVE -> offers.filter { it.estado == "activo" }
            OfferFilter.PAUSED -> offers.filter { it.estado == "pausado" }
            OfferFilter.FINISHED -> offers.filter { it.estado == "finalizado" }
        }
    }

    fun deleteOffer(offerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = offerRepository.deleteOffer(offerId)) {
                is Result.Success -> {
                    // La lista se actualizará automáticamente por el Flow
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Oferta eliminada correctamente"
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message ?: "Error al eliminar"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun pauseOffer(offerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val updates = mapOf("estado" to "pausado")
            when (val result = offerRepository.updateOffer(offerId, updates)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Oferta pausada"
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message ?: "Error al pausar"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun activateOffer(offerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val updates = mapOf("estado" to "activo")
            when (val result = offerRepository.updateOffer(offerId, updates)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Oferta activada"
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exception.message ?: "Error al activar"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}

data class MyOffersUiState(
    val offers: List<Offer> = emptyList(),
    val filteredOffers: List<Offer> = emptyList(),
    val selectedFilter: OfferFilter = OfferFilter.ALL,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

enum class OfferFilter(val displayName: String) {
    ALL("Todas"),
    ACTIVE("Activas"),
    PAUSED("Pausadas"),
    FINISHED("Finalizadas")
}