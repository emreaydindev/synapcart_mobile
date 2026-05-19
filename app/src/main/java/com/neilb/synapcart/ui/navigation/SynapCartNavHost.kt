package com.neilb.synapcart.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.google.gson.Gson
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.ui.screens.auth.ForgotPasswordScreen
import com.neilb.synapcart.ui.screens.auth.ForgotPasswordViewModel
import com.neilb.synapcart.ui.screens.auth.LoginScreen
import com.neilb.synapcart.ui.screens.auth.LoginViewModel
import com.neilb.synapcart.ui.screens.auth.RegisterScreen
import com.neilb.synapcart.ui.screens.auth.RegisterViewModel
import com.neilb.synapcart.ui.screens.auth.ResetPasswordScreen
import com.neilb.synapcart.ui.screens.auth.ResetPasswordViewModel
import com.neilb.synapcart.ui.screens.dashboard.DashboardScreen
import com.neilb.synapcart.ui.screens.dashboard.DashboardViewModel
import com.neilb.synapcart.ui.screens.onboarding.OnboardingScreen
import com.neilb.synapcart.ui.screens.onboarding.OnboardingViewModel
import com.neilb.synapcart.ui.screens.splash.SplashScreen
import com.neilb.synapcart.ui.screens.splash.SplashViewModel
import com.neilb.synapcart.ui.screens.favorites.FavoritesScreen
import com.neilb.synapcart.ui.screens.favorites.FavoritesViewModel
import com.neilb.synapcart.ui.screens.product.ProductDetailsScreen
import com.neilb.synapcart.ui.screens.product.ProductDetailsViewModel
import com.neilb.synapcart.ui.screens.product.ProductWebScreen
import com.neilb.synapcart.ui.screens.profile.ProfileScreen
import com.neilb.synapcart.ui.screens.profile.ProfileViewModel

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
            val splashViewModel: SplashViewModel = hiltViewModel()

            SplashScreen(
                viewModel = splashViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()

            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onContinueAsGuest = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Register.route) {
            val registerViewModel: RegisterViewModel = hiltViewModel()

            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            val forgotPasswordViewModel: ForgotPasswordViewModel = hiltViewModel()

            ForgotPasswordScreen(
                viewModel = forgotPasswordViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(navArgument("token") { type = NavType.StringType; nullable = true }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://synapcart.app/reset-password?token={token}" },
                navDeepLink { uriPattern = "synapcart://reset-password?token={token}" }
            )
        ) {
            val resetViewModel: ResetPasswordViewModel = hiltViewModel()

            ResetPasswordScreen(
                viewModel = resetViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ResetPassword.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Onboarding.route) {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()

            OnboardingScreen(
                viewModel = onboardingViewModel,
                onFinished = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() },
                onAccountDeleted = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Favorites.route) {
            val favoritesViewModel: FavoritesViewModel = hiltViewModel()
            FavoritesScreen(
                viewModel = favoritesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onLogout = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToProduct = { product ->
                    val json = Gson().toJson(product)
                    navController.navigate(Screen.ProductDetails.passProduct(json))
                }
            )
        }

        composable(
            route = Screen.ProductDetails.route,
            arguments = listOf(navArgument("productJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val productJson = backStackEntry.arguments?.getString("productJson") ?: ""
            val product = Gson().fromJson(productJson, ProductDTO::class.java)
            val viewModel: ProductDetailsViewModel = hiltViewModel()

            ProductDetailsScreen(
                product = product,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWeb = { link ->
                    navController.navigate(Screen.ProductWeb.passUrl(link))
                }
            )
        }

        composable(
            route = Screen.ProductWeb.route,
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            ProductWebScreen(
                url = url,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground)
    }
}