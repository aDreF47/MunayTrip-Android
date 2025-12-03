package com.dsm.munaytripandroid.feature.analytics.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.munaytripandroid.feature.analytics.domain.model.ProviderAnalytics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDashboardScreen(
    providerId: String,
    viewModel: ProviderDashboardViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val analytics by viewModel.analytics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(providerId) {
        viewModel.loadAnalytics(providerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard de Analytics") },
                navigationIcon = {  // ✅ AGREGAR BOTÓN DE VOLVER
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAnalytics(providerId) }) {
                        Icon(Icons.Default.Refresh, "Actualizar")
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "❌",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshAnalytics(providerId) }) {
                            Text("Reintentar")
                        }
                    }
                }

                analytics.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay datos de analytics disponibles",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(analytics) { data ->
                            AnalyticsCard(analytics = data)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsCard(analytics: ProviderAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Text(
                text = "Oferta: ${analytics.offerId}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1A7FA6)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Métricas principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem("👁️ Vistas", analytics.totalViews.toString())
                MetricItem("👆 Clics", analytics.totalClicks.toString())
                MetricItem("✅ Reservas", analytics.totalBookings.toString())
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tasa de conversión
            LinearProgressIndicator(
                progress = { (analytics.conversionRate / 100).toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF4CAF50),
            )
            Text(
                text = "Conversión: ${String.format("%.2f", analytics.conversionRate)}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // Términos de búsqueda
            if (analytics.topSearchTerms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🔍 Términos de búsqueda populares:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                analytics.topSearchTerms.take(5).forEach { term ->
                    Text(
                        text = "  • $term",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF1A7FA6)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
/*
```

---

## ✅ **Estructura final de archivos:**
```
feature/
└── analytics/
├── data/
│   └── repository/
│       └── AnalyticsRepository.kt
├── domain/
│   └── model/
│       ├── ProviderAnalytics.kt
│       └── Interaction.kt (ya existe)
└── presentation/
├── ProviderDashboardScreen.kt
└── ProviderDashboardViewModel.kt*/