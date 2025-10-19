// feature/profile/domain/model/UserProfile.kt
package com.dsm.munaytripandroid.feature.profile.domain.model

import com.dsm.munaytripandroid.feature.auth.domain.model.User

data class UserProfile(
    val user: User,
    val fullName: String,
    val userType: String,
    val avatarUrl: String? = null
)