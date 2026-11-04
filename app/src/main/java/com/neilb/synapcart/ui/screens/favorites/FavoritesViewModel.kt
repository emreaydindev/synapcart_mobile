package com.neilb.synapcart.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neilb.synapcart.R
import com.neilb.synapcart.data.model.FavoriteResponse
import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.data.remote.toUiText
import com.neilb.synapcart.domain.use_case.favorites.FavoritesUseCases
import com.neilb.synapcart.util.SessionManager
import com.neilb.synapcart.util.SnackbarController
import com.neilb.synapcart.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesUseCases: FavoritesUseCases,
    private val snackbarController: SnackbarController,
    sessionManager: SessionManager
) : ViewModel() {

    /** Shared cache, so removals here are reflected on every other screen. */
    val favorites: StateFlow<List<FavoriteResponse>> = favoritesUseCases.favorites

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadError = MutableStateFlow<UiText?>(null)
    val loadError: StateFlow<UiText?> = _loadError.asStateFlow()

    val isPremium: StateFlow<Boolean> = sessionManager.isPremium
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Non-null while picking favorites to compare. */
    private val _selection = MutableStateFlow<Set<Int>?>(null)
    val selection: StateFlow<Set<Int>?> = _selection.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            favoritesUseCases.refresh().fold(
                onSuccess = { _loadError.value = null },
                onFailure = { e -> _loadError.value = e.toUiText(R.string.error_favorites_load) }
            )
            _isLoading.value = false
        }
    }

    fun removeFavorite(favorite: FavoriteResponse) {
        viewModelScope.launch {
            favoritesUseCases.remove(favorite).fold(
                onSuccess = { snackbarController.showSnackbar(R.string.favorite_removed) },
                onFailure = { e -> snackbarController.showSnackbar(e.toUiText(R.string.error_favorite_update)) }
            )
        }
    }

    fun startSelection() {
        _selection.value = emptySet()
    }

    fun cancelSelection() {
        _selection.value = null
    }

    /** Toggles [id]; at most [MAX_COMPARE] can be picked. */
    fun toggleSelected(id: Int) {
        _selection.update { current ->
            val set = current ?: return@update null
            when {
                id in set -> set - id
                set.size >= MAX_COMPARE -> {
                    viewModelScope.launch { snackbarController.showSnackbar(UiText.Plural(R.plurals.compare_max, MAX_COMPARE, MAX_COMPARE)) }
                    set
                }
                else -> set + id
            }
        }
    }

    fun selectedFavorites(): List<FavoriteResponse> {
        val ids = _selection.value ?: return emptyList()
        return favorites.value.filter { it.id in ids }
    }

    companion object {
        const val MAX_COMPARE = 4

        fun FavoriteResponse.toProduct() = ProductDTO(
            title = productTitle,
            price = price,
            source = source,
            link = productLink,
            thumbnail = thumbnailUrl
        )
    }
}
