package com.dsm.munaytripandroid.feature.auth.domain.usecase

import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.auth.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}
