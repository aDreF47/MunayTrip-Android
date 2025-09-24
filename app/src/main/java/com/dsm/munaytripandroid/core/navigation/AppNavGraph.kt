package com.dsm.munaytripandroid.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dsm.munaytripandroid.presentation.splash.ui.SplashScreen
import com.dsm.munaytripandroid.presentation.auth.ui.AuthScreen
import com.dsm.munaytripandroid.presentation.home.ui.HomeScreen
import com.dsm.munaytripandroid.presentation.initial.ui.InitialScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH
    ) {
        composable(Destinations.SPLASH) {
            SplashScreen(navController)
        }
        composable(Destinations.INITIAL) {
            InitialScreen()
        }
        composable(Destinations.AUTH) {
            AuthScreen()
        }
        composable(Destinations.HOME) {
            HomeScreen()
        }
    }
}

