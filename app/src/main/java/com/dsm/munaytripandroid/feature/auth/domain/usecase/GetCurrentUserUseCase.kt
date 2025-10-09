package com.dsm.munaytripandroid.feature.auth.domain.usecase

import com.dsm.munaytripandroid.feature.auth.domain.model.User
import com.dsm.munaytripandroid.feature.auth.domain.repository.AuthRepository

class GetCurrentUserUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}