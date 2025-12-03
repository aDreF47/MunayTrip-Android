package com.dsm.munaytripandroid.feature.analytics.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.munaytripandroid.feature.analytics.domain.model.ProviderAnalytics
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlin.math.roundToInt

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
                navigationIcon = {
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
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF1A7FA6)
                    )
                }

                error != null -> {
                    ErrorState(
                        error = error ?: "Error desconocido",
                        onRetry = { viewModel.refreshAnalytics(providerId) }
                    )
                }

                analytics.isEmpty() -> {
                    EmptyState()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            OverviewCard(analytics)
                        }

                        item {
                            VicoBarChartCard(analytics)
                        }

                        item {
                            VicoLineChartCard(analytics)
                        }

                        item {
                            ConversionRateCard(analytics)
                        }

                        items(analytics) { data ->
                            OfferAnalyticsCard(analytics = data)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewCard(analyticsList: List<ProviderAnalytics>) {
    val totalViews = analyticsList.sumOf { it.totalViews }
    val totalClicks = analyticsList.sumOf { it.totalClicks }
    val totalBookings = analyticsList.sumOf { it.totalBookings }
    val avgConversion = if (analyticsList.isNotEmpty()) {
        analyticsList.map { it.conversionRate }.average()
    } else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A7FA6)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "📊 Resumen General",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("👁️", totalViews.toString(), "Vistas", Color.White)
                StatItem("👆", totalClicks.toString(), "Clics", Color.White)
                StatItem("✅", totalBookings.toString(), "Reservas", Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Conversión promedio: ${String.format("%.1f", avgConversion)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatItem(emoji: String, value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun VicoBarChartCard(analyticsList: List<ProviderAnalytics>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Comparación de Métricas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            SimpleBarChart(
                title = "👁️ Vistas",
                data = analyticsList.map { it.totalViews },
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SimpleBarChart(
                title = "👆 Clics",
                data = analyticsList.map { it.totalClicks },
                color = Color(0xFF2196F3)
            )

            Spacer(modifier = Modifier.height(16.dp))

            SimpleBarChart(
                title = "✅ Reservas",
                data = analyticsList.map { it.totalBookings },
                color = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
fun SimpleBarChart(
    title: String,
    data: List<Int>,
    color: Color
) {
    if (data.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries {
                series(data)
            }
        }
    }

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            color = color,
                            thickness = 16.dp,
                            shape = Shape.rounded(50)
                        )
                    )
                ),
                startAxis = rememberStartAxis(
                    label = rememberTextComponent(
                        color = Color.Gray,
                        textSize = 10.sp
                    )
                ),
                bottomAxis = rememberBottomAxis(
                    label = rememberTextComponent(
                        color = Color.Gray,
                        textSize = 10.sp
                    ),
                    valueFormatter = { value, _, _ ->
                        "O${value.toInt() + 1}"
                    }
                )
            ),
            modelProducer = modelProducer,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
    }
}

@Composable
fun VicoLineChartCard(analyticsList: List<ProviderAnalytics>) {
    if (analyticsList.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📈 Tendencia de Conversión",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            val modelProducer = remember { CartesianChartModelProducer() }

            LaunchedEffect(analyticsList) {
                modelProducer.runTransaction {
                    columnSeries {
                        series(analyticsList.map { it.conversionRate })
                    }
                }
            }

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        ColumnCartesianLayer.ColumnProvider.series(
                            rememberLineComponent(
                                color = Color(0xFF1A7FA6),
                                thickness = 12.dp,
                                shape = Shape.rounded(50)
                            )
                        )
                    ),
                    startAxis = rememberStartAxis(
                        label = rememberTextComponent(
                            color = Color.Gray,
                            textSize = 12.sp
                        ),
                        valueFormatter = { value, _, _ ->
                            "${value.toInt()}%"
                        }
                    ),
                    bottomAxis = rememberBottomAxis(
                        label = rememberTextComponent(
                            color = Color.Gray,
                            textSize = 12.sp
                        ),
                        valueFormatter = { value, _, _ ->
                            "Oferta ${value.toInt() + 1}"
                        }
                    )
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }
    }
}

// ... (resto de las funciones igual: ConversionRateCard, OfferAnalyticsCard, EmptyState, ErrorState, etc.)


@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier.size(14.dp),
            shape = RoundedCornerShape(3.dp),
            color = color
        ) {}
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
    }
}

// ============ TASA DE CONVERSIÓN (MANTENER) ============
@Composable
fun ConversionRateCard(analyticsList: List<ProviderAnalytics>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 Tasas de Conversión Detalladas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            analyticsList.forEachIndexed { index, analytics ->
                ConversionProgressBar(
                    label = "Oferta ${index + 1}",
                    percentage = analytics.conversionRate.toFloat()
                )
                if (index < analyticsList.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ConversionProgressBar(label: String, percentage: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(1000),
        label = "conversion_animation"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${percentage.roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1A7FA6),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = when {
                percentage >= 15 -> Color(0xFF4CAF50)
                percentage >= 10 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            },
            trackColor = Color(0xFFE0E0E0),
        )
    }
}

// ============ TARJETA DE OFERTA INDIVIDUAL (MANTENER) ============
@Composable
fun OfferAnalyticsCard(analytics: ProviderAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📦 ${analytics.offerId.take(20)}...",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A7FA6)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn("👁️ Vistas", analytics.totalViews.toString())
                MetricColumn("👆 Clics", analytics.totalClicks.toString())
                MetricColumn("✅ Reservas", analytics.totalBookings.toString())
            }

            if (analytics.topSearchTerms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🔍 Búsquedas populares:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    analytics.topSearchTerms.take(3).forEach { term ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE3F2FD)
                        ) {
                            Text(
                                text = term,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1A7FA6),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A7FA6)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

// ============ ESTADOS (MANTENER) ============
@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📊",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No hay datos de analytics disponibles",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Las estadísticas aparecerán cuando los usuarios interactúen con tus ofertas",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A7FA6)
            )
        ) {
            Text("Reintentar")
        }
    }
}

