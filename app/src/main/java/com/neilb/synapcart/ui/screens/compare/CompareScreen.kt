package com.neilb.synapcart.ui.screens.compare

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neilb.synapcart.R
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.ui.components.ProductImage
import com.neilb.synapcart.ui.components.SecondaryButton
import com.neilb.synapcart.ui.components.SynapScaffold
import com.neilb.synapcart.ui.components.SynapTopBar
import com.neilb.synapcart.ui.components.currentLocale
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.ui.theme.SynapColors
import com.neilb.synapcart.util.formatPrice

@Composable
fun CompareScreen(
    viewModel: CompareViewModel,
    onNavigateBack: () -> Unit,
    onOpenProduct: (ProductDTO) -> Unit
) {
    val products = viewModel.products
    if (products.size < 2) {
        LaunchedEffect(Unit) { onNavigateBack() }
        return
    }
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    SynapScaffold(
        topBar = { SynapTopBar(title = stringResource(R.string.compare_title), onBack = onNavigateBack) }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = Dimens.SpaceS),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL)
        ) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
            // Two columns fill the width; more scroll horizontally.
            val columnWidth = ((maxWidth - Dimens.SpaceL * 2 - Dimens.SpaceS) / 2).coerceAtLeast(150.dp)
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.SpaceL),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS)
            ) {
                products.forEachIndexed { index, product ->
                    CompareColumn(
                        product = product,
                        currency = currency,
                        isCheapest = index == viewModel.cheapestIndex,
                        onOpen = { onOpenProduct(product) },
                        modifier = Modifier.width(columnWidth)
                    )
                }
            }
            }
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SpaceL)
            ) {
                Row(Modifier.padding(Dimens.SpaceL), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = SynapColors.PremiumGold)
                    Spacer(Modifier.width(Dimens.SpaceM))
                    Text(
                        stringResource(R.string.compare_ai_soon),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CompareColumn(
    product: ProductDTO,
    currency: String,
    isCheapest: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isCheapest) 2.dp else 1.dp, if (isCheapest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Dimens.SpaceM), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(SynapColors.Text.copy(alpha = 0.96f))
            ) {
                ProductImage(product.thumbnail, Modifier.fillMaxSize().padding(Dimens.SpaceS), product.title)
            }
            Box(Modifier.height(22.dp)) {
                if (isCheapest) {
                    Text(
                        stringResource(R.string.compare_cheapest),
                        style = MaterialTheme.typography.labelSmall,
                        color = SynapColors.OnPrimary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                            .padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceXxs)
                    )
                }
            }
            Text(
                product.title ?: stringResource(R.string.product_unknown),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(60.dp)
            )
            CompareField(stringResource(R.string.compare_price), formatPrice(product.price, currency, currentLocale()) ?: stringResource(R.string.product_price_unknown), highlight = true)
            CompareField(stringResource(R.string.compare_store), product.source ?: stringResource(R.string.product_unknown_store))
            SecondaryButton(text = stringResource(R.string.compare_details), onClick = onOpen)
        }
    }
}

@Composable
private fun CompareField(label: String, value: String, highlight: Boolean = false) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
