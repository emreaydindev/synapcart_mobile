package com.neilb.synapcart.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neilb.synapcart.ui.components.BrandHeader
import com.neilb.synapcart.ui.navigation.Screen

/**
 * Routes to Login, Onboarding or Dashboard. The system splash (core-splashscreen) stays up
 * until [onDestinationResolved], so this branded frame is only a fallback.
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onDestinationResolved: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    LaunchedEffect(destination) {
        val target = destination ?: return@LaunchedEffect
        when (target) {
            Screen.Login.route -> onNavigateToLogin()
            Screen.Onboarding.route -> onNavigateToOnboarding()
            Screen.Dashboard.route -> onNavigateToMain()
        }
        onDestinationResolved()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        BrandHeader(markWidth = 120.dp)
    }
}
