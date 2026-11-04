package com.neilb.synapcart.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.neilb.synapcart.R
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.ui.theme.SynapColors

/**
 * The agent's progress for the in-flight message: analyze → search → compare. [step] is the
 * 0-based active step. The current label is a polite live region for TalkBack.
 */
@Composable
fun StatusIndicator(step: Int, modifier: Modifier = Modifier) {
    val labels = listOf(
        stringResource(R.string.agent_step_analyze),
        stringResource(R.string.agent_step_search),
        stringResource(R.string.agent_step_compare)
    )
    val active = step.coerceIn(0, labels.lastIndex)
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        AssistantAvatar()
        Spacer(Modifier.width(Dimens.SpaceS))
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp, 20.dp, 20.dp, 20.dp))
                .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
        ) {
            labels.forEachIndexed { index, label ->
                StepRow(
                    label = label,
                    state = when {
                        index < active -> StepState.DONE
                        index == active -> StepState.ACTIVE
                        else -> StepState.PENDING
                    },
                    modifier = if (index == active) {
                        Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    } else Modifier
                )
            }
        }
    }
}

private enum class StepState { DONE, ACTIVE, PENDING }

@Composable
private fun StepRow(label: String, state: StepState, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(18.dp), contentAlignment = Alignment.Center) {
            when (state) {
                StepState.DONE -> Box(
                    Modifier.size(18.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                }
                StepState.ACTIVE -> PulsingDot()
                StepState.PENDING -> Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.outline, CircleShape))
            }
        }
        Spacer(Modifier.width(Dimens.SpaceM))
        Text(
            label,
            style = if (state == StepState.ACTIVE) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            color = when (state) {
                StepState.ACTIVE -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun PulsingDot() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(0.7f, 1.25f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "pulseScale")
    val alpha by transition.animateFloat(1f, 0.5f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "pulseAlpha")
    Box(
        Modifier
            .size(10.dp)
            .scale(scale)
            .alpha(alpha)
            .background(SynapColors.BrandGradient, CircleShape)
    )
}
