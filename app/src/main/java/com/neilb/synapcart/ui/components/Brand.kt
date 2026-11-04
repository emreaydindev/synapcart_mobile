package com.neilb.synapcart.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neilb.synapcart.R
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.ui.theme.SynapColors

/** Paints the content with the brand gradient (used for the logo mark and wordmark). */
fun Modifier.brandGradient() = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(SynapColors.BrandGradient, blendMode = BlendMode.SrcIn)
    }

/** The cart mark, in the brand gradient. Decorative unless [contentDescription] is given. */
@Composable
fun BrandMark(width: Dp, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Image(
        painter = painterResource(R.drawable.logo_mark),
        contentDescription = contentDescription,
        modifier = modifier
            .width(width)
            .height(width * (49.3f / 56f))
            .brandGradient()
    )
}

/** "SynapCart" in the brand gradient. */
@Composable
fun BrandWordmark(modifier: Modifier = Modifier, style: TextStyle = MaterialTheme.typography.headlineMedium) {
    Text(
        text = stringResource(R.string.app_name),
        style = style.copy(letterSpacing = 0.5.sp),
        color = SynapColors.Text,
        modifier = modifier.brandGradient()
    )
}

/** Compact logo + wordmark row, for top bars. */
@Composable
fun BrandLockup(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BrandMark(width = 26.dp)
        Spacer(Modifier.width(Dimens.SpaceS))
        BrandWordmark(style = MaterialTheme.typography.titleLarge)
    }
}

/** Shared header of the splash and auth screens: mark, wordmark, tagline / subtitle. */
@Composable
fun BrandHeader(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = stringResource(R.string.brand_tagline),
    markWidth: Dp = 88.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
    ) {
        BrandMark(width = markWidth, contentDescription = stringResource(R.string.cd_logo))
        Spacer(Modifier.height(Dimens.SpaceS))
        if (title == null) {
            BrandWordmark(style = MaterialTheme.typography.displaySmall, modifier = Modifier.semantics { heading() })
        } else {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )
        }
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
