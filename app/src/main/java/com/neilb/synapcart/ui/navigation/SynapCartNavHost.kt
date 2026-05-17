package com.neilb.synapcart.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

@Composable
fun SynapCartNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Splash.route) {
            PlaceholderScreen("Splash Screen (Yükleniyor...)")
        }

        composable(route = Screen.Login.route) {
            PlaceholderScreen("Login Screen")
        }

        composable(route = Screen.Register.route) {
            PlaceholderScreen("Register Screen")
        }

        composable(route = Screen.ForgotPassword.route) {
            PlaceholderScreen("Forgot Password Screen")
        }

        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType; nullable = true }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://synapcart.app/reset-password?token={token}" },
                navDeepLink { uriPattern = "synapcart://reset-password?token={token}" }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token")
            PlaceholderScreen("Reset Password Screen\nGelen Token: $token")
        }

        composable(route = Screen.Onboarding.route) {
            PlaceholderScreen("Onboarding Screen (Dil & Para Birimi)")
        }

        composable(route = Screen.Dashboard.route) {
            PlaceholderScreen("Dashboard Screen (Ana Menü)")
        }

        composable(route = Screen.Chat.route) {
            PlaceholderScreen("Chat Screen (Ajan ile Sohbet)")
        }

        composable(route = Screen.Profile.route) {
            PlaceholderScreen("Profile Screen (Hesap Ayarları)")
        }

        composable(route = Screen.Favorites.route) {
            PlaceholderScreen("Favorites Screen (Beğenilen Ürünler)")
        }

        composable(
            route = Screen.ProductWeb.route,
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url")
            PlaceholderScreen("WebView Screen\nURL: $url")
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}