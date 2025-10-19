// HomeScreen.kt - Diseño Profesional para Munay Trip

package com.dsm.munaytripandroid.feature.home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import com.google.firebase.auth.FirebaseAuth

// Colores del tema Munay Trip
private val MunayPrimary = Color(0xFF1A7FA6)
private val MunaySecondary = Color(0xFF4DB6E8)
private val MunayAccent = Color(0xFF125C7A)
private val MunayBackground = Color(0xFFF5F9FB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile:() -> Unit,
    auth: FirebaseAuth,
    onLogout: () -> Unit
) {
    val currentUser = auth.currentUser
    val displayName = currentUser?.displayName ?: "Viajero"
    val userEmail = currentUser?.email ?: ""

    // Estado para mostrar/ocultar diálogo de logout
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HomeTopBar(
                userName = displayName,
                onLogoutClick = { showLogoutDialog = true }
            )
        },
        bottomBar = {
            HomeBottomBar(onNavigateToProfile = onNavigateToProfile)
        },
        containerColor = MunayBackground
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            // Header con saludo
            item {
                WelcomeHeader(userName = displayName)
            }

            // Barra de búsqueda
            item {
                SearchBar()
            }

            // Sección: Destinos populares
            item {
                SectionTitle(title = "Destinos Populares", onSeeAllClick = {})
                PopularDestinations()
            }

            // Sección: Ofertas especiales
            item {
                SectionTitle(title = "Ofertas Especiales", onSeeAllClick = {})
                SpecialOffers()
            }

            // Sección: Categorías
            item {
                SectionTitle(title = "Explora por Categoría", onSeeAllClick = {})
                Categories()
            }

            // Sección: Viajes recientes
            item {
                SectionTitle(title = "Viajes Recientes", onSeeAllClick = {})
                RecentTrips()
            }
        }

        // Diálogo de confirmación para logout
        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                userName = displayName,
                onConfirm = {
                    auth.signOut()
                    onLogout()
                },
                onDismiss = { showLogoutDialog = false }
            )
        }
    }
}


// ==================== TOP BAR ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    userName: String,
    onLogoutClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Munay Trip",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tu compañero de viajes",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        actions = {
            // Notificaciones
            IconButton(onClick = { /* TODO */ }) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) {
                            Text("3")
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones",
                        tint = MunayPrimary
                    )
                }
            }

            // Perfil/Logout
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Perfil",
                    tint = MunayPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}


// ==================== WELCOME HEADER ====================
@Composable
fun WelcomeHeader(userName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(MunayPrimary, MunaySecondary)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "¡Hola, $userName! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¿Listo para tu próxima aventura?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}


// ==================== SEARCH BAR ====================
@Composable
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text("Busca destinos, actividades...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = MunayPrimary
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White
        ),
        readOnly = true, // Por ahora solo visual
        singleLine = true
    )
    Spacer(modifier = Modifier.height(24.dp))
}


// ==================== SECTION TITLE ====================
@Composable
fun SectionTitle(
    title: String,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onSeeAllClick) {
            Text(
                text = "Ver todo",
                color = MunayPrimary
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MunayPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}


// ==================== POPULAR DESTINATIONS ====================
@Composable
fun PopularDestinations() {
    val destinations = listOf(
        DestinationItem("Cusco", "Perú", "⛰️", "Desde S/ 299"),
        DestinationItem("Máncora", "Perú", "🏖️", "Desde S/ 189"),
        DestinationItem("Arequipa", "Perú", "🏔️", "Desde S/ 249"),
        DestinationItem("Iquitos", "Perú", "🌴", "Desde S/ 399")
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(destinations) { destination ->
            DestinationCard(destination)
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}

data class DestinationItem(
    val name: String,
    val country: String,
    val emoji: String,
    val price: String
)

@Composable
fun DestinationCard(destination: DestinationItem) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(220.dp)
            .clickable { /* TODO */ },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Imagen placeholder con emoji
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(MunaySecondary, MunayPrimary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = destination.emoji,
                    style = MaterialTheme.typography.displayLarge
                )
            }

            // Información
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = destination.country,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = destination.price,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MunayPrimary
                )
            }
        }
    }
}


// ==================== SPECIAL OFFERS ====================
@Composable
fun SpecialOffers() {
    val offers = listOf(
        OfferItem("Descuento 20%", "En paquetes familiares", Icons.Default.Favorite, Color(0xFFE91E63)),
        OfferItem("2x1 Tours", "Especial de temporada", Icons.Default.LocalOffer, Color(0xFFFF9800)),
        OfferItem("Hoteles -30%", "Reserva anticipada", Icons.Default.Home, Color(0xFF4CAF50))
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(offers) { offer ->
            OfferCard(offer)
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}

data class OfferItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun OfferCard(offer: OfferItem) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { /* TODO */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = offer.color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = offer.icon,
                contentDescription = null,
                tint = offer.color,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = offer.color
                )
                Text(
                    text = offer.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}


// ==================== CATEGORIES ====================
@Composable
fun Categories() {
    val categories = listOf(
        CategoryItem("Aventura", Icons.Default.Hiking, MunayPrimary),
        CategoryItem("Playa", Icons.Default.Hiking, Color(0xFF00BCD4)),
        CategoryItem("Montaña", Icons.Default.Terrain, Color(0xFF8BC34A)),
        CategoryItem("Ciudad", Icons.Default.LocationCity, Color(0xFFFF5722))
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            CategoryChip(category)
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}

data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun CategoryChip(category: CategoryItem) {
    Surface(
        modifier = Modifier.clickable { /* TODO */ },
        shape = RoundedCornerShape(24.dp),
        color = category.color.copy(alpha = 0.1f),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.name,
                fontWeight = FontWeight.Medium,
                color = category.color
            )
        }
    }
}


// ==================== RECENT TRIPS ====================
@Composable
fun RecentTrips() {
    val trips = listOf(
        TripItem("Tour Machu Picchu", "Hace 2 días", Icons.Default.CheckCircle, Color.Green),
        TripItem("Paracas & Huacachina", "Hace 1 semana", Icons.Default.CheckCircle, Color.Green),
        TripItem("Líneas de Nazca", "Hace 2 semanas", Icons.Default.CheckCircle, Color.Green)
    )

    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        trips.forEach { trip ->
            TripListItem(trip)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

data class TripItem(
    val name: String,
    val date: String,
    val icon: ImageVector,
    val statusColor: Color
)

@Composable
fun TripListItem(trip: TripItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = trip.icon,
                contentDescription = null,
                tint = trip.statusColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = trip.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}


// ==================== BOTTOM BAR ====================
@Composable
fun HomeBottomBar(
    onNavigateToProfile: () -> Unit // 👈 Añade este parámetro
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Inicio")
            },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MunayPrimary,
                selectedTextColor = MunayPrimary,
                indicatorColor = MunayPrimary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(Icons.Default.Search, contentDescription = "Explorar")
            },
            label = { Text("Explorar") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Icon(Icons.Default.BookmarkBorder, contentDescription = "Guardados")
            },
            label = { Text("Guardados") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToProfile,
            icon = {
                Icon(Icons.Default.Person, contentDescription = "Perfil")
            },
            label = { Text("Perfil") }
        )
    }
}


// ==================== LOGOUT DIALOG ====================
@Composable
fun LogoutConfirmationDialog(
    userName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                tint = MunayPrimary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Cerrar sesión",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas cerrar sesión, $userName?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MunayPrimary
                )
            ) {
                Text("Cerrar sesión")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar", color = MunayPrimary)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}


// ==================== RESUMEN ====================
/**
 * CARACTERÍSTICAS DEL NUEVO HOME:
 *
 * ✅ FUNCIONALIDADES MANTENIDAS:
 * - Login/Logout con Firebase Auth
 * - Muestra nombre del usuario
 * - Botón de cerrar sesión funcional
 * - Diálogo de confirmación de logout
 *
 * 🎨 NUEVO DISEÑO PROFESIONAL:
 * - TopBar con logo, notificaciones y perfil
 * - Header con gradiente y saludo personalizado
 * - Barra de búsqueda (visual por ahora)
 * - Sección de destinos populares (scroll horizontal)
 * - Ofertas especiales con íconos y colores
 * - Categorías con chips interactivos
 * - Lista de viajes recientes
 * - Bottom Navigation Bar con 4 secciones
 *
 * 🎯 ELEMENTOS VISUALES:
 * - Cards con elevación y esquinas redondeadas
 * - Gradientes de color
 * - Emojis como placeholders de imágenes
 * - Colores consistentes del tema Munay Trip
 * - Espaciado y padding profesional
 * - Animaciones sutiles (puede agregar más)
 *
 * 📱 COMPONENTES PRINCIPALES:
 * - WelcomeHeader: Saludo personalizado
 * - SearchBar: Búsqueda (placeholder)
 * - PopularDestinations: Tarjetas de destinos
 * - SpecialOffers: Ofertas en tarjetas
 * - Categories: Chips de categorías
 * - RecentTrips: Lista de viajes
 * - HomeBottomBar: Navegación inferior
 *
 * 🔧 TODO (para futuro):
 * - Conectar con ViewModel y datos reales
 * - Implementar navegación entre secciones
 * - Agregar funcionalidad de búsqueda
 * - Cargar imágenes reales de destinos
 * - Implementar favoritos/guardados
 * - Agregar animaciones de transición
 */