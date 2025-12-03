// feature/profile/presentation/ProfileUiState.kt
package com.dsm.munaytripandroid.feature.profile.presentation

import com.dsm.munaytripandroid.feature.profile.domain.model.UserProfile

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEditing: Boolean = false,
    val editedName: String = ""
)