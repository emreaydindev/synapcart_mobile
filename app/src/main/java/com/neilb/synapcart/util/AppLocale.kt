package com.neilb.synapcart.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * The app UI language (Turkish or English). The user's profile language is the source of
 * truth: it is applied with [apply] when it changes, and AppCompat persists it per app.
 * Must be called on the main thread.
 */
object AppLocale {
    const val TURKISH = "tr"
    const val ENGLISH = "en"
    val SUPPORTED = listOf(TURKISH, ENGLISH)

    /** The language the UI is shown in right now. */
    fun current(): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val tag = if (!appLocales.isEmpty) appLocales[0]?.language else Locale.getDefault().language
        return normalize(tag)
    }

    /** Maps any language to a supported one: Turkish stays Turkish, everything else is English. */
    fun normalize(language: String?): String = if (language == TURKISH) TURKISH else ENGLISH

    /** Switches the UI language; recreates visible activities only if it actually changes. */
    fun apply(language: String?) {
        if (language !in SUPPORTED) return
        val applied = AppCompatDelegate.getApplicationLocales()
        if (!applied.isEmpty && applied[0]?.language == language) return
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
    }
}
