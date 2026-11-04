package com.neilb.synapcart.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")

    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object ResetPassword : Screen("reset_password_screen?token={token}") {
        fun passToken(token: String) = "reset_password_screen?token=${Uri.encode(token)}"
    }

    object Onboarding : Screen("onboarding_screen")
    object Dashboard : Screen("dashboard_screen")
    object Profile : Screen("profile_screen")
    object Favorites : Screen("favorites_screen")

    /** The product itself is handed over through SelectedProductStore. */
    object ProductDetails : Screen("product_details_screen")
    /** Products to compare are handed over through SelectedProductStore.compareProducts. */
    object Compare : Screen("compare_screen")
    object Paywall : Screen("paywall_screen")

    object ProductWeb : Screen("product_web_screen?url={url}") {
        fun passUrl(url: String) = "product_web_screen?url=${Uri.encode(url)}"
    }
}
