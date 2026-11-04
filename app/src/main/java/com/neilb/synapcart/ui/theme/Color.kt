package com.neilb.synapcart.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * The single SynapCart palette, derived from the logo. The app is dark-only.
 * Screens read colors through MaterialTheme.colorScheme; the few brand colors that have no
 * Material role (gradient, warning, premium gold) live in [SynapColors].
 */
object SynapColors {
    val Bg = Color(0xFF060B14)
    val Surface = Color(0xFF0E1624)
    val SurfaceHigh = Color(0xFF152034)
    val SurfaceHighest = Color(0xFF1B2840)
    val Outline = Color(0xFF22304A)
    val OutlineStrong = Color(0xFF34466A)

    val Primary = Color(0xFF2DD4B4)
    val OnPrimary = Color(0xFF032A23)
    val PrimaryContainer = Color(0xFF0B3B33)
    val OnPrimaryContainer = Color(0xFFA6F5E4)

    val Secondary = Color(0xFF33D6FF)
    val OnSecondary = Color(0xFF00283A)
    val SecondaryContainer = Color(0xFF0B3446)
    val OnSecondaryContainer = Color(0xFFB8ECFF)

    val Error = Color(0xFFF87171)
    val OnError = Color(0xFF3B0A0A)
    val ErrorContainer = Color(0xFF4A1515)
    val OnErrorContainer = Color(0xFFFFD6D6)

    val Warning = Color(0xFFF59E0B)
    val PremiumGold = Color(0xFFF5C451)
    val OnPremiumGold = Color(0xFF2E2205)

    val Text = Color(0xFFE8EEF8)

    /** #9AA8C1 on [Bg] is ~8:1, well above WCAG AA. */
    val TextMuted = Color(0xFF9AA8C1)

    val Scrim = Color(0xCC03060B)

    /** Brand gradient: primary teal into cyan. */
    val BrandGradient: Brush = Brush.linearGradient(listOf(Primary, Secondary))
    val BrandGradientHorizontal: Brush = Brush.horizontalGradient(listOf(Primary, Secondary))
    val PremiumGradient: Brush = Brush.linearGradient(listOf(Color(0xFFF5C451), Color(0xFFF59E0B)))
}
