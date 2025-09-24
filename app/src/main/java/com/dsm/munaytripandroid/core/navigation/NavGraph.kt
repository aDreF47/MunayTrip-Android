package com.dsm.munaytripandroid.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dsm.munaytripandroid.core.navigation.Screen
import com.dsm.munaytripandroid.presentation.splash.ui.SplashScreen
import com.dsm.munaytripandroid.presentation.auth.ui.AuthScreen
import com.dsm.munaytripandroid.presentation.home.ui.HomeScreen
import com.dsm.munaytripandroid.presentation.home.ui.DetailScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToAuth = { navController.navigate(Screen.Auth.route) }
            )
        }
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginSuccess = { navController.navigate(Screen.Home.route) }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDetail = { itemId ->
                    navController.navigate(Screen.Detail.createRoute(itemId))
                }
            )
        }
        composable(Screen.Detail.route) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            DetailScreen(itemId = itemId ?: "Sin ID")
        }
    }
}
