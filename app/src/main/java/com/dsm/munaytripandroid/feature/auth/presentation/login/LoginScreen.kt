package com.dsm.munaytripandroid.feature.auth.presentation.login

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.dsm.munaytripandroid.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    onLoginSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Coroutine scope para operaciones asíncronas
    val scope = rememberCoroutineScope()

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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bienvenido de vuelta",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A7FA6)
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null // Limpiar error al escribir
                },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                isError = errorMessage != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)

            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null // Limpiar error al escribir
                },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading,
                isError = errorMessage != null,
                trailingIcon = {
                    val image = if (passwordVisible) {
                        Icons.Default.Visibility
                    } else {
                        Icons.Default.VisibilityOff
                    }
                    val description = if (passwordVisible) {
                        stringResource(R.string.login_password_ocultar)
                    } else {
                        stringResource(R.string.login_password_mostrar)
                    }

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = description,
                            tint = Color.Gray
                        )
                    }
                }
            )

            // Mostrar mensaje de error
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // Validaciones
                    when {
                        email.isBlank() -> {
                            errorMessage = "El correo no puede estar vacío"
                            return@Button
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            errorMessage = "Correo electrónico inválido"
                            return@Button
                        }
                        password.isBlank() -> {
                            errorMessage = "La contraseña no puede estar vacía"
                            return@Button
                        }
                        password.length < 6 -> {
                            errorMessage = "La contraseña debe tener al menos 6 caracteres"
                            return@Button
                        }
                    }

                    // Iniciar proceso de login
                    isLoading = true
                    errorMessage = null

                    scope.launch {
                        try {
                            // Autenticación con Firebase (AWAIT para esperar resultado)
                            val result = auth.signInWithEmailAndPassword(email, password).await()
                            val user = result.user

                            if (user != null) {
                                Log.d("AUTH", "Login exitoso: ${user.email}")
                                // Solo navegar si el login fue exitoso
                                onLoginSuccess()
                            } else {
                                errorMessage = "Error al iniciar sesión"
                                Log.e("AUTH", "Usuario nulo después del login")
                            }

                        } catch (e: Exception) {
                            // Manejo de errores específicos
                            errorMessage = when (e) {
                                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                    "Contraseña incorrecta"
                                }
                                is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                                    "Usuario no encontrado"
                                }
                                is com.google.firebase.FirebaseNetworkException -> {
                                    "Error de conexión. Verifica tu internet"
                                }
                                else -> {
                                    "Error: ${e.message}"
                                }
                            }
                            Log.e("AUTH", "Error en login: ${e.message}", e)
                        } finally {
                            isLoading = false
                        }
                    }

                    // TODO: Implementar lógica de login
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A7FA6)
                ),
                enabled = !isLoading // Deshabilitar mientras carga
            ) {
                if (isLoading) {
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

            // Botón de olvidé mi contraseña
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    // TODO: Implementar reset de contraseña
                },
                enabled = !isLoading
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = Color(0xFF1A7FA6)
                )
            }
        }
    }
}
