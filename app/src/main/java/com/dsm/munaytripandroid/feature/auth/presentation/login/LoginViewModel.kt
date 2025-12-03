package com.dsm.munaytripandroid.feature.auth.presentation.login

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Repository (por ahora sin DI)
    private val authRepository: AuthRepository = AuthRepositoryImpl(
        FirebaseAuthDataSource(),
        FirestoreDataSource(),
        ClientFirestoreDataSource(),
        ProviderFirestoreDataSource()
    )

    // UI State
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Navigation events
    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    // Actions
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClick() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        // Validaciones
        val validationError = validateLoginInput(email, password)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        // Proceso de login
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _loginSuccess.value = true
                }
                is Result.Error -> {
                    val errorMsg = when (result.exception) {
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                            "Contraseña incorrecta"
                        is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
                            "Usuario no encontrado"
                        is com.google.firebase.FirebaseNetworkException ->
                            "Error de conexión. Verifica tu internet"
                        else ->
                            result.exception.message ?: "Error al iniciar sesión"
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                }
                is Result.Loading -> {
                    // No se usa en este flujo
                }
            }
        }
    }

    private fun validateLoginInput(email: String, password: String): String? {
        return when {
            email.isBlank() -> "El correo no puede estar vacío"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Correo electrónico inválido"
            password.isBlank() -> "La contraseña no puede estar vacía"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }
    }

    fun resetLoginSuccess() {
        _loginSuccess.value = false
    }

    fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                user?.let {
                    val fullName = it.displayName ?: ""
                    val parts = fullName.split(" ", limit = 2)
                    val firstName = parts.getOrNull(0) ?: ""
                    val lastName = parts.getOrNull(1) ?: ""
                    val photo = it.photoUrl?.toString()

                    createUserDocument(
                        uid = it.uid,
                        firstName = firstName,
                        lastName = lastName,
                        email = it.email ?: "",
                        photoUrl = photo
                    )
                }
                _uiState.update { it.copy(isLoading = false) }
                _loginSuccess.value = true
            }
            .addOnFailureListener { e ->
                setError("Google Auth failed: ${e.message}")
            }
    }

    private fun createUserDocument(
        uid: String,
        firstName: String,
        lastName: String,
        email: String,
        photoUrl: String? = null
    ) {
        val data = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "photoUrl" to (photoUrl ?: "")
        )
        firestore.collection("users").document(uid)
            .set(data, SetOptions.merge())
    }

    fun setError(msg: String?) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
    }
}