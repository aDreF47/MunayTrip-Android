
package com.dsm.munaytripandroid.presentation.splash.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SplashScreen(onNavigateToAuth: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pantalla de Splash")
        Button(onClick = onNavigateToAuth) {
            Text("Ir a Login")
        }
    }
}
