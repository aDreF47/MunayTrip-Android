package com.dsm.munaytripandroid.feature.home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dsm.munaytripandroid.core.navigation.ClientHome
import com.dsm.munaytripandroid.core.navigation.Home
import com.dsm.munaytripandroid.core.navigation.ProviderHome
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * HomeScreen como DISPATCHER
 * - Detecta el userType desde Firestore
 * - Redirige a ClientHome o ProviderHome
 * - Muestra loading mientras detecta
 */
@Composable
fun HomeScreen(
    navController: NavHostController,
    auth: FirebaseAuth,
    onLogout: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentUser = auth.currentUser

    LaunchedEffect(currentUser?.uid) {
        if (currentUser == null) {
            // Si no hay usuario, regresar a login
            onLogout()
            return@LaunchedEffect
        }

        try {
            // Obtener userType desde Firestore
            val userType = getUserTypeFromFirestore(currentUser.uid)

            // Navegar según el tipo de usuario
            when (userType) {
                "client" -> {
                    navController.navigate(ClientHome) {
                        popUpTo(Home) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                "provider" -> {
                    navController.navigate(ProviderHome) {
                        popUpTo(Home) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                else -> {
                    // Por defecto, enviar a ClientHome
                    navController.navigate(ClientHome) {
                        popUpTo(Home) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = e.message
            isLoading = false
        }
    }

    // UI mientras carga
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF1A7FA6),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cargando tu perfil...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        } else if (errorMessage != null) {
            // Mostrar error
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "Error al cargar perfil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "Error desconocido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A7FA6)
                    )
                ) {
                    Text("Volver a iniciar sesión")
                }
            }
        }
    }
}

/**
 * Obtiene el tipo de usuario desde Firestore
 * @return "client" o "provider"
 */
private suspend fun getUserTypeFromFirestore(userId: String): String {
    return try {
        val firestore = FirebaseFirestore.getInstance()
        val userDoc = firestore.collection("users")
            .document(userId)
            .get()
            .await()

        // Obtener userType, por defecto "client"
        userDoc.getString("user_type") ?: userDoc.getString("userType") ?: "client"
    } catch (e: Exception) {
        // Si hay error, asumir client por defecto
        "client"
    }
}



/**
 * NOTAS DE IMPLEMENTACIÓN:
 *
 * 1. Este HomeScreen es un DISPATCHER:
 *    - No muestra UI complejo
 *    - Solo detecta userType y redirige
 *
 * 2. Estructura de Firestore esperada:
 *    users/{userId}/
 *    └─ user_type: "client" o "provider"
 *
 * 3. Flujo:
 *    Login Success → Home (dispatcher) → ClientHome/ProviderHome
 *
 * 4. Si falla la detección:
 *    - Por defecto envía a ClientHome
 *    - Muestra botón para volver a login
 *
 * 5. Este componente se ejecuta UNA SOLA VEZ:
 *    - popUpTo(Home) { inclusive = true } lo elimina de la pila
 *    - El usuario nunca regresa aquí con back button
 */