package com.neilb.synapcart.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.neilb.synapcart.R

/** Plus Jakarta Sans, bundled (OFL, see OFL-PlusJakartaSans.txt) so it renders offline. */
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold)
)

private fun style(weight: FontWeight, size: Int, line: Int, tracking: Double = 0.0) = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp
)

/** The full M3 type scale. Screens use these styles instead of inline font sizes. */
val Typography = Typography(
    displayLarge = style(FontWeight.Bold, 57, 64, -0.25),
    displayMedium = style(FontWeight.Bold, 45, 52),
    displaySmall = style(FontWeight.Bold, 36, 44),
    headlineLarge = style(FontWeight.Bold, 32, 40),
    headlineMedium = style(FontWeight.Bold, 28, 36),
    headlineSmall = style(FontWeight.SemiBold, 24, 32),
    titleLarge = style(FontWeight.SemiBold, 22, 28),
    titleMedium = style(FontWeight.SemiBold, 16, 24, 0.1),
    titleSmall = style(FontWeight.SemiBold, 14, 20, 0.1),
    bodyLarge = style(FontWeight.Normal, 16, 24, 0.15),
    bodyMedium = style(FontWeight.Normal, 14, 21, 0.2),
    bodySmall = style(FontWeight.Normal, 12, 16, 0.3),
    labelLarge = style(FontWeight.SemiBold, 14, 20, 0.1),
    labelMedium = style(FontWeight.Medium, 12, 16, 0.4),
    labelSmall = style(FontWeight.Medium, 11, 16, 0.5)
)
