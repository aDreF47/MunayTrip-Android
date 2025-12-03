package com.dsm.munaytripandroid.feature.Foot

import android.R.attr.radius
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt
import kotlin.random.Random

// --- Colores de tu proyecto (Basado en la imagen) ---
private val MunayPrimary = Color(0xFF1A7FA6) // Color principal de ejemplo
private val MunaySecondary = Color(0xFF4D6BE8) // Color secundario de ejemplo

// --- 1. Definición de Datos ---

/**
 * Clase de datos para representar un premio en la ruleta.
 * @param name Nombre del premio.
 * @param color Color del segmento en la ruleta.
 * @param startAngle Ángulo inicial (en grados) del segmento.
 * @param sweepAngle Extensión del ángulo (en grados) del segmento.
 */
data class Prize(
    val name: String,
    val color: Color,
    val startAngle: Float,
    val sweepAngle: Float
)

// Definición de los premios con colores vibrantes
private val prizesList = listOf(
    Prize("Ticket 2x1", Color(0xFF4CAF50), 0f, 45f),
    Prize("Descuento 50%", Color(0xFFFFC107), 45f, 45f),
    Prize("Ticket Gratis", Color(0xFFE91E63), 90f, 45f),
    Prize("Noche de Hotel", Color(0xFF2196F3), 135f, 45f),
    Prize("Ticket de 20%", Color(0xFF9C27B0), 180f, 45f),
    Prize("Vuelva a intentarlo", Color(0xFFE91E63), 225f, 45f),
    Prize("Ticket de 20%", Color(0xFF4CAF50), 270f, 45f),
    Prize("Vuelva a intentarlo", Color(0xFFFFC107), 315f, 45f)
)

/**
 * Composable que simula la pantalla FootScreen de tu proyecto.
 * Contiene la lógica de la ruleta adaptada al diseño de tu app.
 *
 * Puedes llamar a esta función directamente en tu navegación:
 * NavHost(..., startDestination = "...") {
 * composable("foot_route") {
 * FootScreen(onNavigateBack = { navController.popBackStack() }, ...)
 * }
 * }
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FootScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFoots: () -> Unit,
    // Aquí iría el parámetro viewModel si lo necesitaras:
    // viewModel: BookingsViewModel = viewModel()
) {
    // --- Lógica de la Ruleta ---
    var targetRotation by remember { mutableStateOf(0f) }
    var winningPrize by remember { mutableStateOf<Prize?>(null) }
    var isSpinning by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(
            durationMillis = 4000,
            easing = EaseOutQuart
        ), label = "wheelRotation"
    )

    // Determinar el premio al finalizar la animación
    LaunchedEffect(rotation) {
        if (isSpinning && rotation == targetRotation) {
            val finalAngle = targetRotation % 360f
            val normalizedAngle = (finalAngle + 90f) % 360f
            val prize = determineWinningPrize(normalizedAngle)
            winningPrize = prize
            isSpinning = false
        }
    }
    // --- Fin Lógica de la Ruleta ---

    // Usamos SnackBarHostState, aunque no se usa aquí, respetamos la estructura
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Huella de Carbono", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MunayPrimary, // Usando tu color principal
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F5F9)), // Fondo claro para resaltar la ruleta
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Gira y Gana Premios",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MunayPrimary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 2. Ruleta Composable
            SpinningWheel(
                modifier = Modifier
                    .size(320.dp) // Un poco más grande
                    .rotate(rotation)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // 3. Botón para Girar
            Button(
                onClick = {
                    if (!isSpinning) {
                        isSpinning = true
                        winningPrize = null
                        // Generar un nuevo ángulo objetivo
                        val randomOffset = Random.nextFloat() * 360f
                        targetRotation += 360f * (5..10).random() + randomOffset
                    }
                },
                enabled = !isSpinning,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp)), // Añadimos sombra para un mejor diseño
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSpinning) Color.Gray else MunaySecondary // Usando tu color secundario
                )
            ) {
                Text(
                    text = if (isSpinning) "¡Girando...!" else "GIRAR AHORA",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }

    // 4. Diálogo de Premio
    winningPrize?.let { prize ->
        PrizeDialog(
            prizeName = prize.name,
            onDismiss = { winningPrize = null }
        )
    }
}

/**
 * Dibuja la ruleta con sus segmentos y texto.
 */
@Composable
fun SpinningWheel(
    modifier: Modifier = Modifier,
    prizes: List<Prize> = prizesList // Usamos la lista predeterminada
) {
    val textMeasurer = rememberTextMeasurer()
    val textSize = 14.sp

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val segmentRadius = radius * 0.75f

            prizes.forEach { prize ->
                // Dibujar el segmento (Arco)
                drawArc(
                    color = prize.color,
                    startAngle = prize.startAngle,
                    sweepAngle = prize.sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(canvasSize, canvasSize)
                )

                val textAngle = prize.startAngle + prize.sweepAngle / 2f

                // Rotar el sistema de coordenadas para dibujar el texto
                rotate(degrees = textAngle + 90f, pivot = center) {
                    val textLayoutResult = textMeasurer.measure(
                        text = prize.name,
                        style = TextStyle(color = Color.White, fontSize = textSize, fontWeight = FontWeight.SemiBold)
                    )

                    // Posicionar el texto a lo largo del radio
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = center.x - (textLayoutResult.size.width / 2f),
                            y = center.y - radius + (radius - segmentRadius) * 0.5f
                        )
                    )
                }
            }

            // Dibujar el círculo central (estético)
            drawCircle(
                color = Color.White,
                radius = radius * 0.1f,
                center = center
            )
        }

        // Indicador (Flecha superior que señala el resultado)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val indicatorSize = 25.dp.toPx()
            drawPath(
                color = Color.Black, // Color más oscuro para el indicador
                path = androidx.compose.ui.graphics.Path().apply {
                    // Flecha en la parte superior (0 grados)
                    moveTo(center.x, center.y - radius - 5.dp.toPx()) // Mover ligeramente afuera
                    lineTo(center.x + indicatorSize / 2f, center.y - radius + indicatorSize / 4f - 5.dp.toPx())
                    lineTo(center.x - indicatorSize / 2f, center.y - radius + indicatorSize / 4f - 5.dp.toPx())
                    close()
                }
            )
            // Círculo decorativo en el centro
            drawCircle(
                color = MunayPrimary,
                radius = 12.dp.toPx(),
                center = center
            )
        }
    }
}

/**
 * Muestra el diálogo modal con el premio ganado.
 */
@Composable
fun PrizeDialog(prizeName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "¡PREMIO ENCONTRADO!",
                fontWeight = FontWeight.Black,
                color = MunayPrimary
            )
        },
        text = {
            Text(
                text = "¡Felicidades! Se ha desbloqueado la zona turística: $prizeName.",
                fontSize = 18.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MunaySecondary)
            ) {
                Text("Aceptar y Ver", color = Color.White)
            }
        },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.background(Color.White)
    )
}

/**
 * Lógica para determinar qué premio se ganó basándose en el ángulo final.
 */
private fun determineWinningPrize(normalizedAngle: Float): Prize {
    // El ángulo está normalizado y apunta hacia el indicador de la ruleta.

    val angle = (normalizedAngle.roundToInt() % 360).toFloat()

    return prizesList.firstOrNull { prize ->
        val start = prize.startAngle
        val end = start + prize.sweepAngle
        angle >= start && angle < end
    } ?: prizesList.first() // Fallback
}

@Preview(showBackground = true)
@Composable
fun PreviewFootScreen() {
    // Simulamos la pantalla para el Preview
    MaterialTheme(colorScheme = lightColorScheme(primary = MunayPrimary)) {
        FootScreen(onNavigateBack = {}, onNavigateToFoots = {})
    }
}