package com.dsm.munaytripandroid.feature.offer.presentation.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private val MunayPrimary = Color(0xFF1A7FA6)
private val MunaySecondary = Color(0xFF4DB6E8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    offerId: String,
    viewModel: OfferDetailViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onBookOffer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(offerId) {
        viewModel.loadOffer(offerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Oferta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.addToFavorites() }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (uiState.isFavorite) Color.Red else Color.White
                        )
                    }
                    IconButton(onClick = { /* TODO: Compartir */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MunayPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (uiState.offer != null && !uiState.isLoading) {
                BottomAppBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Precio
                        Column {
                            if (uiState.offer!!.esGratis) {
                                Text(
                                    text = "GRATIS",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            } else {
                                val precioFinal = if (uiState.offer!!.descuento > 0) {
                                    uiState.offer!!.precio * (1 - uiState.offer!!.descuento / 100.0)
                                } else {
                                    uiState.offer!!.precio
                                }

                                Text(
                                    text = "S/ ${String.format("%.2f", precioFinal)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MunayPrimary
                                )

                                if (uiState.offer!!.descuento > 0) {
                                    Text(
                                        text = "S/ ${uiState.offer!!.precio}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                }
                            }
                        }

                        // Botón Reservar
                        Button(
                            onClick = onBookOffer,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MunayPrimary
                            ),
                            enabled = uiState.offer!!.cuposDisponibles > 0
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.offer!!.cuposDisponibles > 0) "Reservar" else "Sin cupos",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MunayPrimary
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "😕",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Volver")
                        }
                    }
                }
                uiState.offer != null -> {
                    OfferDetailContent(offer = uiState.offer!!)
                }
            }
        }
    }
}

@Composable
fun OfferDetailContent(offer: com.dsm.munaytripandroid.feature.offer.domain.model.Offer) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Hero Image (placeholder con gradiente)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(MunaySecondary, MunayPrimary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📸",
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }

        // Contenido principal
        item {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MunayPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = offer.categoria.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MunayPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Text(
                            text = offer.tipoOferta.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    if (offer.descuento > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Red.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "-${offer.descuento}% OFF",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Título
                Text(
                    text = offer.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción corta
                Text(
                    text = offer.descripcionCorta,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Info rápida
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickInfoItem(
                        icon = "📍",
                        label = offer.ubicacion.nombre.ifBlank { "Sin ubicación" }
                    )
                    if (offer.duracion.isNotBlank()) {
                        QuickInfoItem(
                            icon = "⏱️",
                            label = offer.duracion
                        )
                    }
                    if (offer.capacidadMaxima > 0) {
                        QuickInfoItem(
                            icon = "👥",
                            label = "${offer.cuposDisponibles}/${offer.capacidadMaxima}"
                        )
                    }
                }
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        // Descripción larga
        if (offer.descripcionLarga.isNotBlank()) {
            item {
                SectionCard(title = "Descripción") {
                    Text(
                        text = offer.descripcionLarga,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        // Incluye
        if (offer.incluye.isNotEmpty()) {
            item {
                SectionCard(title = "Incluye") {
                    offer.incluye.forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "✓",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Recomendaciones
        if (offer.recomendaciones.isNotEmpty()) {
            item {
                SectionCard(title = "Recomendaciones") {
                    offer.recomendaciones.forEach { recomendacion ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "💡")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = recomendacion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Horarios
        if (offer.horarios.isNotEmpty()) {
            item {
                SectionCard(title = "Horarios") {
                    offer.horarios.forEach { horario ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F9FB)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = horario.dia.capitalize(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${horario.horaInicio} - ${horario.horaFin}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // Ubicación
        if (offer.ubicacion.direccion.isNotBlank()) {
            item {
                SectionCard(title = "Ubicación") {
                    Column {
                        val context = LocalContext.current
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MunayPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = offer.ubicacion.nombre,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = offer.ubicacion.direccion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mapa placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(
                                    color = Color(0xFFF5F5F5),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {


                            val markerPosition = LatLng(offer.ubicacion.lat, offer.ubicacion.lng)

                            // ✅ Recordar el estado del marcador
                            val markerState = remember {
                                MarkerState(position = markerPosition)
                            }

                            // ✅ (Opcional) Recordar el estado de la cámara
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(markerPosition, 14f)
                            }

                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState
                            ) {
                                Marker(
                                    state = markerState
                                )
                            }


                        }
                        Button(
                            onClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${offer.ubicacion.lat},${offer.ubicacion.lng}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Abrir en Google Maps")
                        }
                    }
                }
            }
        }

        // Etiquetas
        if (offer.etiquetas.isNotEmpty()) {
            item {
                SectionCard(title = "Etiquetas") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        offer.etiquetas.forEach { etiqueta ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF5F5F5)
                            ) {
                                Text(
                                    text = "#$etiqueta",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Instrucciones de pago
        if (offer.instruccionesPago.isNotBlank()) {
            item {
                SectionCard(title = "Instrucciones de Pago") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF9C4)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💳", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = offer.instruccionesPago,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF827717)
                            )
                        }
                    }
                }
            }
        }

        // Espacio final
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun QuickInfoItem(icon: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MunayPrimary.copy(alpha = 0.1f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            maxLines = 2
        )
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}


/**
 * CARACTERÍSTICAS DE OFFERDETAILSCREEN:
 *
 * ✅ Hero image con placeholder
 * ✅ Badges de categoría, tipo y descuento
 * ✅ Información rápida (ubicación, duración, capacidad)
 * ✅ Descripción completa
 * ✅ Lista de incluidos con checkmarks
 * ✅ Recomendaciones con iconos
 * ✅ Horarios en cards
 * ✅ Ubicación con mapa placeholder
 * ✅ Etiquetas
 * ✅ Instrucciones de pago destacadas
 * ✅ Bottom bar con precio y botón reservar
 * ✅ Botón favorito funcional
 * ✅ Manejo de estados (loading, error, success)
 * ✅ Cálculo de precio con descuento
 * ✅ Validación de cupos disponibles
 */