package com.neilb.synapcart.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neilb.synapcart.R
import com.neilb.synapcart.ui.theme.SynapDarkBg
import com.neilb.synapcart.ui.theme.SynapForeground

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val destination by viewModel.destination.collectAsState()

    LaunchedEffect(destination) {
        when (destination) {
            "login_screen" -> onNavigateToLogin()
            "onboarding_screen" -> onNavigateToOnboarding()
            "dashboard_screen" -> onNavigateToMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SynapDarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "SynapCart Marka Logosu",
                modifier = Modifier
                    .size(300.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SynapCart",
                fontSize = 42.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                color = SynapForeground,
                letterSpacing = 4.sp
            )

            Text(
                text = "Yapay Zeka Destekli Alışveriş Ajanı",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.padding(top = 10.dp),
                letterSpacing = 1.5.sp
            )
        }
    }
}