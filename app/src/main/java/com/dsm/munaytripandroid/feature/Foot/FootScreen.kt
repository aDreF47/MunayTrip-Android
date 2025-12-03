package com.dsm.munaytripandroid.feature.Foot

import android.R.attr.radius
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.io.path.Path
import kotlin.io.path.moveTo
import kotlin.math.roundToInt
import kotlin.random.Random

// --- Colores de tu proyecto (Basado en la imagen) ---
private val MunayPrimary = Color(0xFF1A7FA6) // Color principal de ejemplo
private val MunaySecondary = Color(0xFF4D6BE8) // Color secundario de ejemplo

// --- 1. ViewModel para la Lógica de Puntos ---
class FootViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // Estado de los puntos del usuario
    var userPoints by mutableStateOf(0)
        private set

    init {
        listenToUserPoints()
    }

    private fun listenToUserPoints() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null && snapshot.exists()) {
                    userPoints = snapshot.getLong("points")?.toInt() ?: 0
                }
            }
    }

    /**
     * Intenta gastar puntos para girar la ruleta.
     * @param cost Costo del giro (ej. 50 puntos).
     * @param onSuccess Callback si la transacción es exitosa.
     * @param onError Callback si no hay puntos suficientes o hay error.
     */
    fun spinWheel(cost: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                onError("Debes iniciar sesión.")
                return@launch
            }

            val userRef = db.collection("users").document(userId)

            try {
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val currentPoints = snapshot.getLong("points") ?: 0

                    if (currentPoints >= cost) {
                        // CAMBIO: Calculamos el nuevo valor explícitamente para mayor seguridad
                        val newPoints = currentPoints - cost
                        transaction.update(userRef, "points", newPoints)
                    } else {
                        throw Exception("insuficientes")
                    }
                }.await()

                // Si llegamos aquí, la transacción fue exitosa
                onSuccess()

            } catch (e: Exception) {
                // Mensaje personalizado si el error fue por puntos
                val msg = if (e.message?.contains("insuficientes") == true)
                    "Necesitas $cost puntos para girar la ruleta"
                else
                    e.message ?: "Error al procesar puntos"
                onError(msg)
            }
        }
    }
}

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
    Prize("Ticket de 10%", Color(0xFF2196F3), 135f, 45f),
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
    viewModel: FootViewModel = viewModel() // Inyectamos el ViewModel aquí
) {
    val context = LocalContext.current
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
            // 1. Normalizamos la rotación final a 0-360
            val currentRotationNormalized = targetRotation % 360f

            // 2. FORMULA MAESTRA:
            // La flecha está fija en 270 grados (arriba).
            // Restamos la rotación de la ruleta para ver qué ángulo del disco está "debajo" de la flecha.
            // Sumamos 360 antes del módulo final para evitar números negativos.
            val angleOfArrowOnWheel = (270f - currentRotationNormalized + 360f) % 360f

            // 3. Buscamos el premio con ese ángulo corregido
            val prize = determineWinningPrize(angleOfArrowOnWheel)
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
                title = { Text(text = "Canjea tus pts y gana", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.Top
        ) {
            // --- NUEVO: Tarjeta de Puntos ---
            Spacer(modifier = Modifier.height(24.dp))
            RoulettePointsCard(points = viewModel.userPoints)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Gira y Gana Premios",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MunayPrimary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // 2. Ruleta Composable
            SpinningWheel(
                rotationDegrees = rotation,
                modifier = Modifier
                    .size(320.dp) // Un poco más grande
                    //.rotate(rotation)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // 3. Botón para Girar
            Button(
                onClick = {
                    if (!isSpinning) {
                        // 1. VALIDACIÓN LOCAL INMEDIATA
                        if (viewModel.userPoints < 50) {
                            Toast.makeText(context, "Necesitas 50 puntos para girar la ruleta", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // 2. LÓGICA DE GASTO DE PUNTOS (Con validación de servidor)
                        viewModel.spinWheel(
                            cost = 50,
                            onSuccess = {
                                // Si se cobró con éxito, giramos la ruleta
                                isSpinning = true
                                winningPrize = null
                                val randomOffset = Random.nextFloat() * 360f
                                targetRotation += 360f * (5..10).random() + randomOffset
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        )
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

@Composable
fun RoulettePointsCard(points: Int) {
    Card(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFB300), // Dorado
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TUS PUNTOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = points.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MunayPrimary
                )
            }
        }
    }
}


/**
 * Dibuja la ruleta con sus segmentos y texto.
 */@Composable
fun SpinningWheel(
    rotationDegrees: Float, // 1. Recibimos la rotación aquí
    modifier: Modifier = Modifier,
    prizes: List<Prize> = prizesList
) {
    val textMeasurer = rememberTextMeasurer()
    val textSize = 14.sp

    Box(modifier = modifier) {
        // --- CAPA 1: LA RULETA (Esta SÍ rota) ---
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotationDegrees) // 2. Aplicamos la rotación SOLO a este Canvas
        ) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val segmentRadius = radius * 0.75f

            prizes.forEach { prize ->
                // Dibujar el segmento
                drawArc(
                    color = prize.color,
                    startAngle = prize.startAngle,
                    sweepAngle = prize.sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(canvasSize, canvasSize)
                )

                val textAngle = prize.startAngle + prize.sweepAngle / 2f

                // Rotar el texto para que coincida con el segmento
                rotate(degrees = textAngle + 90f, pivot = center) {
                    val textLayoutResult = textMeasurer.measure(
                        text = prize.name,
                        style = TextStyle(color = Color.White, fontSize = textSize, fontWeight = FontWeight.SemiBold)
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = center.x - (textLayoutResult.size.width / 2f),
                            y = center.y - radius + (radius - segmentRadius) * 0.5f
                        )
                    )
                }
            }

            // Círculo central estético (que gira con la ruleta)
            drawCircle(color = Color.White, radius = radius * 0.1f, center = center)
        }

        // --- CAPA 2: EL INDICADOR / FLECHA (Esta NO rota) ---
        // Al no ponerle .rotate(), se queda fija en la pantalla
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Dibujamos la flecha en la parte SUPERIOR (12 en punto)
            val path = Path().apply {
                moveTo(center.x, center.y - radius + 50f) // Punta bajando un poco más
                lineTo(center.x - 30f, center.y - radius - 20f) // Izquierda arriba
                lineTo(center.x + 30f, center.y - radius - 20f) // Derecha arriba
                close()
            }

            // Sombra de la flecha
            drawPath(color = Color.Black.copy(alpha = 0.3f), path = path)

            // Flecha Principal
            drawPath(color = MunayPrimary, path = path)

            // Borde blanco de la flecha
            drawPath(
                color = Color.White,
                path = path,
                style = Stroke(width = 4.dp.toPx())
            )

            // Botón central decorativo (Fijo encima de todo)
            drawCircle(color = Color.White, radius = 20.dp.toPx(), center = center)
            drawCircle(color = MunayPrimary, radius = 14.dp.toPx(), center = center)
        }
    }
}

/**
 * Muestra el diálogo modal con el premio ganado.
 */
@Composable
fun PrizeDialog(prizeName: String, onDismiss: () -> Unit) {
    val isLoss = prizeName.equals("Vuelva a intentarlo", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isLoss) "Suerte para la próxima" else "¡Felicidades!",
                fontWeight = FontWeight.Black,
                color = MunayPrimary
            )
        },
        text = {
            Text(
                text = if (isLoss) "No ganaste ningún premio." else "Ganaste: $prizeName.",
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
private fun determineWinningPrize(angleOnWheel: Float): Prize {
    // El ángulo ya viene calculado respecto al inicio de cada segmento (0 grados = Este)

    return prizesList.firstOrNull { prize ->
        val start = prize.startAngle
        val end = start + prize.sweepAngle

        // Verificamos si el ángulo de la flecha cae dentro de este segmento
        angleOnWheel >= start && angleOnWheel < end
    } ?: prizesList.first() // Fallback por seguridad
}

@Preview(showBackground = true)
@Composable
fun PreviewFootScreen() {
    // Simulamos la pantalla para el Preview
    MaterialTheme(colorScheme = lightColorScheme(primary = MunayPrimary)) {
        FootScreen(onNavigateBack = {}, onNavigateToFoots = {})
    }
}