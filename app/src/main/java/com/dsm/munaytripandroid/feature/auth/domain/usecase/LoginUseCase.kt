package com.dsm.munaytripandroid.feature.auth.domain.usecase

import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.auth.domain.model.User
import com.dsm.munaytripandroid.feature.auth.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {

        if (email.isBlank()) {
            return Result.Error(Exception("El email no puede estar vacío"))
        }

        if (password.isBlank()) {
            return Result.Error(Exception("La contraseña no puede estar vacía"))
        }

        return authRepository.login(email, password)
    }
}