// feature/profile/presentation/ProfileScreen.kt
package com.dsm.munaytripandroid.feature.profile.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

private val MunayPrimary = Color(0xFF1A7FA6)
private val MunaySecondary = Color(0xFF4DB6E8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current

    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // Cargar perfil cuando se inicia el composable o cuando cambia el usuario
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            viewModel.loadProfile()
        } else {
            // No mostrar error inmediatamente, dejar que el ViewModel maneje esto
        }
    }

    // Launcher para seleccionar imagen desde galería
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImageUri ->
            val user = currentUser
            if (user == null) {
                uploadError = "Debes iniciar sesión para subir imágenes"
                return@let
            }

            isUploading = true
            uploadError = null

            // Verificar que el usuario puede editar este perfil
            val canEdit = uiState.userProfile?.user?.uid == user.uid
            if (!canEdit) {
                uploadError = "No tienes permisos para editar este perfil"
                isUploading = false
                return@let
            }

            val storageRef: StorageReference = FirebaseStorage.getInstance().reference
                .child("profile_images/${user.uid}.jpg")

            // Subir archivo con manejo de errores mejorado
            storageRef.putFile(selectedImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Obtener la URL de descarga
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Actualizar Firestore con la nueva URL
                        viewModel.updateProfilePhoto(downloadUri.toString())
                        uploadError = null
                    }.addOnFailureListener { exception ->
                        uploadError = "Error al obtener URL: ${exception.message}"
                        isUploading = false
                    }
                }
                .addOnFailureListener { exception ->
                    uploadError = when {
                        exception.message?.contains("Permission denied") == true ->
                            "Error de permisos. Verifica que las reglas de Storage estén configuradas correctamente."
                        exception.message?.contains("network") == true ->
                            "Error de red. Verifica tu conexión a internet."
                        exception.message?.contains("canceled") == true ->
                            "Subida cancelada."
                        else -> "Error al subir imagen: ${exception.message}"
                    }
                    isUploading = false
                }
        }
    }

    // Observar cambios en el UI state para detectar cuando termina de cargar
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && isUploading) {
            isUploading = false
        }
    }

    // Determinar si el usuario actual puede editar este perfil
    val canEdit = currentUser != null && uiState.userProfile?.user?.uid == currentUser.uid

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Perfil",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (uiState.userProfile != null && canEdit) {
                        // Solo mostrar botones de edición si el usuario puede editar
                        IconButton(
                            onClick = {
                                if (uiState.isEditing) {
                                    viewModel.onSaveProfile()
                                } else {
                                    viewModel.toggleEdit()
                                }
                            },
                            enabled = !uiState.isLoading && !isUploading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = if (uiState.isEditing) Icons.Default.Check else Icons.Default.Edit,
                                    contentDescription = if (uiState.isEditing) "Guardar" else "Editar",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MunayPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F9FB))
        ) {
            when {
                uiState.isLoading || isUploading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MunayPrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = when {
                                    isUploading -> "Subiendo imagen..."
                                    uiState.isLoading -> "Cargando perfil..."
                                    else -> "Procesando..."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MunayPrimary
                            )
                        }
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Error",
                                modifier = Modifier.size(64.dp),
                                tint = MunayPrimary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Error al cargar perfil",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MunayPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (uiState.errorMessage!!.contains("autenticado") || currentUser == null) {
                                Button(
                                    onClick = { onNavigateToLogin() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MunayPrimary
                                    )
                                ) {
                                    Text("Iniciar Sesión")
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.loadProfile() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MunayPrimary
                                    )
                                ) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                }

                uiState.userProfile != null -> {
                    ProfileContent(
                        userProfile = uiState.userProfile!!,
                        isEditing = uiState.isEditing && canEdit,
                        editedName = uiState.editedName,
                        onNameChange = viewModel::onEditNameChanged,
                        onUploadPhoto = {
                            if (canEdit) launcher.launch("image/*")
                            else uploadError = "No tienes permisos para editar este perfil"
                        },
                        isUploading = isUploading,
                        uploadError = uploadError,
                        canEdit = canEdit,
                        onSignOut = {
                            auth.signOut()
                            onNavigateToLogin()
                        },
                        onNavigateToBookings = onNavigateToBookings,
                        onNavigateToFavorites = onNavigateToFavorites
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (currentUser == null) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "No autenticado",
                                    modifier = Modifier.size(64.dp),
                                    tint = MunayPrimary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Inicia sesión para ver tu perfil",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MunayPrimary,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { onNavigateToLogin() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MunayPrimary
                                    )
                                ) {
                                    Text("Iniciar Sesión")
                                }
                            } else {
                                Text(
                                    "No se pudo cargar el perfil",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.loadProfile() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MunayPrimary
                                    )
                                ) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    userProfile: com.dsm.munaytripandroid.feature.profile.domain.model.UserProfile,
    isEditing: Boolean,
    editedName: String,
    onNameChange: (String) -> Unit,
    onUploadPhoto: () -> Unit,
    isUploading: Boolean,
    uploadError: String?,
    canEdit: Boolean,
    onSignOut: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToFavorites: () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con información del usuario
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar con funcionalidad de subida
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    if (userProfile.avatarUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = userProfile.avatarUrl,
                            contentDescription = "Avatar del usuario",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(MunaySecondary, MunayPrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Sin foto de perfil",
                                modifier = Modifier.size(48.dp),
                                tint = Color.White
                            )
                        }
                    }

                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(120.dp),
                            color = MunayPrimary
                        )
                    }
                }

                // Botón para subir foto - SOLO si puede editar
                if (canEdit) {
                    Button(
                        onClick = onUploadPhoto,
                        modifier = Modifier.fillMaxWidth(0.8f),
                        enabled = !isUploading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MunayPrimary
                        )
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Cambiar foto de perfil")
                    }

                    // Mostrar error de subida si existe
                    uploadError?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Nombre - campo editable solo si puede editar
                if (isEditing) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = onNameChange,
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = canEdit,
                    )
                } else {
                    Text(
                        text = editedName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MunayPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                // Email
                Text(
                    text = userProfile.user.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // Badges de información
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Tipo de usuario
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MunayPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = when (userProfile.userType) {
                                "client" -> "Cliente"
                                "provider" -> "Proveedor"
                                else -> "Usuario"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MunayPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Estado
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (userProfile.user.estado) {
                            "activo" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            "inactivo" -> Color.Red.copy(alpha = 0.1f)
                            else -> Color.Gray.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = when (userProfile.user.estado) {
                                "activo" -> "Activo"
                                "inactivo" -> "Inactivo"
                                else -> "Desconocido"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = when (userProfile.user.estado) {
                                "activo" -> Color(0xFF4CAF50)
                                "inactivo" -> Color.Red
                                else -> Color.Gray
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            if (viewModel.isClient) {
                ClientPointsCard(
                    points = viewModel.userPoints
                )
        }

        }
        if (viewModel.isClient) {
            // Opciones del perfil
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mis Reservas
                Card(
                    onClick = onNavigateToBookings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MunayPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        Icons.Default.Bookmark,
                                        contentDescription = "Mis Reservas",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Mis Reservas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Ver y gestionar tus reservas activas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Ver reservas",
                            tint = Color.White
                        )
                    }
                }

                // Mis Favoritos
                ProfileOptionCard(
                    title = "Mis Favoritos",
                    subtitle = "Ofertas que te han gustado",
                    icon = Icons.Default.Favorite,
                    iconColor = Color.Red,
                    onClick = onNavigateToFavorites
                )

            }
        }


        Spacer(modifier = Modifier.weight(1f))

        // Botón cerrar sesión - SOLO si está viendo su propio perfil
        if (canEdit) {
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color.Red
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Cerrar sesión",
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Mensaje informativo si está viendo otro perfil
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Text(
                    text = "Estás viendo el perfil de otro usuario",
                    style = MaterialTheme.typography.bodySmall,
                    color = MunayPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun ClientPointsCard(
    points: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1) // Un color dorado/crema suave
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Mis Puntos Viajeros",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFF57F17), // Dorado oscuro
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¡Úsalos en tu próxima aventura!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Badge de puntos
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFB300), // Dorado brillante
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = points.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            icon,
                            contentDescription = title,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MunayPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Ir a $title",
                tint = MunayPrimary
            )
        }
    }
}