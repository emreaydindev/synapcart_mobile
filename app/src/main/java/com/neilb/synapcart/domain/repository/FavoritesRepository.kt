package com.neilb.synapcart.domain.repository

import com.neilb.synapcart.data.model.FavoriteResponse
import com.neilb.synapcart.data.model.ProductDTO
import kotlinx.coroutines.flow.StateFlow

/**
 * Favorites with an in-memory cache shared by every screen, so heart icons show the real
 * state everywhere and toggling is consistent.
 */
interface FavoritesRepository {
    /** Links of all favorited products (empty until [refresh] succeeds). */
    val favoriteLinks: StateFlow<Set<String>>
    val favorites: StateFlow<List<FavoriteResponse>>

    suspend fun refresh(): Result<List<FavoriteResponse>>
    suspend fun add(product: ProductDTO): Result<FavoriteResponse>
    suspend fun remove(favorite: FavoriteResponse): Result<Unit>
    /** Adds or removes by link; returns the new state (true = now a favorite). */
    suspend fun toggle(product: ProductDTO): Result<Boolean>
    fun clear()
}
