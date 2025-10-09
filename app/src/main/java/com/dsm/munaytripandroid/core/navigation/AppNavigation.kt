package com.dsm.munaytripandroid.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dsm.munaytripandroid.feature.onboarding.presentation.splash.SplashScreen
import com.dsm.munaytripandroid.feature.auth.presentation.login.LoginScreen
import com.dsm.munaytripandroid.feature.auth.presentation.register.RegisterScreen
import com.dsm.munaytripandroid.feature.home.presentation.HomeScreen
import com.dsm.munaytripandroid.feature.onboarding.presentation.initial.InitialScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(navController: NavHostController, auth: FirebaseAuth) {
    // PRINCIPIO: NavHost define el gráfico de navegación
    // startDestination: El primer destino que ve el usuario
    NavHost(
        navController = navController,
        startDestination = Splash
    ) {
        // ========== SPLASH SCREEN ==========
        composable<Splash> {
            SplashScreen(
                onNavigateToInitial = {
                    navController.navigate(Initial) {
                        // Eliminar splash de la pila para que no se pueda regresar
                        popUpTo(Splash) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    // Si el usuario ya tiene sesión, ir directo a Home
                    navController.navigate(Home) {
                        popUpTo(Splash) { inclusive = true }
                    }
                }
            )
        }

        // ========== INITIAL SCREEN ==========
        composable<Initial> {
            InitialScreen(
                onNavigateToLogin = {
                    navController.navigate(Login)
                },
                onNavigateToRegister = {
                    navController.navigate(Register)
                }
            )
        }

        // ========== LOGIN SCREEN ==========
        composable<Login> {
            LoginScreen(
                auth,
                onLoginSuccess = {
                    navController.navigate(Home) {
                        // Eliminar toda la pila hasta Initial
                        popUpTo(Initial) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // ========== REGISTER SCREEN ==========
        composable<Register> {
            RegisterScreen(
                auth,
                onRegisterSuccess = {
                    navController.navigate(Home) {
                        popUpTo(Initial) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // ========== HOME SCREEN ==========
        composable<Home> {
            HomeScreen(
                auth = auth,
                onLogout = {
                    navController.navigate(Initial) {
                        popUpTo(Home) { inclusive = true } // ✅ Limpia la pila hasta Home
                    }
                }
            )
        }
    }
}

