package com.neilb.synapcart.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")

    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object ResetPassword : Screen("reset_password_screen?token={token}") {
        fun passToken(token: String) = "reset_password_screen?token=$token"
    }

    object Onboarding : Screen("onboarding_screen")
    object Dashboard : Screen("dashboard_screen")
    object Chat : Screen("chat_screen?sessionId={sessionId}") {
        fun passSession(sessionId: Int) = "chat_screen?sessionId=$sessionId"
    }
    object Profile : Screen("profile_screen")
    object Favorites : Screen("favorites_screen")

    object ProductDetails : Screen("product_details_screen?productJson={productJson}") {
        fun passProduct(productJson: String) = "product_details_screen?productJson=${android.net.Uri.encode(productJson)}"
    }
    object ProductWeb : Screen("product_web_screen?url={url}") {
        fun passUrl(url: String) = "product_web_screen?url=${android.net.Uri.encode(url)}"
    }
}