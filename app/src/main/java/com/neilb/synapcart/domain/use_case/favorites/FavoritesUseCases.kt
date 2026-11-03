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

data class FavoritesUseCases(
    val refresh: RefreshFavoritesUseCase,
    val toggle: ToggleFavoriteUseCase,
    val remove: RemoveFavoriteUseCase,
    /** Shared cache: the favorites list and the set of favorited links. */
    val repository: FavoritesRepository
) {
    val favorites get() = repository.favorites
    val favoriteLinks get() = repository.favoriteLinks
}
