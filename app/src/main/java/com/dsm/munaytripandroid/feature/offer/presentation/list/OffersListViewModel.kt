package com.dsm.munaytripandroid.feature.offer.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.feature.offer.data.remote.OfferFirestoreDataSource
import com.dsm.munaytripandroid.feature.offer.data.repository.OfferRepositoryImpl
import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import com.dsm.munaytripandroid.feature.offer.domain.repository.OfferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class OffersListViewModel : ViewModel() {

    private val offerRepository: OfferRepository = OfferRepositoryImpl(
        OfferFirestoreDataSource()
    )

    private val _uiState = MutableStateFlow(OffersListUiState())
    val uiState: StateFlow<OffersListUiState> = _uiState.asStateFlow()

    init {
        loadOffers()
    }

    private fun loadOffers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            offerRepository.getAllOffers().collect { offers ->
                _uiState.value = _uiState.value.copy(
                    offers = offers,
                    filteredOffers = offers,
                    isLoading = false
                )
            }
        }
    }

    fun filterByCategory(category: String?) {
        val filtered = if (category == null) {
            _uiState.value.offers
        } else {
            _uiState.value.offers.filter { it.categoria.name.lowercase() == category.lowercase() }
        }

        _uiState.value = _uiState.value.copy(
            filteredOffers = filtered,
            selectedCategory = category
        )
    }

    // En OffersListViewModel
    fun searchOffers(query: String) {
        _uiState.update { currentState ->
            val filtered = if (query.isBlank()) {
                // Si no hay búsqueda, aplica solo filtro de categoría
                if (currentState.selectedCategory == null) {
                    currentState.offers  // ✅ CAMBIAR: allOffers → offers
                } else {
                    currentState.offers.filter {  // ✅ CAMBIAR: allOffers → offers
                        it.categoria.name.lowercase() == currentState.selectedCategory
                    }
                }
            } else {
                // Busca en título, descripción y etiquetas
                currentState.offers.filter { offer ->  // ✅ CAMBIAR: allOffers → offers
                    val matchesSearch = offer.titulo.contains(query, ignoreCase = true) ||
                            offer.descripcionCorta.contains(query, ignoreCase = true) ||
                            offer.etiquetas.any { it.contains(query, ignoreCase = true) }

                    val matchesCategory = currentState.selectedCategory == null ||
                            offer.categoria.name.lowercase() == currentState.selectedCategory

                    matchesSearch && matchesCategory
                }
            }

            currentState.copy(
                filteredOffers = filtered,
                searchQuery = query
            )
        }
    }
}

data class OffersListUiState(
    val offers: List<Offer> = emptyList(),
    val filteredOffers: List<Offer> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "", // ← AGREGAR
    val isLoading: Boolean = true,
    val error: String? = null
)