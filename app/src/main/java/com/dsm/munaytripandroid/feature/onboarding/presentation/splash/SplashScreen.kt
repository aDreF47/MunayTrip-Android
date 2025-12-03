package com.dsm.munaytripandroid.feature.onboarding.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.dsm.munaytripandroid.R

/**
 * SPLASH SCREEN - Pantalla de carga inicial con animación de logo circular
 *
 * ANIMACIÓN: La llama se asoma desde abajo dentro del círculo del logo
 * DURACIÓN: ~2.5 segundos totales
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = viewModel(),
    onNavigateToInitial: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    // Estados de animación
    var startLogoAnimation by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    // Animación: Escala del logo (aparece con zoom suave)
    val logoScale by animateFloatAsState(
        targetValue = if (startLogoAnimation) 1f else 0.7f,
        animationSpec = tween(
            durationMillis = 600,
            easing = EaseOutBack
        ),
        label = "logo_scale"
    )

    // Animación: Llama asomándose desde abajo
    val llamaOffsetY by animateFloatAsState(
        targetValue = if (showContent) 0f else 1f,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = 200,
            easing = EaseOutCubic
        ),
        label = "llama_slide"
    )

    // Animación: Fade in del texto
    val textAlpha by animateFloatAsState(
        targetValue = if (showContent && llamaOffsetY < 0.3f) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500
        ),
        label = "text_fade"
    )

    // Control de navegación
    LaunchedEffect(Unit) {
        delay(100)
        startLogoAnimation = true

        delay(400)
        showContent = true

        delay(2000) // Tiempo total visible

        val hasActiveSession = viewModel.checkUserSession()
        if (hasActiveSession) {
            onNavigateToHome()
        } else {
            onNavigateToInitial()
        }
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A7FA6), // Turquesa
                        Color(0xFF156B8E)  // Turquesa más oscuro
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo circular con animación
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                // Sombra/borde sutil
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                // Contenedor circular principal
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .clip(CircleShape)
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    // Background (montañas, cielo) - Escala mayor para llenar el círculo
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = null,
                        modifier = Modifier
                            .size(300.dp) // Más grande que el círculo para llenar completamente
                            .scale(1.43f), // Escala adicional
                        contentScale = ContentScale.Crop
                    )

                    // Foreground (llama con lentes) - Se asoma desde abajo
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Munay Trip - Llama",
                        modifier = Modifier
                            .size(300.dp) // Mismo tamaño que el background
                            .scale(1.5f) // Misma escala
                            .graphicsLayer {
                                translationY = size.height * llamaOffsetY
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Texto del nombre de la app
            Text(
                text = "Munay Trip",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Descubre Perú con estilo",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Indicador de carga
            if (showContent && llamaOffsetY < 0.5f) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer { alpha = textAlpha },
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            }
        }

        // Versión o tagline en la parte inferior (opcional)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "v1.0.0",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha
                }
            )
        }
    }
}