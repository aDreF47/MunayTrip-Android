package com.dsm.munaytripandroid.presentation.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(onNavigateToDetail: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pantalla de Home")
        Button(onClick = { onNavigateToDetail("123") }) {
            Text("Ir a Detalle 123")
        }
    }
}
