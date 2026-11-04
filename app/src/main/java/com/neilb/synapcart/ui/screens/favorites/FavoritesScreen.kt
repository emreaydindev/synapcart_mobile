package com.neilb.synapcart.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neilb.synapcart.R
import com.neilb.synapcart.data.model.FavoriteResponse
import com.neilb.synapcart.ui.components.EmptyState
import com.neilb.synapcart.ui.components.ErrorState
import com.neilb.synapcart.ui.components.PrimaryButton
import com.neilb.synapcart.ui.components.ProductCard
import com.neilb.synapcart.ui.components.ProductCardSkeleton
import com.neilb.synapcart.ui.components.SynapScaffold
import com.neilb.synapcart.ui.components.SynapTopBar
import com.neilb.synapcart.ui.screens.favorites.FavoritesViewModel.Companion.toProduct
import com.neilb.synapcart.ui.theme.Dimens
import com.neilb.synapcart.util.asString

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigateBack: () -> Unit,
    onOpenProduct: (favorite: FavoriteResponse, all: List<FavoriteResponse>) -> Unit,
    onCompare: (List<FavoriteResponse>) -> Unit,
    onOpenPaywall: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadError by viewModel.loadError.collectAsStateWithLifecycle()
    val selection by viewModel.selection.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val selecting = selection != null

    SynapScaffold(
        topBar = {
            if (selecting) {
                SynapTopBar(
                    title = pluralStringResource(R.plurals.compare_selected, selection!!.size, selection!!.size),
                    navigationIcon = {
                        IconButton(onClick = viewModel::cancelSelection) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.action_cancel))
                        }
                    }
                )
            } else {
                SynapTopBar(
                    title = stringResource(R.string.favorites_title),
                    onBack = onNavigateBack,
                    actions = {
                        if (favorites.size >= 2) {
                            TextButton(onClick = { if (isPremium) viewModel.startSelection() else onOpenPaywall() }) {
                                Icon(Icons.AutoMirrored.Outlined.CompareArrows, contentDescription = null)
                                Text(stringResource(R.string.product_compare), modifier = Modifier.padding(start = Dimens.SpaceXs))
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (selecting) {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                    PrimaryButton(
                        text = stringResource(R.string.compare_action, selection!!.size),
                        onClick = {
                            onCompare(viewModel.selectedFavorites())
                            viewModel.cancelSelection()
                        },
                        enabled = selection!!.size >= 2,
                        leadingIcon = Icons.AutoMirrored.Outlined.CompareArrows,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            val error = loadError
            when {
                isLoading && favorites.isEmpty() -> FavoritesGrid {
                    items(4) { ProductCardSkeleton() }
                }
                favorites.isEmpty() && error != null -> ErrorState(
                    message = error.asString(),
                    onRetry = viewModel::refresh,
                    modifier = Modifier.padding(top = Dimens.SpaceXxxl)
                )
                favorites.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = stringResource(R.string.favorites_empty_title),
                    message = stringResource(R.string.favorites_empty_message),
                    modifier = Modifier.padding(top = Dimens.SpaceXxxl)
                )
                else -> FavoritesGrid {
                    items(favorites, key = { it.id }) { item ->
                        val isSelected = selection?.contains(item.id)
                        ProductCard(
                            product = item.toProduct(),
                            currency = item.currency ?: "TRY",
                            isFavorite = true,
                            onClick = {
                                if (selecting) viewModel.toggleSelected(item.id) else onOpenProduct(item, favorites)
                            },
                            onToggleFavorite = { viewModel.removeFavorite(item) },
                            selected = isSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesGrid(content: androidx.compose.foundation.lazy.grid.LazyGridScope.() -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        content = content
    )
}
