package com.dsm.munaytripandroid.feature.auth.presentation.register

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "", // 👈 nuevo
    val userType: String = "tourist", // 👈 por defecto "tourist"
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false, // 👈 nuevo
    val username: String = ""          // 👈 Opcional: si quieres mostrarlo
)