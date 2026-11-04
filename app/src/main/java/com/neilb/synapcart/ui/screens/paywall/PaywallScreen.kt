package com.neilb.synapcart.ui.screens.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neilb.synapcart.R
import com.neilb.synapcart.ui.components.BrandMark
import com.neilb.synapcart.ui.components.PremiumBadge
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.SynapScaffold
import com.neilb.synapcart.ui.components.SynapTopBar
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.ui.theme.SynapColors

/** A row of the free-vs-premium table. A null value is shown as "not included". */
private data class PlanRow(val label: Int, val free: String?, val premium: String?)

/**
 * Premium upsell. Purchasing arrives with Play Billing (roadmap Phase 5); until then the
 * plans are shown and the buy button is disabled with a "coming soon" label.
 */
@Composable
fun PaywallScreen(onNavigateBack: () -> Unit) {
    var yearly by rememberSaveable { mutableStateOf(true) }
    val included = stringResource(R.string.paywall_included)
    val unlimited = stringResource(R.string.paywall_unlimited)
    val rows = listOf(
        PlanRow(R.string.paywall_row_messages, "5", "100"),
        PlanRow(R.string.paywall_row_products, "3", "8"),
        PlanRow(R.string.paywall_row_deep, null, included),
        PlanRow(R.string.paywall_row_alerts, null, included),
        PlanRow(R.string.paywall_row_compare, null, included),
        PlanRow(R.string.paywall_row_history, stringResource(R.string.paywall_history_free), unlimited)
    )

    SynapScaffold(
        topBar = { SynapTopBar(onBack = onNavigateBack) },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
                ) {
                    PrimaryButton(
                        text = stringResource(R.string.paywall_coming_soon),
                        onClick = {},
                        enabled = false,
                        brush = SynapColors.PremiumGradient,
                        contentColor = SynapColors.OnPremiumGold
                    )
                    Text(
                        stringResource(R.string.paywall_legal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier
                    .widthIn(max = Dimens.ContentMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.SpaceL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
            ) {
                BrandMark(width = 72.dp)
                PremiumBadge()
                Text(
                    stringResource(R.string.paywall_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    stringResource(R.string.paywall_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                ComparisonTable(rows)
                Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
                    PlanOption(
                        title = stringResource(R.string.paywall_monthly),
                        caption = stringResource(R.string.paywall_monthly_caption),
                        selected = !yearly,
                        onClick = { yearly = false },
                        modifier = Modifier.weight(1f)
                    )
                    PlanOption(
                        title = stringResource(R.string.paywall_yearly),
                        caption = stringResource(R.string.paywall_yearly_caption),
                        selected = yearly,
                        onClick = { yearly = true },
                        modifier = Modifier.weight(1f),
                        highlight = stringResource(R.string.paywall_best_value)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonTable(rows: List<PlanRow>) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(vertical = Dimens.SpaceS)) {
            Row(Modifier.padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS)) {
                Text("", modifier = Modifier.weight(1.6f))
                Text(stringResource(R.string.plan_free), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.plan_premium), style = MaterialTheme.typography.labelLarge, color = SynapColors.PremiumGold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
            rows.forEachIndexed { index, row ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = Dimens.SpaceL))
                Row(
                    Modifier.padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(row.label), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1.6f))
                    Cell(row.free, premium = false, modifier = Modifier.weight(1f))
                    Cell(row.premium, premium = true, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Cell(value: String?, premium: Boolean, modifier: Modifier) {
    val included = stringResource(R.string.paywall_included)
    Box(modifier, contentAlignment = Alignment.Center) {
        when (value) {
            null -> Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.paywall_not_included), tint = MaterialTheme.colorScheme.outline)
            included -> Icon(Icons.Filled.Check, contentDescription = included, tint = if (premium) SynapColors.PremiumGold else MaterialTheme.colorScheme.primary)
            else -> Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                color = if (premium) SynapColors.PremiumGold else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PlanOption(
    title: String,
    caption: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlight: String? = null
) {
    Surface(
        modifier = modifier.selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = MaterialTheme.shapes.large,
        color = if (selected) SynapColors.PremiumGold.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) SynapColors.PremiumGold else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Dimens.SpaceL), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = SynapColors.PremiumGold, modifier = Modifier.size(Dimens.IconS))
            }
            Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (highlight != null) {
                Text(
                    highlight,
                    style = MaterialTheme.typography.labelSmall,
                    color = SynapColors.OnPremiumGold,
                    modifier = Modifier
                        .background(SynapColors.PremiumGold, MaterialTheme.shapes.small)
                        .padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceXxs)
                )
            }
        }
    }
}

