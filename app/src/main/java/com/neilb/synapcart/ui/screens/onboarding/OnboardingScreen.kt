package com.neilb.synapcart.ui.screens.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neilb.synapcart.R
import com.neilb.synapcart.ui.components.BrandMark
import com.neilb.synapcart.ui.components.ChipGroup
import com.neilb.synapcart.ui.components.PremiumBadge
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.SectionHeader
import com.neilb.synapcart.ui.components.SynapScaffold
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.ui.theme.SynapColors
import com.neilb.synapcart.util.AppLocale
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 3

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit
) {
    val lang by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val curr by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isComplete by viewModel.isComplete.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState { PAGE_COUNT }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isComplete) {
        if (isComplete) onFinished()
    }

    val isLast = pagerState.currentPage == PAGE_COUNT - 1

    SynapScaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenPadding)
                    .navigationBarsPadding()
                    .padding(bottom = Dimens.SpaceL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
            ) {
                PageIndicator(count = PAGE_COUNT, current = pagerState.currentPage)
                PrimaryButton(
                    text = stringResource(if (isLast) R.string.onboarding_start else R.string.action_continue),
                    isLoading = isLoading,
                    onClick = {
                        if (isLast) viewModel.finishOnboarding()
                        else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier.widthIn(max = Dimens.ContentMaxWidth)
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalAlignment = Alignment.Top
        ) { page ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column(
                    modifier = Modifier
                        .widthIn(max = Dimens.ContentMaxWidth)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpaceXl),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
                ) {
                    when (page) {
                        0 -> WelcomePage()
                        1 -> PreferencesPage(lang, curr, viewModel::selectLanguage, viewModel::selectCurrency)
                        else -> PremiumPage(onSkip = viewModel::finishOnboarding)
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Spacer(Modifier.height(Dimens.SpaceXl))
    BrandMark(width = 96.dp)
    Spacer(Modifier.height(Dimens.SpaceS))
    PageTitle(stringResource(R.string.onboarding_welcome_title), stringResource(R.string.onboarding_welcome_subtitle))
    Spacer(Modifier.height(Dimens.SpaceS))
    FeatureRow(Icons.AutoMirrored.Outlined.ManageSearch, stringResource(R.string.onboarding_feature_search_title), stringResource(R.string.onboarding_feature_search_body))
    FeatureRow(Icons.Outlined.AutoAwesome, stringResource(R.string.onboarding_feature_analysis_title), stringResource(R.string.onboarding_feature_analysis_body))
    FeatureRow(Icons.Outlined.Storefront, stringResource(R.string.onboarding_feature_stores_title), stringResource(R.string.onboarding_feature_stores_body))
}

@Composable
private fun PreferencesPage(
    lang: String,
    curr: String,
    onLanguage: (String) -> Unit,
    onCurrency: (String) -> Unit
) {
    Spacer(Modifier.height(Dimens.SpaceXl))
    PageTitle(stringResource(R.string.onboarding_prefs_title), stringResource(R.string.onboarding_prefs_subtitle))
    Spacer(Modifier.height(Dimens.SpaceS))
    SectionHeader(stringResource(R.string.settings_language))
    ChipGroup(
        options = listOf(stringResource(R.string.language_turkish) to AppLocale.TURKISH, stringResource(R.string.language_english) to AppLocale.ENGLISH),
        selected = lang,
        onSelect = onLanguage
    )
    Spacer(Modifier.height(Dimens.SpaceS))
    SectionHeader(stringResource(R.string.settings_currency))
    ChipGroup(options = CURRENCY_OPTIONS, selected = curr, onSelect = onCurrency)
    Text(
        stringResource(R.string.onboarding_prefs_note),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Currency choices shared by onboarding and profile. */
val CURRENCY_OPTIONS = listOf("₺ TRY" to "TRY", "$ USD" to "USD", "€ EUR" to "EUR")

@Composable
private fun PremiumPage(onSkip: () -> Unit) {
    Spacer(Modifier.height(Dimens.SpaceXl))
    PremiumBadge()
    PageTitle(stringResource(R.string.onboarding_premium_title), stringResource(R.string.onboarding_premium_subtitle))
    Spacer(Modifier.height(Dimens.SpaceS))
    FeatureRow(Icons.Outlined.AutoAwesome, stringResource(R.string.premium_feature_deep_title), stringResource(R.string.premium_feature_deep_body), SynapColors.PremiumGold)
    FeatureRow(Icons.Outlined.NotificationsActive, stringResource(R.string.premium_feature_alerts_title), stringResource(R.string.premium_feature_alerts_body), SynapColors.PremiumGold)
    FeatureRow(Icons.AutoMirrored.Outlined.CompareArrows, stringResource(R.string.premium_feature_compare_title), stringResource(R.string.premium_feature_compare_body), SynapColors.PremiumGold)
    FeatureRow(Icons.Outlined.History, stringResource(R.string.premium_feature_history_title), stringResource(R.string.premium_feature_history_body), SynapColors.PremiumGold)
    Text(
        stringResource(R.string.onboarding_premium_note),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    TextButton(onClick = onSkip) { Text(stringResource(R.string.onboarding_premium_later)) }
}

@Composable
private fun PageTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun FeatureRow(icon: ImageVector, title: String, body: String, tint: Color = MaterialTheme.colorScheme.primary) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(44.dp)
                .background(tint.copy(alpha = 0.12f), MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint)
        }
        Spacer(Modifier.width(Dimens.SpaceL))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PageIndicator(count: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            val width by animateDpAsState(if (i == current) 24.dp else 8.dp, label = "dot")
            Box(
                Modifier
                    .height(8.dp)
                    .width(width)
                    .background(
                        if (i == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
            )
        }
    }
}
