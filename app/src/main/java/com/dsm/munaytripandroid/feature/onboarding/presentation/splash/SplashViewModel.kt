package com.dsm.munaytripandroid.feature.onboarding.presentation.splash

//import androidx.lifecycle.ViewModel
//import com.dsm.munaytripandroid.feature.auth.domain.repository.AuthRepository
//
//class SplashViewModel(
//    private val authRepository: AuthRepository
//) : ViewModel() {
//
//    /**
//     * Verifica si el usuario tiene una sesión activa
//     * Ahora con Firebase real
//     */
//    suspend fun checkUserSession(): Boolean {
//        return authRepository.isUserLoggedIn()
//    }
//}


import androidx.lifecycle.ViewModel
import com.dsm.munaytripandroid.feature.auth.data.remote.FirebaseAuthDataSource
import com.dsm.munaytripandroid.feature.auth.data.repository.AuthRepositoryImpl
import com.dsm.munaytripandroid.feature.auth.domain.repository.AuthRepository

class SplashViewModel : ViewModel() {

    // Crear dependencias manualmente (temporal)
    private val authRepository: AuthRepository = AuthRepositoryImpl(
        FirebaseAuthDataSource()
    )

    /**
     * Verifica si el usuario tiene una sesión activa
     */
    suspend fun checkUserSession(): Boolean {
        return authRepository.isUserLoggedIn()
    }
}

