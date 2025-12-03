package com.dsm.munaytripandroid.core.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dsm.munaytripandroid.R

/**
 * MUNAYTRIP LOGO - Componente reutilizable
 *
 * Logo circular con background (paisaje) y foreground (llama)
 *
 * @param size Tamaño del círculo contenedor
 * @param backgroundScale Escala del background (default: 1.43f)
 * @param foregroundScale Escala del foreground/llama (default: 1.5f)
 * @param modifier Modificador adicional para el contenedor principal
 */
@Composable
fun MunayTripLogo(
    size: Dp = 230.dp,
    backgroundScale: Float = 1.43f,
    foregroundScale: Float = 1.5f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        // Background (montañas, cielo) - Escala mayor para llenar el círculo
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier
                .size(size * 1.3f) // Más grande que el círculo para llenar completamente
                .scale(backgroundScale),
            contentScale = ContentScale.Crop
        )

        // Foreground (llama con lentes)
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "MunayTrip Logo - Llama",
            modifier = Modifier
                .size(size * 1.3f) // Mismo tamaño que el background
                .scale(foregroundScale),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * VARIANTES PREDEFINIDAS
 * Para casos de uso comunes
 */

// Logo pequeño para headers o cards
@Composable
fun MunayTripLogoSmall(modifier: Modifier = Modifier) {
    MunayTripLogo(
        size = 80.dp,
        backgroundScale = 1.43f,
        foregroundScale = 1.5f,
        modifier = modifier
    )
}

// Logo mediano para pantallas de login/register
@Composable
fun MunayTripLogoMedium(modifier: Modifier = Modifier) {
    MunayTripLogo(
        size = 160.dp,
        backgroundScale = 1.43f,
        foregroundScale = 1.5f,
        modifier = modifier
    )
}

// Logo grande para splash o pantallas principales
@Composable
fun MunayTripLogoLarge(modifier: Modifier = Modifier) {
    MunayTripLogo(
        size = 230.dp,
        backgroundScale = 1.43f,
        foregroundScale = 1.5f,
        modifier = modifier
    )
}

// Logo extra grande para efectos especiales
@Composable
fun MunayTripLogoXLarge(modifier: Modifier = Modifier) {
    MunayTripLogo(
        size = 300.dp,
        backgroundScale = 1.43f,
        foregroundScale = 1.5f,
        modifier = modifier
    )
}