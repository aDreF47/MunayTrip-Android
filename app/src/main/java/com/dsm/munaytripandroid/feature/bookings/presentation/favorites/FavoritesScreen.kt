package com.dsm.munaytripandroid.feature.bookings.presentation.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

private val MunayPrimary = Color(0xFF1A7FA6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOfferDetail: (String) -> Unit,
    viewModel: FavoritesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    LaunchedEffect(uiState.favorites) {
        println("Favorites loaded: ${uiState.favorites.size}")
        uiState.favorites.forEach { favorite ->
            println("Favorite: ${favorite.offer.titulo}, ID: ${favorite.id}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Favoritos") },
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
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadFavorites() }) {
                            Text("Reintentar")
                        }
                    }
                }
                uiState.favorites.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = "Sin favoritos",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tienes favoritos",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cuando agregues ofertas a favoritos, aparecerán aquí",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                else -> {
                    FavoritesContent(
                        favorites = uiState.favorites,
                        onFavoriteClick = { favorite ->
                            onNavigateToOfferDetail(favorite.offerId)
                        },
                        onRemoveFavorite = { favorite ->
                            viewModel.removeFromFavorites(favorite.offerId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesContent(
    favorites: List<com.dsm.munaytripandroid.feature.bookings.domain.model.Favorite>,
    onFavoriteClick: (com.dsm.munaytripandroid.feature.bookings.domain.model.Favorite) -> Unit,
    onRemoveFavorite: (com.dsm.munaytripandroid.feature.bookings.domain.model.Favorite) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(favorites, key = { it.id }) { favorite ->
            FavoriteCard(
                favorite = favorite,
                onClick = { onFavoriteClick(favorite) },
                onRemove = { onRemoveFavorite(favorite) }
            )
        }
    }
}

@Composable
fun FavoriteCard(
    favorite: com.dsm.munaytripandroid.feature.bookings.domain.model.Favorite,
    onClick: () -> Unit,
    onRemove: () -> Unit
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = favorite.offer.categoria.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Quitar de favoritos",
                        tint = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = favorite.offer.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = favorite.offer.descripcionCorta,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (favorite.offer.esGratis) {
                Text(
                    text = "GRATIS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            } else {
                val precioFinal = if (favorite.offer.descuento > 0) {
                    favorite.offer.precio * (1 - favorite.offer.descuento / 100.0)
                } else {
                    favorite.offer.precio
                }

                Text(
                    text = "S/ ${String.format("%.2f", precioFinal)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MunayPrimary
                )
            }
        }
    }
}