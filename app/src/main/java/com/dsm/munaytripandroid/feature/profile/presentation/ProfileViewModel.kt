// feature/profile/presentation/ProfileViewModel.kt
package com.dsm.munaytripandroid.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.auth.data.repository.AuthRepositoryImpl
import com.dsm.munaytripandroid.feature.auth.data.remote.FirebaseAuthDataSource
import com.dsm.munaytripandroid.feature.auth.data.remote.FirestoreDataSource
import com.dsm.munaytripandroid.feature.auth.data.remote.ProviderFirestoreDataSource
import com.dsm.munaytripandroid.feature.auth.data.remote.TouristFirestoreDataSource
import com.dsm.munaytripandroid.feature.profile.data.repository.ProfileRepositoryImpl
import com.dsm.munaytripandroid.feature.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    // Repositorios (instanciados manualmente, igual que en RegisterViewModel)
    private val authRepository = AuthRepositoryImpl(
        FirebaseAuthDataSource(),
        FirestoreDataSource(),
        TouristFirestoreDataSource(),
        ProviderFirestoreDataSource()
    )

    private val profileRepository: ProfileRepository = ProfileRepositoryImpl()

    // UI State
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val userId = authRepository.getCurrentUser()?.uid
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Usuario no autenticado") }
                return@launch
            }

            when (val result = profileRepository.getProfile(userId)) {
                is Result.Success -> {
                    val userProfile = result.data
                    _uiState.update {
                        it.copy(
                            userProfile = userProfile,
                            editedName = userProfile.fullName,
                            isLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Error al cargar el perfil"
                        )
                    }
                }

                is Result.Loading -> {  }
            }
        }
    }

    fun onEditNameChanged(name: String) {
        _uiState.update { it.copy(editedName = name) }
    }

    fun toggleEdit() {
        _uiState.update { it.copy(isEditing = !it.isEditing) }
    }

    fun onSaveProfile() {
        val userProfile = _uiState.value.userProfile ?: return
        val newName = _uiState.value.editedName

        if (newName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El nombre no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = profileRepository.updateProfileName(
                userId = userProfile.user.uid,
                userType = userProfile.userType,
                newName = newName
            )) {
                is Result.Success -> {
                    // Recargar el perfil actualizado
                    loadProfile()
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Error al guardar cambios"
                        )
                    }
                }
                is Result.Loading -> {  }
            }
        }
    }
}