package com.neilb.synapcart.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/** Every Material role filled from [SynapColors]. SynapCart is dark-only. */
private val SynapColorScheme = darkColorScheme(
    primary = SynapColors.Primary,
    onPrimary = SynapColors.OnPrimary,
    primaryContainer = SynapColors.PrimaryContainer,
    onPrimaryContainer = SynapColors.OnPrimaryContainer,
    inversePrimary = SynapColors.OnPrimary,
    secondary = SynapColors.Secondary,
    onSecondary = SynapColors.OnSecondary,
    secondaryContainer = SynapColors.SecondaryContainer,
    onSecondaryContainer = SynapColors.OnSecondaryContainer,
    tertiary = SynapColors.PremiumGold,
    onTertiary = SynapColors.OnPremiumGold,
    tertiaryContainer = SynapColors.SurfaceHigh,
    onTertiaryContainer = SynapColors.PremiumGold,
    background = SynapColors.Bg,
    onBackground = SynapColors.Text,
    surface = SynapColors.Surface,
    onSurface = SynapColors.Text,
    surfaceVariant = SynapColors.SurfaceHigh,
    onSurfaceVariant = SynapColors.TextMuted,
    surfaceTint = SynapColors.Primary,
    inverseSurface = SynapColors.Text,
    inverseOnSurface = SynapColors.Bg,
    error = SynapColors.Error,
    onError = SynapColors.OnError,
    errorContainer = SynapColors.ErrorContainer,
    onErrorContainer = SynapColors.OnErrorContainer,
    outline = SynapColors.OutlineStrong,
    outlineVariant = SynapColors.Outline,
    scrim = SynapColors.Scrim,
    surfaceBright = SynapColors.SurfaceHighest,
    surfaceDim = SynapColors.Bg,
    surfaceContainerLowest = SynapColors.Bg,
    surfaceContainerLow = SynapColors.Surface,
    surfaceContainer = SynapColors.Surface,
    surfaceContainerHigh = SynapColors.SurfaceHigh,
    surfaceContainerHighest = SynapColors.SurfaceHighest
)

/**
 * App theme. System bar colors are not set here: the activity is edge-to-edge with a dark
 * window theme (see themes.xml), so bars are transparent with light icons.
 */
@Composable
fun SynapCartTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SynapColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
