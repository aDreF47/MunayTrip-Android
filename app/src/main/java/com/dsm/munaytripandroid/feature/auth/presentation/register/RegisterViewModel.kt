package com.dsm.munaytripandroid.feature.auth.presentation.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.auth.data.remote.FirebaseAuthDataSource
import com.dsm.munaytripandroid.feature.auth.data.remote.FirestoreDataSource
import com.dsm.munaytripandroid.feature.auth.data.remote.ProviderFirestoreDataSource
import com.dsm.munaytripandroid.feature.auth.data.remote.ClientFirestoreDataSource
import com.dsm.munaytripandroid.feature.auth.data.repository.AuthRepositoryImpl
import com.dsm.munaytripandroid.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.FirebaseNetworkException

class RegisterViewModel : ViewModel() {

    // Repository
    private val authRepository: AuthRepository = AuthRepositoryImpl(
        FirebaseAuthDataSource(),
        FirestoreDataSource(),
        ClientFirestoreDataSource(),
        ProviderFirestoreDataSource()
    )

    // UI State
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Navigation events
    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess.asStateFlow()

    // Actions
    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    // Alternar entre provider y client
    fun toggleUserType() {
        val newUserType = if (_uiState.value.userType == "provider") "client" else "provider"
        _uiState.update { it.copy(userType = newUserType, errorMessage = null) }
    }

    fun onRegisterClick() {
        val fullName = _uiState.value.name
        val email = _uiState.value.email
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword
        val userType = _uiState.value.userType

        // Generar username único (ej: karla_rodriguez_123)
        val username = generateUniqueUsername(fullName)

        // Validaciones
        val validationError = validateRegisterInput(fullName, email, password, confirmPassword, userType)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        // Proceso de registro
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.register(email, password, fullName, userType,username)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _registerSuccess.value = true
                }
                is Result.Error -> {
                    val errorMsg = when (result.exception) {
                        is FirebaseAuthUserCollisionException ->
                            "Este correo ya está registrado"
                        is FirebaseAuthWeakPasswordException ->
                            "Contraseña muy débil"
                        is FirebaseNetworkException ->
                            "Error de conexión"
                        else ->
                            result.exception.message ?: "Error al crear la cuenta"
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                }
                is Result.Loading -> {
                    // No se usa en este flujo
                }
            }
        }
    }

    private fun validateRegisterInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        userType: String
    ): String? {
        return when {
            name.isBlank() -> "El nombre completo no puede estar vacío"
            email.isBlank() -> "El correo electrónico no puede estar vacío"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Correo electrónico inválido"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            password != confirmPassword -> "Las contraseñas no coinciden"
            userType.isBlank() -> "Selecciona si eres turista o proveedor"
            else -> null
        }
    }

    fun resetRegisterSuccess() {
        _registerSuccess.value = false
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    fun onUserTypeSelected(userType: String) {
        _uiState.update { it.copy(userType = userType, errorMessage = null) }
    }

    private fun generateUniqueUsername(fullName: String): String {
        return if (fullName.isNotBlank()) {
            val base = fullName.trim()
                .lowercase()
                .replace(Regex("[^a-z0-9\\s]"), "")
                .replace(Regex("\\s+"), "_")
            "${base}_${System.currentTimeMillis().toString().takeLast(4)}"
        } else {
            "usuario_${System.currentTimeMillis().toString().takeLast(6)}"
        }
    }


}