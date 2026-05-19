package com.neilb.synapcart.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SynapForeground,
    secondary = AccentMint,
    tertiary = PrimaryCyanLight,
    background = SynapDarkBg,
    surface = DeepNavy_800,
    onPrimary = DeepNavy_900,
    onBackground = NearWhite_100,
    onSurface = NearWhite_100
)

private val LightColorScheme = lightColorScheme(
    primary = SynapForeground,
    secondary = AccentMintDark,
    tertiary = PrimaryCyanDark,
    background = OffWhite_50,
    surface = White,
    onPrimary = White,
    onBackground = NearBlack_900,
    onSurface = NearBlack_900
)

@Composable
fun SynapCartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}