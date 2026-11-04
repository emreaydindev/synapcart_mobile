package com.neilb.synapcart.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neilb.synapcart.R
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.ui.theme.SynapColors

/** Animated loading placeholder. */
@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerX"
    )
    val brush = Brush.linearGradient(
        colors = listOf(SynapColors.SurfaceHigh, SynapColors.SurfaceHighest, SynapColors.SurfaceHigh),
        start = Offset(x, 0f),
        end = Offset(x + 400f, 200f)
    )
    Box(modifier.background(brush))
}

/** Lines of skeleton text, e.g. while chat history loads. */
@Composable
fun LoadingSkeleton(modifier: Modifier = Modifier, lines: Int = 3) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
        repeat(lines) { i ->
            ShimmerBox(
                Modifier
                    .fillMaxWidth(if (i % 2 == 0) 0.85f else 0.6f)
                    .height(if (i % 3 == 0) 56.dp else 18.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
        }
    }
}

/** Centered icon, title, message and optional action. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .widthIn(max = Dimens.ContentMaxWidth)
            .padding(Dimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(Dimens.SpaceL))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        if (message != null) {
            Spacer(Modifier.height(Dimens.SpaceS))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (action != null) {
            Spacer(Modifier.height(Dimens.SpaceXl))
            action()
        }
    }
}

/** Error with a retry button. */
@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.CloudOff,
        title = stringResource(R.string.error_title),
        message = message,
        modifier = modifier,
        action = {
            SecondaryButton(
                text = stringResource(R.string.action_retry),
                onClick = onRetry,
                leadingIcon = Icons.Outlined.Refresh,
                modifier = Modifier.widthIn(max = 240.dp)
            )
        }
    )
}
