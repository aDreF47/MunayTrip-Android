package com.dsm.munaytripandroid.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dsm.munaytripandroid.feature.onboarding.presentation.splash.SplashScreen
import com.dsm.munaytripandroid.feature.auth.presentation.login.LoginScreen
import com.dsm.munaytripandroid.feature.auth.presentation.register.RegisterScreen
import com.dsm.munaytripandroid.feature.auth.presentation.register.steps.RegisterMailStepScreen
import com.dsm.munaytripandroid.feature.home.presentation.ClientHomeScreen
import com.dsm.munaytripandroid.feature.home.presentation.HomeScreen
import com.dsm.munaytripandroid.feature.home.presentation.ProviderHomeScreen
import com.dsm.munaytripandroid.feature.offer.presentation.create.CreateOfferScreen
import com.dsm.munaytripandroid.feature.offer.presentation.detail.OfferDetailScreen
import com.dsm.munaytripandroid.feature.offer.presentation.list.OffersListScreen
import com.dsm.munaytripandroid.feature.offer.presentation.provider.MyOffersScreen
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

        // ========== HOME (DISPATCHER) ==========
        composable<Home> {
            HomeScreen(
                navController = navController,
                auth = auth,
                onLogout = {
                    navController.navigate(Initial) {
                        popUpTo(Home) { inclusive = true }
                    }
                }
            )
        }

        // ========== CLIENT HOME ==========
        composable<ClientHome> {
            ClientHomeScreen(
                auth = auth,
                onNavigateToProfile = {
                    navController.navigate(Profile)
                },
                onNavigateToOffersList = {
                    navController.navigate(OffersList)
                },
                onNavigateToOfferDetail = { offerId ->
                    navController.navigate(OfferDetail(offerId))
                },
                onLogout = {
                    auth.signOut()
                    navController.navigate(Initial) {
                        popUpTo(ClientHome) { inclusive = true }
                    }
                }
            )
        }

        // ========== PROVIDER HOME ==========
        composable<ProviderHome> {
            ProviderHomeScreen(
                auth = auth,
                onNavigateToProfile = {
                    navController.navigate(Profile)
                },
                onNavigateToMyOffers = {
                    navController.navigate(MyOffers)
                },
                onNavigateToCreateOffer = {
                    navController.navigate(CreateOffer)
                },
                onNavigateToOfferDetail = { offerId ->
                    navController.navigate(OfferDetail(offerId))
                },
                onLogout = {
                    auth.signOut()
                    navController.navigate(Initial) {
                        popUpTo(ProviderHome) { inclusive = true }
                    }
                }
            )
        }

        // ========== OFFERS LIST (Clients) ==========
        composable<OffersList> {
            OffersListScreen(
                onOfferClick = { offerId ->
                    navController.navigate(OfferDetail(offerId))
                },
                onSearchClick = {
                    // TODO: Implementar búsqueda
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ========== OFFER DETAIL ==========
        composable<OfferDetail> { backStackEntry ->
            val offerDetail: OfferDetail = backStackEntry.toRoute()
            OfferDetailScreen(
                offerId = offerDetail.offerId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onBookOffer = {
                    // TODO: Implementar reserva
                }
            )
        }

        // ========== MY OFFERS (Providers) ==========
        composable<MyOffers> {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                MyOffersScreen(
                    providerId = currentUser.uid,
                    onNavigateToCreateOffer = {
                        navController.navigate(CreateOffer)
                    },
                    onNavigateToOfferDetail = { offerId ->
                        navController.navigate(OfferDetail(offerId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ========== CREATE OFFER (Providers) ==========
        composable<CreateOffer> {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                CreateOfferScreen(
                    providerId = currentUser.uid,
                    onOfferCreated = { offerId ->
                        // Navegar al detalle de la oferta creada
                        navController.navigate(OfferDetail(offerId)) {
                            popUpTo(CreateOffer) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ========== PROFILE SCREEN ==========
        composable<Profile> {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}


/**
 * FLUJO DE NAVEGACIÓN:
 *
 * 1. INICIO:
 *    Splash → Initial → Login/Register → Home (dispatcher)
 *
 * 2. CLIENTS:
 *    Home → ClientHome
 *    ├─ OffersList → OfferDetail
 *    ├─ OfferDetail (desde featured)
 *    └─ Profile
 *
 * 3. PROVIDERS:
 *    Home → ProviderHome
 *    ├─ MyOffers → OfferDetail
 *    ├─ CreateOffer → OfferDetail (nueva)
 *    └─ Profile
 *
 * 4. LOGOUT:
 *    Cualquier Home → Initial (limpia toda la pila)
 *
 * NOTAS:
 * - Home es solo un dispatcher, se elimina de la pila inmediatamente
 * - ClientHome y ProviderHome son los verdaderos "homes"
 * - OfferDetail es compartido por clients y providers
 * - Profile es compartido por ambos roles
 */
