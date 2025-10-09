package com.dsm.munaytripandroid.feature.auth.domain.usecase

import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.auth.domain.model.User
import com.dsm.munaytripandroid.feature.auth.domain.repository.AuthRepository

/**
 * Use Case para registro
 * - Valida datos antes de llamar al repository
 * - Lógica de negocio específica
 */
class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String
    ): Result<User> {

        // Validaciones
        if (email.isBlank()) {
            return Result.Error(Exception("El email no puede estar vacío"))
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.Error(Exception("Email inválido"))
        }

        if (password.length < 6) {
            return Result.Error(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        if (displayName.isBlank()) {
            return Result.Error(Exception("El nombre no puede estar vacío"))
        }

        return authRepository.register(email, password, displayName)
    }
}
