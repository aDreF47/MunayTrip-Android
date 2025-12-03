package com.dsm.munaytripandroid.feature.auth.presentation.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsm.munaytripandroid.R
import com.dsm.munaytripandroid.core.presentation.components.MunayTripLogoMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToEmailRegister: () -> Unit,
    onNavigateToPhoneRegister: () -> Unit,
    onNavigateToGoogleRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A7FA6)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ========== SECCIÓN SUPERIOR: LOGO Y TÍTULO ==========
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MunayTripLogoMedium()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Bienvenido a MunayTrip",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A7FA6),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Conecta con viajeros y descubre nuevas experiencias",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // ========== SECCIÓN MEDIA: BOTONES DE REGISTRO ==========
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón: Email
                RegisterMethodButton(
                    icon = Icons.Default.Email,
                    title = "Continuar con correo",
                    subtitle = "Rápido y seguro",
                    onClick = onNavigateToEmailRegister,
                    enabled = true
                )

                // Botón: Teléfono deshabilitado
                RegisterMethodButton(
                    icon = Icons.Default.Phone,
                    title = "Continuar con teléfono",
                    subtitle = "Próximamente",
                    onClick = onNavigateToPhoneRegister,
                    enabled = false
                )

                // Botón: Google deshabilitado
                GoogleRegisterButton(
                    onClick = onNavigateToGoogleRegister,
                    enabled = false
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¿Ya tienes una cuenta? ",
                        color = Color(0xFF666666),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal
                    )
                    TextButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.padding(0.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Inicia sesión",
                            color = Color(0xFF1A7FA6),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterMethodButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.5.dp,
            if (enabled) Color(0xFF1A7FA6) else Color(0xFFE0E0E0)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (enabled) Color.White else Color(0xFFFAFAFA),
            contentColor = if (enabled) Color.Black else Color.Gray
        ),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (enabled) Color(0xFF1A7FA6) else Color(0xFFBDBDBD)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) Color(0xFF1A1A1A) else Color(0xFFBDBDBD)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enabled) Color(0xFF999999) else Color(0xFFD0D0D0)
                )
            }
        }
    }
}

@Composable
fun GoogleRegisterButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.5.dp,
            if (enabled) Color(0xFF1A7FA6) else Color(0xFFE0E0E0)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (enabled) Color.White else Color(0xFFFAFAFA),
            contentColor = if (enabled) Color.Black else Color.Gray
        ),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Continuar con Google",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) Color(0xFF1A1A1A) else Color(0xFFBDBDBD)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Próximamente",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enabled) Color(0xFF999999) else Color(0xFFD0D0D0)
                )
            }
        }
    }
}