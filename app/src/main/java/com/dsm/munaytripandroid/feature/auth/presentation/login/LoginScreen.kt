package com.dsm.munaytripandroid.feature.auth.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dsm.munaytripandroid.R
import com.dsm.munaytripandroid.core.presentation.components.MunayTripLogoMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    // Observar evento de login exitoso
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
            viewModel.resetLoginSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar Sesión") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A7FA6),
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
                .verticalScroll(rememberScrollState()) // ✅ Scroll vertical
                .padding(horizontal = 32.dp)
                .padding(
                    top = if (isLandscape) 16.dp else 32.dp,
                    bottom = 32.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isLandscape) Arrangement.Top else Arrangement.Center
        ) {

            // Espaciador flexible solo en portrait
            if (!isLandscape) {
                Spacer(modifier = Modifier.weight(0.3f))
            }

            // Logo de la app
            Box(
                modifier = Modifier
                    .size(if (isLandscape) 120.dp else 160.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
//                    contentDescription = "MunayTrip Logo",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .scale(1.2f),
//                    contentScale = ContentScale.Fit
//                )
                MunayTripLogoMedium()
            }

            Text(
                text = "Bienvenido de vuelta hermano",
                style = if (isLandscape) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A7FA6)
            )

            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5D6D7E),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 20.dp else 32.dp))

            // Email Field
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
                isError = uiState.errorMessage != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1A7FA6),
                    focusedLabelColor = Color(0xFF1A7FA6)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (uiState.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !uiState.isLoading,
                isError = uiState.errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1A7FA6),
                    focusedLabelColor = Color(0xFF1A7FA6)
                ),
                trailingIcon = {
                    IconButton(onClick = viewModel::togglePasswordVisibility) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = if (uiState.isPasswordVisible) "Ocultar" else "Mostrar",
                            tint = Color.Gray
                        )
                    }
                }
            )

            // Error Message
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 24.dp else 32.dp))

            // Login Button
            Button(
                onClick = viewModel::onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A7FA6)
                ),
                enabled = !uiState.isLoading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { /* TODO: Reset password */ },
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = Color(0xFF1A7FA6)
                )
            }

            // Espaciador flexible solo en portrait
            if (!isLandscape) {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Espaciado final
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes una cuenta? ",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(
                    onClick = onNavigateToRegister, // 👈 usamos la navegación hacia  register
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = "Registrarse",
                        color = Color(0xFF1A7FA6),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}