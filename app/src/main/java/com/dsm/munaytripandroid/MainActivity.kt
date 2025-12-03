package com.dsm.munaytripandroid

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.dsm.munaytripandroid.core.navigation.AppNavigation
import com.dsm.munaytripandroid.feature.profile.presentation.ProfileViewModel
import com.dsm.munaytripandroid.ui.theme.MunayTripAndroidTheme
import com.google.android.libraries.places.api.Places
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth



class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController
    private lateinit var auth: FirebaseAuth




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        Places.initialize(applicationContext, "AIzaSyBD731D3mQkUG-CejYzvg3KKnQwPm2ACPQ")
        enableEdgeToEdge()
        setContent {
            val profileViewModel: ProfileViewModel = viewModel() // Obtén tu ViewModel
            navController = rememberNavController()
            MunayTripAndroidTheme {
                Surface(
                    modifier = Modifier.Companion.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val profileViewModel: ProfileViewModel = viewModel() // Obtén tu ViewModel
                }
                    AppNavigation(navController = navController, auth)
                    DeepLinkHandler(viewModel = profileViewModel)

                    if (profileViewModel.showSuccessDialog) {
                        RedeemSuccessDialog(
                            pointsEarned = profileViewModel.lastPointsEarned,
                            onDismiss = { profileViewModel.dismissDialog() }
                        )
                    }
            }
        }
    }

}

@SuppressLint("ContextCastToActivity")
@Composable
fun DeepLinkHandler(viewModel: ProfileViewModel) {
    val activity = LocalContext.current as? Activity
    val intent = activity?.intent

    // Este efecto se ejecuta una vez cuando se carga la pantalla
    LaunchedEffect(Unit) {
        val data: Uri? = intent?.data

        // Verificamos si el link es de nuestro esquema munaytrip://redeem
        if (data != null && data.scheme == "munaytrip" && data.host == "redeem") {
            val code = data.getQueryParameter("code")
            if (code != null) {
                // LLAMAMOS A LA FUNCIÓN DE CANJE
                viewModel.redeemPoints(code)

                // Limpiamos el intent para que no se canjee de nuevo si rotamos la pantalla
                intent.data = null
            }
        }
    }
}

@Composable
fun RedeemSuccessDialog(
    pointsEarned: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen / Ícono festivo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFFFF176), CircleShape), // Fondo amarillo claro
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Estrella",
                        tint = Color(0xFFFBC02D), // Dorado fuerte
                        modifier = Modifier.size(60.dp)
                    )
                    // Pequeño check verde encima
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "¡Felicidades!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ganaste $pointsEarned puntos\npor tu compra",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("¡Genial!", fontSize = 16.sp, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}
