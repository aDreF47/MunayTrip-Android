package com.dsm.munaytripandroid.feature.offer.presentation.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.munaytripandroid.feature.offer.domain.model.Offer
import com.dsm.munaytripandroid.feature.offer.domain.model.OfferCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffersListScreen(
    viewModel: OffersListViewModel = viewModel(),
    onOfferClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ofertas Disponibles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, "Buscar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A7FA6),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Filtros por categoría
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.filterByCategory(null) },
                        label = { Text("Todos") }
                    )
                }

                items(OfferCategory.entries) { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category.name.lowercase(),
                        onClick = { viewModel.filterByCategory(category.name.lowercase()) },
                        label = { Text(category.displayName) }
                    )
                }
            }

            HorizontalDivider()

            // Lista de ofertas
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1A7FA6))
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
                            text = "📭",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay ofertas disponibles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Intenta con otra categoría o vuelve más tarde",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.filteredOffers) { offer ->
                        OfferCard(
                            offer = offer,
                            onClick = { onOfferClick(offer.offerId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OfferCard(
    offer: Offer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Categoría + Tipo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de categoría
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1A7FA6).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = offer.categoria.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1A7FA6),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Badge de tipo
                Text(
                    text = offer.tipoOferta.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
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

            // Descripción
            Text(
                text = offer.descripcionCorta,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ubicación
            if (offer.ubicacion.nombre.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = offer.ubicacion.nombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Duración
            if (offer.duracion.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = offer.duracion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Precio + Cupos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Precio
                Column {
                    if (offer.esGratis) {
                        Text(
                            text = "GRATIS",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        if (offer.descuento > 0) {
                            val precioFinal = offer.precio * (1 - offer.descuento / 100.0)
                            Text(
                                text = "S/ ${String.format("%.2f", precioFinal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A7FA6)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "S/ ${String.format("%.2f", offer.precio)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.Red.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "-${offer.descuento}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "S/ ${String.format("%.2f", offer.precio)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A7FA6)
                            )
                        }
                    }
                }

                // Cupos disponibles
                if (offer.capacidadMaxima > 0) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        val disponibilidad = if (offer.cuposDisponibles > 0) {
                            offer.cuposDisponibles.toFloat() / offer.capacidadMaxima
                        } else {
                            0f
                        }

                        val color = when {
                            offer.cuposDisponibles <= 0 -> Color(0xFFF44336)
                            disponibilidad > 0.5f -> Color(0xFF4CAF50)
                            disponibilidad > 0.2f -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }

                        LinearProgressIndicator(
                            progress = { disponibilidad },
                            modifier = Modifier
                                .width(100.dp)
                                .height(6.dp),
                            color = color,
                            trackColor = color.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                offer.cuposDisponibles <= 0 -> "❌ Agotado"
                                offer.cuposDisponibles == 1 -> "⚠️ Último cupo"
                                offer.cuposDisponibles <= 3 -> "⚠️ ${offer.cuposDisponibles} cupos"
                                else -> "${offer.cuposDisponibles} cupos"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                    }
                }
            }

            // Etiquetas
            if (offer.etiquetas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    offer.etiquetas.take(3).forEach { etiqueta ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF5F5F5)
                        ) {
                            Text(
                                text = "#$etiqueta",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


/**
 * MEJORAS EN FASE 2:
 * ✅ OfferCard completo con toda la información
 * ✅ Badges de categoría y tipo
 * ✅ Indicador visual de disponibilidad
 * ✅ Cálculo de precio con descuento
 * ✅ Etiquetas limitadas a 3
 * ✅ Estado vacío mejorado
 * ✅ Navegación back funcional
 */