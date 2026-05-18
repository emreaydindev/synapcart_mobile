package com.neilb.synapcart.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class SnackbarController {

    private val _snackbarEvent = Channel<String>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    suspend fun showSnackbar(message: String) {
        _snackbarEvent.send(message)
    }
}