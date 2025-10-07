package com.dsm.munaytripandroid.presentation.splash.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dsm.munaytripandroid.core.navigation.Destinations
import kotlinx.coroutines.delay
import com.dsm.munaytripandroid.R

@Composable
fun SplashScreen(navController: NavController) {
    // Simulación de delay y chequeo de login
    LaunchedEffect(Unit) {
        delay(5000) // 2 segundos de splash

        val isLoggedIn = false // aquí luego conectarás con tu sesión real

        if (isLoggedIn) {
            navController.navigate(Destinations.HOME) {
                popUpTo(Destinations.SPLASH) { inclusive = true }
            }
        } else {
            navController.navigate("auth_graph")  {
                popUpTo(Destinations.SPLASH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // tu logo
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Munay Trippp",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Estamos más cerca de tiii",
                fontSize = 16.sp
            )
        }
    }
}
