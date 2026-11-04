package com.neilb.synapcart.data.repository

import com.google.gson.GsonBuilder
import com.neilb.synapcart.data.model.AddFavoriteRequest
import com.neilb.synapcart.data.model.FavoriteUpdateRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.neilb.synapcart.data.model.FavoriteResponse
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.data.remote.ApiException
import com.neilb.synapcart.data.remote.FavoritesApiService
import com.neilb.synapcart.data.remote.requireSuccess
import com.neilb.synapcart.data.remote.safeApiCall
import com.neilb.synapcart.domain.repository.FavoritesRepository
import com.neilb.synapcart.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted

/** `target_price: null` must be sent explicitly to clear an alert; the default Gson drops nulls. */
private val nullsGson = GsonBuilder().serializeNulls().create()
private val JSON = "application/json; charset=utf-8".toMediaType()

class FavoritesRepositoryImpl(
    private val apiService: FavoritesApiService,
    private val sessionManager: SessionManager
) : FavoritesRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _favorites = MutableStateFlow<List<FavoriteResponse>>(emptyList())
    override val favorites: StateFlow<List<FavoriteResponse>> = _favorites.asStateFlow()

    override val favoriteLinks: StateFlow<Set<String>> = _favorites
        .map { list -> list.mapNotNullTo(HashSet()) { it.productLink } }
        .stateIn(scope, SharingStarted.Eagerly, emptySet())

    override suspend fun refresh(): Result<List<FavoriteResponse>> =
        safeApiCall { apiService.getFavorites() }.onSuccess { _favorites.value = it }

    override suspend fun add(product: ProductDTO): Result<FavoriteResponse> {
        val link = product.link
        if (link.isNullOrBlank()) return Result.failure(ApiException.Validation(com.neilb.synapcart.R.string.error_product_no_link))
        val request = AddFavoriteRequest(
            productTitle = product.title?.takeIf { it.isNotBlank() } ?: link,
            productLink = link,
            price = product.price,
            currency = sessionManager.currency.first(),
            source = product.source,
            thumbnailUrl = product.thumbnail
        )
        return safeApiCall { apiService.addFavorite(request) }.onSuccess { saved ->
            _favorites.update { current -> listOf(saved) + current.filterNot { it.id == saved.id || it.productLink == saved.productLink } }
        }
    }

    override suspend fun remove(favorite: FavoriteResponse): Result<Unit> {
        val before = _favorites.value
        _favorites.update { list -> list.filterNot { it.id == favorite.id } }
        return safeApiCall {
            apiService.removeFavorite(favorite.id).requireSuccess()
            Unit
        }.onFailure { _favorites.value = before }
    }

    override suspend fun toggle(product: ProductDTO): Result<Boolean> {
        val link = product.link
        if (link.isNullOrBlank()) return Result.failure(ApiException.Validation(com.neilb.synapcart.R.string.error_product_no_link))
        return if (link in favoriteLinks.value) {
            val before = _favorites.value
            _favorites.update { list -> list.filterNot { it.productLink == link } }
            safeApiCall {
                apiService.removeFavoriteByLink(link).requireSuccess()
                false
            }.onFailure { _favorites.value = before }
        } else {
            add(product).map { true }
        }
    }

    override suspend fun setTargetPrice(favoriteId: Int, targetPrice: Double?): Result<FavoriteResponse> =
        safeApiCall {
            val body = nullsGson.toJson(FavoriteUpdateRequest(targetPrice)).toRequestBody(JSON)
            apiService.updateFavorite(favoriteId, body)
        }.onSuccess { updated ->
            _favorites.update { list -> list.map { if (it.id == updated.id) updated else it } }
        }

    override fun clear() {
        _favorites.value = emptyList()
    }
}
