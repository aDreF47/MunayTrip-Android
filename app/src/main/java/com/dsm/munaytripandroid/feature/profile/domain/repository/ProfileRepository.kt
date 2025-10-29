// feature/profile/domain/repository/ProfileRepository.kt
package com.dsm.munaytripandroid.feature.profile.domain.repository

import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.profile.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getProfile(userId: String): Result<UserProfile>
    suspend fun updateProfileName(userId: String, userType: String, newName: String): Result<Unit>
    suspend fun updateProfilePhoto(userId: String, userType: String, photoUrl: String): Result<Unit>
}