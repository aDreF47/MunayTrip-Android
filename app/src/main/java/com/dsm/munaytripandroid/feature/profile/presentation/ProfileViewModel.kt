// feature/profile/presentation/ProfileViewModel.kt
package com.dsm.munaytripandroid.feature.profile.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.auth.data.repository.AuthRepositoryImpl
import com.dsm.munaytripandroid.feature.auth.data.remote.FirebaseAuthDataSource
import com.dsm.munaytripandroid.feature.auth.data.remote.FirestoreDataSource
import com.dsm.munaytripandroid.feature.auth.data.remote.ProviderFirestoreDataSource
import com.dsm.munaytripandroid.feature.auth.data.remote.ClientFirestoreDataSource
import com.dsm.munaytripandroid.feature.profile.data.repository.ProfileRepositoryImpl
import com.dsm.munaytripandroid.feature.profile.domain.repository.ProfileRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    var redeemStatus by mutableStateOf<String?>(null)

    // NUEVO: Estado para almacenar los puntos del usuario
    var userPoints by mutableStateOf(0)

    var isClient by mutableStateOf(false)

    init {
        listenToUserPoints()
    }

    // Escucha en tiempo real. Si el campo "points" no existe, asumimos 0.
    private fun listenToUserPoints() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null && snapshot.exists()) {
                    // 1. VERIFICACIÓN DE ROL
                    // Intenta leer el campo "role", "tipo" o "userType". Ajusta según tu BD.
                    val role = snapshot.getString("role") ?: snapshot.getString("userType") ?: snapshot.getString("tipo") ?: ""

                    // Comprobamos si el valor corresponde a un cliente
                    isClient = role.equals("client", ignoreCase = true) ||
                            role.equals("cliente", ignoreCase = true)

                    // 2. SOLO LEER PUNTOS SI ES CLIENTE
                    if (isClient) {
                        // Si el campo es null (no existe), getLong devuelve null y usamos el operador elvis ?: 0
                        val points = snapshot.getLong("points")?.toInt() ?: 0
                        userPoints = points
                    }
                }
            }
    }

    // Esta función se llama cuando el MainActivity detecta el Deep Link
    fun redeemPoints(code: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val tokenRef = db.collection("puntos_tokens").document(code)
            val userRef = db.collection("users").document(userId)

            try {
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(tokenRef)
                    val status = snapshot.getString("status")
                    val points = snapshot.getLong("pointsAmount") ?: 0

                    if (status == "PENDING") {
                        // 1. Marcar código como usado
                        transaction.update(tokenRef, "status", "REDEEMED")
                        transaction.update(tokenRef, "redeemedBy", userId)
                        transaction.update(tokenRef, "redeemedAt", FieldValue.serverTimestamp())

                        // 2. Sumar puntos al usuario (CREACIÓN AUTOMÁTICA)
                        // Usamos set() con SetOptions.merge() en lugar de update().
                        // Esto asegura que si el documento del usuario o el campo "points"
                        // NO existen, se creen en este momento sin dar error.
                        val updateData = mapOf("points" to FieldValue.increment(points))
                        transaction.set(userRef, updateData, SetOptions.merge())

                    } else {
                        throw Exception("Código ya usado o inválido")
                    }
                }.await()

                redeemStatus = "¡Puntos canjeados con éxito!"

            } catch (e: Exception) {
                redeemStatus = "Error: ${e.message}"
            }
        }
    }
    private val authRepository = AuthRepositoryImpl(
        FirebaseAuthDataSource(),
        FirestoreDataSource(),
        ClientFirestoreDataSource(),
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

    fun updateProfilePhoto(photoUrl: String) {
        val userProfile = _uiState.value.userProfile
        if (userProfile != null) {
            _uiState.update { it.copy(isLoading = true) }

            viewModelScope.launch {
                when (val result = profileRepository.updateProfilePhoto(
                    userId = userProfile.user.uid,
                    userType = userProfile.userType,
                    photoUrl = photoUrl
                )) {
                    is Result.Success -> {
                        loadProfile()
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Error al actualizar foto"
                            )
                        }
                    }

                    Result.Loading -> TODO()
                }

            }
        }
    }

}