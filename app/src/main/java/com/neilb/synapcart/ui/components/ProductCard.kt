package com.neilb.synapcart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.neilb.synapcart.R
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.ui.theme.SynapColors
import com.neilb.synapcart.util.formatPrice
import java.util.Locale

/** The current UI locale (follows the per-app language). */
@Composable
fun currentLocale(): Locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.ROOT

/**
 * A product tile: image with a placeholder, price chip, title, store and a favorite toggle with
 * a 48dp target. In selection mode ([selected] non-null) the tile toggles selection instead.
 */
@Composable
fun ProductCard(
    product: ProductDTO,
    currency: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: (() -> Unit)?,
    modifier: Modifier = Modifier,
    selected: Boolean? = null,
    hasAlert: Boolean = false
) {
    val price = formatPrice(product.price, currency, currentLocale())
    val title = product.title ?: stringResource(R.string.product_unknown)
    val isSelected = selected == true
    Surface(
        onClick = onClick,
        modifier = modifier.semantics {
            if (selected != null) this.selected = isSelected
        },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f)
                    .padding(Dimens.SpaceS)
                    .clip(MaterialTheme.shapes.medium)
                    .background(SynapColors.Text.copy(alpha = 0.96f))
            ) {
                ProductImage(url = product.thumbnail, modifier = Modifier.fillMaxSize().padding(Dimens.SpaceS))
                if (price != null) {
                    PriceChip(
                        price,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(Dimens.SpaceS)
                    )
                }
                if (selected != null) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else SynapColors.Bg.copy(alpha = 0.25f),
                        modifier = Modifier.align(Alignment.TopEnd).padding(Dimens.SpaceS).size(Dimens.IconM)
                    )
                } else if (hasAlert) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = stringResource(R.string.cd_price_alert_on),
                        tint = SynapColors.Warning,
                        modifier = Modifier.align(Alignment.TopEnd).padding(Dimens.SpaceS).size(Dimens.IconS)
                    )
                }
            }
            Row(
                modifier = Modifier.padding(start = Dimens.SpaceM, bottom = Dimens.SpaceXs),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(top = Dimens.SpaceXs, bottom = Dimens.SpaceS)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        product.source ?: stringResource(R.string.product_unknown_store),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (onToggleFavorite != null && selected == null) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = stringResource(
                                if (isFavorite) R.string.cd_remove_favorite else R.string.cd_add_favorite
                            ),
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/** Product image with a loading / missing placeholder. Shop images assume a light backdrop. */
@Composable
fun ProductImage(url: String?, modifier: Modifier = Modifier, contentDescription: String? = null) {
    if (url.isNullOrBlank()) {
        ImagePlaceholder(modifier)
        return
    }
    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier,
        loading = { ShimmerBox(Modifier.fillMaxSize()) },
        error = { ImagePlaceholder(Modifier.fillMaxSize()) }
    )
}

@Composable
private fun ImagePlaceholder(modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.Image, contentDescription = null, tint = SynapColors.Bg.copy(alpha = 0.3f))
    }
}

/** Price label on a dark pill. */
@Composable
fun PriceChip(price: String, modifier: Modifier = Modifier) {
    Text(
        price,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        modifier = modifier
            .background(SynapColors.Bg.copy(alpha = 0.92f), MaterialTheme.shapes.small)
            .padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceXs)
    )
}

/** Skeleton with the same footprint as [ProductCard]. */
@Composable
fun ProductCardSkeleton(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Dimens.SpaceS)) {
            ShimmerBox(Modifier.fillMaxWidth().aspectRatio(1.15f).clip(MaterialTheme.shapes.medium))
            ShimmerBox(Modifier.padding(top = Dimens.SpaceS).fillMaxWidth(0.9f).height(14.dp).clip(MaterialTheme.shapes.small))
            ShimmerBox(Modifier.padding(vertical = Dimens.SpaceS).fillMaxWidth(0.5f).height(12.dp).clip(MaterialTheme.shapes.small))
        }
    }
}

