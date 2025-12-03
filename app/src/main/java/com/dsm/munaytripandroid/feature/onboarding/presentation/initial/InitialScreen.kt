package com.dsm.munaytripandroid.feature.onboarding.presentation.initial

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.munaytripandroid.R
import com.dsm.munaytripandroid.core.presentation.components.MunayTripLogo

/**
 * INITIAL SCREEN - Pantalla de bienvenida responsive
 *
 * CARACTERÍSTICAS:
 * - Scroll vertical para landscape y pantallas pequeñas
 * - Responsive: Se adapta a diferentes tamaños y orientaciones
 * - Botones siempre visibles y accesibles
 * - Logo adaptable según espacio disponible
 */
@Composable
fun InitialScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    // Tamaños adaptativos
    val logoSize = if (isLandscape) 180.dp else 280.dp
    val topSpacing = if (isLandscape) 16.dp else 48.dp
    val middleSpacing = if (isLandscape) 16.dp else 32.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA), // Gris muy claro arriba
                        Color.White        // Blanco abajo
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // ✅ Scroll vertical
                .padding(horizontal = 32.dp)
                .padding(top = topSpacing, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isLandscape) {
                Arrangement.Top
            } else {
                Arrangement.Center
            }
        ) {

            // Espaciador flexible si no es landscape
            if (!isLandscape) {
                Spacer(modifier = Modifier.weight(0.5f))
            }

            // Logo LLama
//            Image(
//                painter = painterResource(id = R.drawable.ic_launcher_foreground),
//                contentDescription = "Munay Trip - Llama",
//                modifier = Modifier
//                    .size(logoSize)
//                    .scale(1.3f),
//                contentScale = ContentScale.Fit
//            )

            // logo general
            MunayTripLogo()

            Spacer(modifier = Modifier.height(middleSpacing))

            // Texto de bienvenida
            Text(
                text = "MunayTrip",
                style = if (isLandscape) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.headlineLarge
                },
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A7FA6)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Descubre Perú con estilo",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF5D6D7E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 24.dp else 48.dp))

            // Botón de Registro (Primario)
            Button(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A7FA6)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Registrarte gratis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Login (Secundario)
            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF1A7FA6)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Espaciador flexible si no es landscape
            if (!isLandscape) {
                Spacer(modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Texto de ayuda o versión (opcional)
            val uriHandler = LocalUriHandler.current

            val annotatedText = buildAnnotatedString {
                append("¿Necesitas ayuda? ")

                pushStringAnnotation(
                    tag = "URL",
                    annotation = "https://wa.me/51935711810"
                )
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF1A7FA6),
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append("Contáctanos")
                }
                pop()
            }

            ClickableText(
                text = annotatedText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF5D6D7E).copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                }
            )
        }
    }
}