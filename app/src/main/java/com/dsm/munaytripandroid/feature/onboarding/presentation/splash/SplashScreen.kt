package com.dsm.munaytripandroid.feature.onboarding.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.dsm.munaytripandroid.R

/**
 * SPLASH SCREEN - Pantalla de carga inicial
 *
 * PRINCIPIOS GOOGLE 2025:
 * - Destino de inicio de la app
 * - Se elimina de la pila después de navegar (no se puede regresar)
 * - Verifica estado de sesión del usuario
 * - Duración recomendada: 1-3 segundos máximo
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = viewModel(),
    onNavigateToInitial: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    // Animación del logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    // EFECTO: Navegar después de verificar sesión
    LaunchedEffect(Unit) {
        // Simular carga de recursos / verificación de sesión
        delay(2000) // 2 segundos de splash

        // Verificar si el usuario tiene sesión activa
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
                color = Color(0xFF1A7FA6) // Color turquesa de Munay Trip
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo con animación
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Munay Trip Logo",
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Indicador de carga
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color.White,
                strokeWidth = 4.dp
            )
        }
    }
}