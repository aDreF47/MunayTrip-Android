package com.dsm.munaytripandroid.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dsm.munaytripandroid.feature.onboarding.presentation.splash.SplashScreen
import com.dsm.munaytripandroid.feature.auth.presentation.login.LoginScreen
import com.dsm.munaytripandroid.feature.auth.presentation.register.RegisterScreen
import com.dsm.munaytripandroid.feature.auth.presentation.register.steps.RegisterMailStepScreen
import com.dsm.munaytripandroid.feature.home.presentation.HomeScreen
import com.dsm.munaytripandroid.feature.onboarding.presentation.initial.InitialScreen
import com.dsm.munaytripandroid.feature.profile.presentation.ProfileScreen
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
                    navController.navigate(Login) {
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Register) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // ========== LOGIN SCREEN ==========
        composable<Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Home) {
                        // Eliminar toda la pila hasta Initial
                        popUpTo(Initial) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRegister = {
                    navController.navigate(Register) {
                        popUpTo(Login) { inclusive = true }
                        launchSingleTop = true
                    }
                }

            )
        }

//        fun RegisterScreen(
//            onNavigateToEmailRegister: () -> Unit,
//            onNavigateToPhoneRegister: () -> Unit,
//            onNavigateToGoogleRegister: () -> Unit,
//            onNavigateToLogin: () -> Unit,
//            onNavigateBack: () -> Unit

        // ========== REGISTER SCREEN ==========
        composable<Register> {
            RegisterScreen(
                onNavigateToEmailRegister = {
                    navController.navigate(RegisterMail) {
                        popUpTo(Register) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToPhoneRegister = {},
                onNavigateToGoogleRegister = {},
                onNavigateToLogin = {
                    navController.navigate(Login) {
                        popUpTo(Register) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    navController.popBackStack() // ✅ igual que arriba
                },

            )
        }

        // ===== registerMail ==========
        composable<RegisterMail> {
            RegisterMailStepScreen(
                onRegisterSuccess = {
                    navController.navigate(Home) {
                        popUpTo(Initial) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ========== HOME SCREEN ==========
        composable<Home> {
            HomeScreen(
                onNavigateToProfile = { navController.navigate(Profile) }, //aagregao para perfil
                auth = auth,
                onLogout = {
                    navController.navigate(Initial) {
                        popUpTo(Home) { inclusive = true } // ✅ Limpia la pila hasta Home
                    }
                }
            )
        }

        // ========== PROFILE SCREEN ==========
        composable<Profile> {
            ProfileScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

