package com.neilb.synapcart.util

import androidx.annotation.StringRes
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/** App-wide snackbar events. Messages are [UiText] so they are resolved in the current locale. */
class SnackbarController {

    private val _snackbarEvent = Channel<UiText>(Channel.BUFFERED)
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    suspend fun showSnackbar(message: UiText) {
        _snackbarEvent.send(message)
    }

    suspend fun showSnackbar(@StringRes id: Int, vararg args: Any) {
        _snackbarEvent.send(UiText.Res(id, *args))
    }
}
