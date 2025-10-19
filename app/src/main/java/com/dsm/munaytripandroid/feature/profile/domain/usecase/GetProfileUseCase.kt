package com.dsm.munaytripandroid.feature.profile.domain.usecase

import com.dsm.munaytripandroid.feature.profile.domain.repository.ProfileRepository

// feature/profile/domain/usecase/GetProfileUseCase.kt
class GetProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String) = profileRepository.getProfile(userId)
}

// feature/profile/domain/usecase/UpdateProfileUseCase.kt
class UpdateProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String, userType: String, newName: String) {
        profileRepository.updateProfileName(userId, userType, newName)
    }
}