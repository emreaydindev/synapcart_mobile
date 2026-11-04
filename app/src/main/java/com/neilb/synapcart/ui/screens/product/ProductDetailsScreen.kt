package com.neilb.synapcart.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neilb.synapcart.R
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.ui.components.PremiumBadge
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.ProductImage
import com.neilb.synapcart.ui.components.SecondaryButton
import com.neilb.synapcart.ui.components.SynapScaffold
import com.neilb.synapcart.ui.components.SynapTopBar
import com.neilb.synapcart.ui.components.currentLocale
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.ui.theme.SynapColors
import com.neilb.synapcart.util.formatPrice

@Composable
fun ProductDetailsScreen(
    viewModel: ProductDetailsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToWeb: (String) -> Unit,
    onCompare: (List<ProductDTO>) -> Unit,
    onOpenPaywall: () -> Unit
) {
    val product = viewModel.product
    if (product == null) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()

    SynapScaffold(
        topBar = {
            SynapTopBar(
                title = stringResource(R.string.product_details_title),
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(if (isFavorite) R.string.cd_remove_favorite else R.string.cd_add_favorite),
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                PrimaryButton(
                    text = stringResource(R.string.product_go_to_store, product.source ?: stringResource(R.string.product_store_web)),
                    onClick = { product.link?.let(onNavigateToWeb) },
                    enabled = !product.link.isNullOrBlank(),
                    leadingIcon = Icons.AutoMirrored.Outlined.OpenInNew,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.SpaceL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(SynapColors.Text.copy(alpha = 0.96f)),
                contentAlignment = Alignment.Center
            ) {
                ProductImage(
                    url = product.thumbnail,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize().padding(Dimens.SpaceXl)
                )
            }

            Text(product.title ?: stringResource(R.string.product_unknown), style = MaterialTheme.typography.titleLarge)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatPrice(product.price, currency, currentLocale()) ?: stringResource(R.string.product_price_unknown),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.small)
                        .padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Storefront, contentDescription = null, modifier = Modifier.size(Dimens.IconS), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(Dimens.SpaceXs))
                    Text(product.source ?: stringResource(R.string.product_unknown_store), style = MaterialTheme.typography.labelLarge)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
                PremiumAction(
                    label = stringResource(R.string.product_price_alert),
                    icon = Icons.Outlined.NotificationsActive,
                    isPremium = isPremium,
                    onClick = { if (isPremium) viewModel.onPriceAlert() else onOpenPaywall() },
                    modifier = Modifier.weight(1f)
                )
                if (viewModel.canCompare) {
                    PremiumAction(
                        label = stringResource(R.string.product_compare),
                        icon = Icons.AutoMirrored.Outlined.CompareArrows,
                        isPremium = isPremium,
                        onClick = { if (isPremium) onCompare(viewModel.compareCandidates) else onOpenPaywall() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(Dimens.SpaceL), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(Dimens.SpaceM))
                    Text(
                        stringResource(R.string.product_found_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.size(Dimens.SpaceS))
        }
    }
}

/** An action that is premium-only; free users see the badge and go to the paywall. */
@Composable
private fun PremiumAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPremium: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        SecondaryButton(text = label, onClick = onClick, leadingIcon = icon)
        if (!isPremium) {
            PremiumBadge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 0.dp)
                    .offset(y = (-10).dp)
            )
        }
    }
}
