package com.dsm.munaytripandroid.feature.auth.domain.model

/**
 * Excepciones específicas de autenticación
 * Facilita el manejo de errores en la UI
 */
sealed class AuthException(message: String) : Exception(message) {
    object InvalidEmail : AuthException("El correo electrónico no es válido")
    object WeakPassword : AuthException("La contraseña debe tener al menos 6 caracteres")
    object EmailAlreadyInUse : AuthException("Este correo ya está registrado")
    object UserNotFound : AuthException("Usuario no encontrado")
    object WrongPassword : AuthException("Contraseña incorrecta")
    object NetworkError : AuthException("Error de conexión. Verifica tu internet")
    object TooManyRequests : AuthException("Demasiados intentos. Intenta más tarde")
    data class Unknown(val originalMessage: String) : AuthException(originalMessage)
}