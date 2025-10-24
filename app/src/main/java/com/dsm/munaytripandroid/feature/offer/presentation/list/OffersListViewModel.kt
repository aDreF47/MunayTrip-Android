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

    fun searchOffers(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(filteredOffers = _uiState.value.offers)
            } else {
                val results = offerRepository.searchOffers(query)
                _uiState.value = _uiState.value.copy(filteredOffers = results)
            }
        }
    }
}

data class OffersListUiState(
    val offers: List<Offer> = emptyList(),
    val filteredOffers: List<Offer> = emptyList(),
    val selectedCategory: String? = null,
    val isLoading: Boolean = false
)