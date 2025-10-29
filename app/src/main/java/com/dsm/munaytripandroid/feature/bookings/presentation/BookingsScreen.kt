package com.dsm.munaytripandroid.feature.bookings.presentation

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

private val MunayPrimary = Color(0xFF1A7FA6)
private val MunaySecondary = Color(0xFF4DB6E8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOfferDetail: (String) -> Unit,
    viewModel: BookingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis Reservas")
                        if (uiState.selectedType != null || uiState.selectedStatus != null) {
                            Text(
                                text = "${uiState.filteredBookings.size} reservas",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showFilterDialog = true },
                        enabled = uiState.bookings.isNotEmpty()
                    ) {
                        BadgedBox(
                            badge = {
                                if (uiState.selectedType != null || uiState.selectedStatus != null) {
                                    Badge {
                                        Text("1")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.FilterList, "Filtrar")
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
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MunayPrimary
                    )
                }
                uiState.error != null -> {
                }
                uiState.bookings.isEmpty() -> {
                }
                else -> {
                    BookingsContent(
                        bookings = uiState.filteredBookings,
                        onBookingClick = { booking ->
                            onNavigateToOfferDetail(booking.offerId)
                        },
                        onCancelBooking = { booking ->
                            viewModel.cancelBooking(booking.id)
                        }
                    )
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            selectedType = uiState.selectedType,
            selectedStatus = uiState.selectedStatus,
            onTypeSelected = { type ->
                viewModel.filterByType(type)
                showFilterDialog = false
            },
            onStatusSelected = { status ->
                viewModel.filterByStatus(status)
                showFilterDialog = false
            },
            onClearFilters = {
                viewModel.filterByType(null)
                viewModel.filterByStatus(null)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun BookingsContent(
    bookings: List<com.dsm.munaytripandroid.feature.bookings.domain.model.Booking>,
    onBookingClick: (com.dsm.munaytripandroid.feature.bookings.domain.model.Booking) -> Unit,
    onCancelBooking: (com.dsm.munaytripandroid.feature.bookings.domain.model.Booking) -> Unit
) {
    val groupedBookings = bookings.groupBy { it.offer.tipoOferta }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedBookings.forEach { (offerType, typeBookings) ->
            item {
                BookingTypeHeader(offerType = offerType, count = typeBookings.size)
            }

            items(typeBookings, key = { it.id }) { booking ->
                BookingCard(
                    booking = booking,
                    onClick = { onBookingClick(booking) },
                    onCancel = { onCancelBooking(booking) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun BookingTypeHeader(offerType: com.dsm.munaytripandroid.feature.offer.domain.model.OfferType, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val (icon, color) = when (offerType) {
                com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.EVENT ->
                    Pair(Icons.Default.Event, Color(0xFFE91E63))
                com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.RESERVATION ->
                    Pair(Icons.Default.Hotel, Color(0xFF2196F3))
                com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.TOUR ->
                    Pair(Icons.Default.Explore, Color(0xFF4CAF50))
                com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.EXPERIENCE ->
                    Pair(Icons.Default.School, Color(0xFFFF9800))
                com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.SERVICE ->
                    Pair(Icons.Default.Build, Color(0xFF9C27B0))
            }

            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = color
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = offerType.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier
                .background(
                    color = Color.Gray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun FilterDialog(
    selectedType: com.dsm.munaytripandroid.feature.offer.domain.model.OfferType?,
    selectedStatus: String?,
    onTypeSelected: (com.dsm.munaytripandroid.feature.offer.domain.model.OfferType?) -> Unit,
    onStatusSelected: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar Reservas") },
        text = {
            Column {
                Text(
                    "Tipo de Oferta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedType == null,
                            onClick = { onTypeSelected(null) },
                            label = { Text("Todos") }
                        )
                    }

                    items(com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.entries) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { onTypeSelected(type) },
                            label = { Text(type.displayName) }
                        )
                    }
                }

                Text(
                    "Estado",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatus == null,
                            onClick = { onStatusSelected(null) },
                            label = { Text("Todos") }
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedStatus == "confirmed",
                            onClick = { onStatusSelected("confirmed") },
                            label = { Text("Confirmadas") }
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedStatus == "completed",
                            onClick = { onStatusSelected("completed") },
                            label = { Text("Completadas") }
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedStatus == "cancelled",
                            onClick = { onStatusSelected("cancelled") },
                            label = { Text("Canceladas") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClearFilters) {
                Text("Limpiar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun BookingCard(
    booking: com.dsm.munaytripandroid.feature.bookings.domain.model.Booking,
    onClick: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (booking.offer.tipoOferta) {
                            com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.EVENT ->
                                Color(0xFFE91E63).copy(alpha = 0.1f)
                            com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.RESERVATION ->
                                Color(0xFF2196F3).copy(alpha = 0.1f)
                            com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.TOUR ->
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.EXPERIENCE ->
                                Color(0xFFFF9800).copy(alpha = 0.1f)
                            com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.SERVICE ->
                                Color(0xFF9C27B0).copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = booking.offer.tipoOferta.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (booking.offer.tipoOferta) {
                                com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.EVENT ->
                                    Color(0xFFE91E63)
                                com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.RESERVATION ->
                                    Color(0xFF2196F3)
                                com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.TOUR ->
                                    Color(0xFF4CAF50)
                                com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.EXPERIENCE ->
                                    Color(0xFFFF9800)
                                com.dsm.munaytripandroid.feature.offer.domain.model.OfferType.SERVICE ->
                                    Color(0xFF9C27B0)
                            },
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (booking.status) {
                            "confirmed" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            "cancelled" -> Color.Red.copy(alpha = 0.1f)
                            "completed" -> Color(0xFF2196F3).copy(alpha = 0.1f)
                            else -> Color.Gray.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = when (booking.status) {
                                "confirmed" -> "Confirmada"
                                "cancelled" -> "Cancelada"
                                "completed" -> "Completada"
                                else -> booking.status
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (booking.status) {
                                "confirmed" -> Color(0xFF4CAF50)
                                "cancelled" -> Color.Red
                                "completed" -> Color(0xFF2196F3)
                                else -> Color.Gray
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(booking.bookingDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = booking.offer.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = booking.offer.descripcionCorta,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (booking.offer.esGratis) {
                    Text(
                        text = "GRATIS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                } else {
                    val precioFinal = if (booking.offer.descuento > 0) {
                        booking.offer.precio * (1 - booking.offer.descuento / 100.0)
                    } else {
                        booking.offer.precio
                    }

                    Text(
                        text = "S/ ${String.format("%.2f", precioFinal)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MunayPrimary
                    )
                }

                if (booking.status == "confirmed") {
                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}