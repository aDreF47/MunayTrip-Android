package com.dsm.munaytripandroid.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.dsm.munaytripandroid.core.navigation.AppNavGraph
import com.dsm.munaytripandroid.presentation.initial.ui.InitialScreen
import com.dsm.munaytripandroid.ui.theme.MunayTripAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Ejemplo: Mantener Splash hasta que termine una condición (ej: cargando datos iniciales)
        var keepOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepOnScreen }

        // Aquí simulo que la app ya está lista (puedes reemplazar con un ViewModel)
        keepOnScreen = false
        enableEdgeToEdge()
        setContent {
            MunayTripAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val navController = rememberNavController()
                   // AppNavGraph(navController = navController)
                    InitialScreen()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MunayTripAndroidTheme {
        Greeting("Android")
    }
}