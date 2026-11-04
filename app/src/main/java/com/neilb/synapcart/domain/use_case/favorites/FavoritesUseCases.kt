package com.neilb.synapcart.domain.use_case.favorites

import com.neilb.synapcart.data.model.FavoriteResponse
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.domain.repository.FavoritesRepository

/** Re-fetches favorites from the server into the shared cache. */
class RefreshFavoritesUseCase(private val repository: FavoritesRepository) {
    suspend operator fun invoke() = repository.refresh()
}

/** Adds the product if it isn't a favorite, removes it otherwise. Result = new state. */
class ToggleFavoriteUseCase(private val repository: FavoritesRepository) {
    suspend operator fun invoke(product: ProductDTO) = repository.toggle(product)
}

class RemoveFavoriteUseCase(private val repository: FavoritesRepository) {
    suspend operator fun invoke(favorite: FavoriteResponse) = repository.remove(favorite)
}

/**
 * Sets a price alert on [product], favoriting it first if needed (the alert lives on the
 * favorite). [targetPrice] null removes the alert.
 */
class SetPriceAlertUseCase(private val repository: FavoritesRepository) {
    suspend operator fun invoke(product: ProductDTO, targetPrice: Double?): Result<FavoriteResponse> {
        val existing = repository.favorites.value.firstOrNull { it.productLink == product.link }
        val favorite = existing ?: repository.add(product).getOrElse { return Result.failure(it) }
        return repository.setTargetPrice(favorite.id, targetPrice)
    }
}

data class FavoritesUseCases(
    val refresh: RefreshFavoritesUseCase,
    val toggle: ToggleFavoriteUseCase,
    val remove: RemoveFavoriteUseCase,
    val setPriceAlert: SetPriceAlertUseCase,
    /** Shared cache: the favorites list and the set of favorited links. */
    val repository: FavoritesRepository
) {
    val favorites get() = repository.favorites
    val favoriteLinks get() = repository.favoriteLinks
}
