package com.dsm.munaytripandroid.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.feature.offer.data.remote.OfferFirestoreDataSource
import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// NOTA: En una arquitectura DI completa, inyectarías un UseCase o un Repository.
// Por simplicidad y para que coincida con tu código, instanciamos el DataSource aquí.
class ClientHomeViewModel : ViewModel() {

    // Instancia de tu DataSource
    private val offerDataSource = OfferFirestoreDataSource()

    // StateFlow para la lista de ofertas de UI (OfferPreview)
    private val _promotions = MutableStateFlow<List<OfferPreview>>(emptyList())
    val promotions: StateFlow<List<OfferPreview>> = _promotions

    // StateFlow para el estado de carga
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchOffers()
    }

    private fun fetchOffers() {
        viewModelScope.launch {
            _isLoading.value = true
            offerDataSource.getAllOffers() // Llama a tu función de Firestore
                .map { domainOffers ->
                    // Convierte la lista de Offer (dominio) a OfferPreview (UI)
                    domainOffers.map { domainOffer ->
                        mapToOfferPreview(domainOffer)
                    }
                }
                .catch {
                    // Manejar error
                    _isLoading.value = false
                    // Aquí podrías exponer un error a la UI
                }
                .collect { uiOffers ->
                    _promotions.value = uiOffers
                    _isLoading.value = false
                }
        }
    }

    // Función que mapea tu modelo de Dominio (Offer) a tu modelo de UI (OfferPreview)
    private fun mapToOfferPreview(offer: Offer): OfferPreview {
        return OfferPreview(
            id = offer.offerId,
            title = offer.titulo,
            provider = offer.providerId, // Quizás necesites buscar el nombre del proveedor
            price = offer.precio,
            rating = 4.5f, // Deberías agregar 'rating' a tu modelo 'Offer' si existe en Firestore
            distance = 0.0, // La distancia se calculará en la UI (como ya lo haces)
            category = offer.categoria.name.replaceFirstChar { it.titlecase() },
            descuento = offer.descuento,

            // ¡¡ESTA ES LA LÍNEA CLAVE!!
            // Obtiene la primera URL de la lista 'thumbnailUrls' de Firestore
            imageUrl = offer.thumbnailUrls.firstOrNull(),

            lat = offer.ubicacion.lat,
            lng = offer.ubicacion.lng
        )
    }
}