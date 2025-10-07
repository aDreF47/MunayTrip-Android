package com.dsm.munaytripandroid.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.dsm.munaytripandroid.presentation.splash.ui.SplashScreen
import com.dsm.munaytripandroid.presentation.auth.ui.LogInScreen
import com.dsm.munaytripandroid.presentation.home.ui.HomeScreen
import com.dsm.munaytripandroid.presentation.initial.ui.InitialScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavGraph(navController: NavHostController, auth: FirebaseAuth
) {
    NavHost(navController = navController, startDestination = Destinations.SPLASH) {
        composable(Destinations.SPLASH) {
            SplashScreen(navController)
        }

        navigation(
            startDestination = Destinations.LOGIN,
            route = "auth_graph" // Dale un nombre al grupo de rutas
        ) {
            composable(Destinations.LOGIN) { LogInScreen(navController) }
//            composable(Destinations.SIGNUP) { SignUpScreen(navController) }
        }

//        composable(Destinations.INITIAL) {
//            InitialScreen(
//                navigateToLogin={navController.navigate(Destinations.LOGIN)},
//                //navigateToSignUp={navHostController.navigate("signUp")}
//            )
//        }

//        composable(Destinations.AUTH) {
//            AuthScreen()
//        }
        composable(Destinations.HOME) {
            HomeScreen()
        }
    }
}

