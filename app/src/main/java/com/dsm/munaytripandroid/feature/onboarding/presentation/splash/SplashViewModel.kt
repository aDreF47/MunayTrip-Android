package com.dsm.munaytripandroid.feature.onboarding.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    /**
     * Verifica si el usuario tiene una sesión activa
     * Aquí deberías verificar:
     * - Token guardado en DataStore/SharedPreferences
     * - Validez del token
     * - Estado de autenticación
     */
    suspend fun checkUserSession(): Boolean {
        // TODO: Implementar verificación real con tu AuthRepository
        // Por ahora, retorna false (sin sesión)
        delay(500) // Simular verificación
        return false
    }
}