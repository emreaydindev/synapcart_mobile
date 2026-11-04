package com.neilb.synapcart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neilb.synapcart.R
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.ui.theme.SynapColors

/** Gold "Premium" pill. */
@Composable
fun PremiumBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(SynapColors.PremiumGradient, CircleShape)
            .padding(horizontal = Dimens.SpaceS + 2.dp, vertical = Dimens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = SynapColors.OnPremiumGold, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(Dimens.SpaceXs))
        Text(stringResource(R.string.plan_premium), style = MaterialTheme.typography.labelMedium, color = SynapColors.OnPremiumGold)
    }
}

/** The user's plan: the gold badge for premium, an outlined "Free" pill otherwise. */
@Composable
fun PlanBadge(isPremium: Boolean, modifier: Modifier = Modifier) {
    if (isPremium) {
        PremiumBadge(modifier)
    } else {
        Text(
            stringResource(R.string.plan_free),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                .padding(horizontal = Dimens.SpaceS + 2.dp, vertical = Dimens.SpaceXs)
        )
    }
}

/** "3/5 messages left today" with a progress bar and an upgrade action. */
@Composable
fun QuotaBanner(used: Int, limit: Int, onUpgrade: () -> Unit, modifier: Modifier = Modifier) {
    val remaining = (limit - used).coerceAtLeast(0)
    val low = remaining <= 1
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (low) SynapColors.Warning.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(start = Dimens.SpaceM, end = Dimens.SpaceXs, top = Dimens.SpaceXs, bottom = Dimens.SpaceXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = if (low) SynapColors.Warning else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.IconS)
            )
            Spacer(Modifier.width(Dimens.SpaceS))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
                Text(
                    pluralStringResource(R.plurals.quota_left, remaining, remaining, limit),
                    style = MaterialTheme.typography.labelLarge
                )
                LinearProgressIndicator(
                    progress = { if (limit > 0) used.coerceIn(0, limit) / limit.toFloat() else 0f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = if (low) SynapColors.Warning else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    drawStopIndicator = {}
                )
            }
            TextButton(onClick = onUpgrade) {
                Text(stringResource(R.string.action_upgrade), color = SynapColors.PremiumGold)
            }
        }
    }
}
