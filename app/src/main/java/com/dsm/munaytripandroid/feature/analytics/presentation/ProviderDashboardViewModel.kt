package com.dsm.munaytripandroid.feature.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.feature.analytics.data.repository.AnalyticsRepository
import com.dsm.munaytripandroid.feature.analytics.data.repository.AnalyticsRepositoryImpl
import com.dsm.munaytripandroid.feature.analytics.domain.model.ProviderAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProviderDashboardViewModel : ViewModel() {

    private val analyticsRepository: AnalyticsRepository = AnalyticsRepositoryImpl()

    private val _analytics = MutableStateFlow<List<ProviderAnalytics>>(emptyList())
    val analytics: StateFlow<List<ProviderAnalytics>> = _analytics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadAnalytics(providerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val analyticsList = analyticsRepository.getAllProviderAnalytics(providerId)
                _analytics.value = analyticsList
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar analytics"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshAnalytics(providerId: String) {
        loadAnalytics(providerId)
    }
}