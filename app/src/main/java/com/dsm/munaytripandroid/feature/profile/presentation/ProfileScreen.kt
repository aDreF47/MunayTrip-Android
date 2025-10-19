// feature/profile/presentation/ProfileScreen.kt
package com.dsm.munaytripandroid.feature.profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (uiState.userProfile != null) {
                        IconButton(onClick = {
                            if (uiState.isEditing) {
                                viewModel.onSaveProfile()
                            } else {
                                viewModel.toggleEdit()
                            }
                        }) {
                            Icon(
                                imageVector = if (uiState.isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (uiState.isEditing) "Guardar" else "Editar"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Error: ${uiState.errorMessage}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadProfile() }) {
                        Text("Reintentar")
                    }
                }
            }
        } else if (uiState.userProfile != null) {
            ProfileContent(
                userProfile = uiState.userProfile!!,
                isEditing = uiState.isEditing,
                editedName = uiState.editedName,
                onNameChange = viewModel::onEditNameChanged
            )
        }
    }
}

@Composable
fun ProfileContent(
    userProfile: com.dsm.munaytripandroid.feature.profile.domain.model.UserProfile,
    isEditing: Boolean,
    editedName: String,
    onNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Avatar
//        AsyncImage(
//            model = userProfile.avatarUrl ?: "https://via.placeholder.com/150",
//            contentDescription = "Avatar del usuario",
//            modifier = Modifier
//                .size(120.dp)
//                .clip(CircleShape),
//            contentScale = ContentScale.Crop
//        )

        // Nombre
        if (isEditing) {
            OutlinedTextField(
                value = editedName,
                onValueChange = onNameChange,
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            Text(
                text = editedName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Email
        Text(
            text = userProfile.user.email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Tipo de usuario
        val userTypeText = when (userProfile.userType) {
            "tourist" -> "Turista"
            "provider" -> "Proveedor"
            else -> "Usuario"
        }
        Text(
            text = userTypeText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Estado
        if (userProfile.user.estado == "activo") {
            Text(
                text = "Cuenta activa",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary //success
            )
        }
    }
}
