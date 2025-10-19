// feature/profile/data/repository/ProfileRepositoryImpl.kt
package com.dsm.munaytripandroid.feature.profile.data.repository

import com.dsm.munaytripandroid.core.util.Result
import com.dsm.munaytripandroid.feature.profile.data.remote.FirestoreProfileDataSource
import com.dsm.munaytripandroid.feature.profile.domain.model.UserProfile
import com.dsm.munaytripandroid.feature.profile.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val firestoreProfileDataSource: FirestoreProfileDataSource = FirestoreProfileDataSource()
) : ProfileRepository {

    override suspend fun getProfile(userId: String): Result<UserProfile> {
        return firestoreProfileDataSource.getProfile(userId)
    }

    override suspend fun updateProfileName(userId: String, userType: String, newName: String): Result<Unit> {
        return firestoreProfileDataSource.updateProfileName(userId, userType, newName)
    }
}