package com.dsm.munaytripandroid.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.munaytripandroid.feature.bookings.domain.model.Booking
import com.google.firebase.auth.FirebaseAuth
import com.github.tehras.charts.bar.BarChart
import com.github.tehras.charts.bar.BarChartData
import com.github.tehras.charts.bar.renderer.label.SimpleValueDrawer


private val MunayPrimary = Color(0xFF1A7FA6)
private val MunaySecondary = Color(0xFF4DB6E8)

/**
 * HOME PARA PROVIDERS (Proveedores)
 * Versión básica FASE 1 - Solo estructura y navegación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderHomeScreen(
    auth: FirebaseAuth,
    onNavigateToProfile: () -> Unit,
    onNavigateToMyOffers: () -> Unit,
    onNavigateToCreateOffer: () -> Unit,
    onNavigateToOfferDetail: (String) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser = auth.currentUser
    val displayName = currentUser?.displayName ?: "Proveedor"
    var showLogoutDialog by remember { mutableStateOf(false) }

    val statsOfertasActivas = 5
    val statsVistasTotales = 120
    val statsReservas = 15

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Munay Trip",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Panel de Proveedor",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateOffer,
                containerColor = MunayPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear oferta")
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MunayPrimary,
                        selectedTextColor = MunayPrimary,
                        indicatorColor = MunayPrimary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToMyOffers,
                    icon = { Icon(Icons.Default.Inventory, "Mis Ofertas") },
                    label = { Text("Ofertas") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToAnalytics,
                    icon = { Icon(Icons.Default.Analytics, "Analytics") },
                    label = { Text("Analytics") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.Person, "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        },
        containerColor = Color(0xFFF5F9FB)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            text = "¡Bienvenido, $displayName! 🏢",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gestiona tus ofertas y atrae más clientes",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Crear Oferta
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    onClick = onNavigateToCreateOffer
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = MunayPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Crear Oferta",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Mis Ofertas
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    onClick = onNavigateToMyOffers
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null,
                            tint = MunayPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mis Ofertas",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ NUEVA TARJETA DE ANALYTICS
            Card(
                onClick = onNavigateToAnalytics,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.BarChart, "Analytics")
                    Text("Ver estadísticas")
                }
            }

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🎯 Rol: Proveedor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Crea y gestiona tus ofertas turísticas, atrae clientes y analiza tu rendimiento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Info de versión
            Text(
                text = "FASE 1 - Navegación Base Implementada ✅",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }



    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
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
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Estás seguro de que deseas cerrar sesión, $displayName?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MunayPrimary
                    )
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = MunayPrimary)
                }
            }
        )
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MunayPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}


@Composable
fun StatsBarChart(
    ofertas: Float,
    vistas: Float,
    reservas: Float
) {
    // Usamos un tercer color para la 3ra barra
    val MunayAccent = Color(0xFFE88C4D) // Un color naranja para "Reservas"

    // Creamos la lista de barras
    val barras = arrayListOf(
        BarChartData.Bar(
            label = "Activas",
            value = ofertas,
            color = MunayPrimary
        ),
        BarChartData.Bar(
            label = "Vistas",
            value = vistas,
            color = MunaySecondary
        ),
        BarChartData.Bar(
            label = "Reservas",
            value = reservas,
            color = MunayAccent
        )
    )

    // Renderizamos el gráfico
    BarChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        barChartData = BarChartData(
            bars = barras
        ),
        // Muestra la etiqueta (ej. "Activas") debajo de la barra
        labelDrawer = SimpleValueDrawer(
            drawLocation = SimpleValueDrawer.DrawLocation.XAxis,
        ),
        // --- LÍNEA DE ANIMACIÓN ELIMINADA ---
        // animation = simpleChartAnimation()
    )
}

/**
 * NOTAS FASE 1:
 * - Esta es una versión BÁSICA funcional
 * - En FASE 2 se agregará dashboard real con stats de Firestore
 * - Por ahora solo valida navegación y estructura
 * - FAB para crear ofertas
 * - Quick actions para acceso rápido
 */