package com.neilb.synapcart.util

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * User-facing text that is resolved in the UI, so ViewModels and repositories never hold
 * hard-coded, single-language strings. [Raw] is text that is already localized (the backend's
 * `detail`, which it writes in the user's profile language).
 */
sealed interface UiText {
    data class Raw(val value: String) : UiText

    class Res(@param:StringRes val id: Int, vararg val args: Any) : UiText {
        override fun equals(other: Any?) = other is Res && other.id == id && other.args.contentEquals(args)
        override fun hashCode() = 31 * id + args.contentHashCode()
    }

    class Plural(@param:PluralsRes val id: Int, val count: Int, vararg val args: Any) : UiText {
        override fun equals(other: Any?) =
            other is Plural && other.id == id && other.count == count && other.args.contentEquals(args)
        override fun hashCode() = 31 * (31 * id + count) + args.contentHashCode()
    }

    fun resolve(context: Context): String = when (this) {
        is Raw -> value
        is Res -> context.getString(id, *args)
        is Plural -> context.resources.getQuantityString(id, count, *args)
    }
}

@Composable
fun UiText.asString(): String = resolve(LocalContext.current)
