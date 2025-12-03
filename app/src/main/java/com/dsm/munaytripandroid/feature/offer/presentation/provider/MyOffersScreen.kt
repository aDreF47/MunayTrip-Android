package com.dsm.munaytripandroid.feature.offer.presentation.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.munaytripandroid.feature.offer.domain.model.Offer

private val MunayPrimary = Color(0xFF1A7FA6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOffersScreen(
    providerId: String,
    viewModel: MyOffersViewModel = viewModel(),
    onNavigateToCreateOffer: () -> Unit,
    onNavigateToOfferDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(providerId) {
        viewModel.loadMyOffers(providerId)
    }

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mis Ofertas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MunayPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateOffer,
                containerColor = MunayPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear oferta")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filtros
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(OfferFilter.entries) { filter ->
                    FilterChip(
                        selected = uiState.selectedFilter == filter,
                        onClick = { viewModel.filterOffers(filter) },
                        label = { Text(filter.displayName) }
                    )
                }
            }

            HorizontalDivider()

            // Lista de ofertas
            if (uiState.isLoading && uiState.offers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MunayPrimary)
                }
            } else if (uiState.filteredOffers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "📦",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tienes ofertas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Crea tu primera oferta para comenzar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateToCreateOffer,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MunayPrimary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Crear Oferta")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.filteredOffers) { offer ->
                        ProviderOfferCard(
                            offer = offer,
                            viewModel = viewModel,
                            onViewClick = { onNavigateToOfferDetail(offer.offerId) },
                            onEditClick = { /* TODO: Implementar edición */ },
                            onPauseClick = { viewModel.pauseOffer(offer.offerId) },
                            onActivateClick = { viewModel.activateOffer(offer.offerId) },
                            onDeleteClick = { viewModel.deleteOffer(offer.offerId) }
                        )
                    }
                }
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mis Ofertas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE), // Usé un color default por si MunayPrimary no está
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateOffer,
                containerColor = Color(0xFF6200EE),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear oferta")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // ... (Filtros y LazyRow se mantienen igual) ...

            HorizontalDivider()

            // Lista de ofertas
            if (uiState.isLoading && uiState.offers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredOffers.isEmpty()) {
                // ... (Estado vacío se mantiene igual) ...
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes ofertas")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.filteredOffers) { offer ->
                        ProviderOfferCard(
                            offer = offer,
                            viewModel = viewModel, // <--- 1. PASAMOS EL VIEWMODEL AQUÍ
                            onViewClick = { onNavigateToOfferDetail(offer.offerId) },
                            onEditClick = { /* TODO */ },
                            onPauseClick = { viewModel.pauseOffer(offer.offerId) },
                            onActivateClick = { viewModel.activateOffer(offer.offerId) },
                            onDeleteClick = { viewModel.deleteOffer(offer.offerId) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderOfferCard(
    offer: Offer,
    viewModel: MyOffersViewModel,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onPauseClick: () -> Unit,
    onActivateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de estado
                val (estadoColor, estadoText) = when (offer.estado) {
                    "activo" -> Color(0xFF4CAF50) to "Activo"
                    "pausado" -> Color(0xFFFF9800) to "Pausado"
                    "finalizado" -> Color(0xFF9E9E9E) to "Finalizado"
                    else -> Color.Gray to offer.estado
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = estadoColor.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color = estadoColor, shape = RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = estadoText,
                            style = MaterialTheme.typography.labelSmall,
                            color = estadoColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Menú de opciones
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ver detalles") },
                            onClick = {
                                showMenu = false
                                onViewClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )

                        HorizontalDivider()

                        if (offer.estado == "activo") {
                            DropdownMenuItem(
                                text = { Text("Pausar") },
                                onClick = {
                                    showMenu = false
                                    onPauseClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Pause, contentDescription = null)
                                }
                            )
                        } else if (offer.estado == "pausado") {
                            DropdownMenuItem(
                                text = { Text("Activar") },
                                onClick = {
                                    showMenu = false
                                    onActivateClick()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                }
                            )
                        }

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text("Eliminar", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Título
            Text(
                text = offer.titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stats rápidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItem(
                    icon = "👁️",
                    value = "0",
                    label = "Vistas"
                )
                StatItem(
                    icon = "📅",
                    value = "${offer.capacidadMaxima-offer.cuposDisponibles}",
                    label = "Reservas"
                )
                StatItem(
                    icon = "👥",
                    value = "${offer.cuposDisponibles}/${offer.capacidadMaxima}",
                    label = "Cupos"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            // Precio y categoría
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (offer.esGratis) {
                    Text(
                        text = "GRATIS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                } else {
                    Text(
                        text = "S/ ${offer.precio}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MunayPrimary
                    )
                }

                Text(
                    text = offer.categoria.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

            // Este es el componente que definimos en SistemaPuntos.kt
            OfferItemButton(
                offer = offer,
                viewModel = viewModel
            )
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Eliminar oferta",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Estás seguro de que deseas eliminar \"${offer.titulo}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun OfferItemButton(
    offer: Offer,
    viewModel: MyOffersViewModel
) {
    val context = LocalContext.current

    Button(
        onClick = { viewModel.generateAndShareLink(offer, context) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Regalar ${offer.pointsReward} Puntos")
    }
}

@Composable
fun StatItem(icon: String, value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}


/**
 * CARACTERÍSTICAS DE MYOFFERSSCREEN:
 *
 * ✅ Lista de ofertas del provider
 * ✅ Filtros por estado (Todas, Activas, Pausadas, Finalizadas)
 * ✅ Cards con información detallada
 * ✅ Estados visuales con colores
 * ✅ Menú de opciones por oferta
 * ✅ Acciones: Ver, Editar, Pausar, Activar, Eliminar
 * ✅ Diálogo de confirmación para eliminar
 * ✅ Stats rápidos (placeholder para Analytics)
 * ✅ FAB para crear nueva oferta
 * ✅ Estado vacío con CTA
 * ✅ Snackbar para feedback
 * ✅ Loading state
 */