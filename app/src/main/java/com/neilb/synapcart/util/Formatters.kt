package com.neilb.synapcart.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Formats a price in the given ISO currency for the current locale, e.g. "₺1.299,90". */
fun formatPrice(amount: Double?, currencyCode: String?, locale: Locale = Locale.getDefault()): String? {
    if (amount == null) return null
    val code = currencyCode?.uppercase() ?: SessionManager.DEFAULT_CURRENCY
    return try {
        NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance(code)
            maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
        }.format(amount)
    } catch (_: IllegalArgumentException) {
        "$amount $code"
    }
}

/** Medium date (e.g. "4 Kas 2026") in [locale]. */
fun formatDate(epochMillis: Long, locale: Locale = Locale.getDefault()): String =
    java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, locale).format(java.util.Date(epochMillis))

// parseServerTimestamp, parsePriceInput and suggestTargetPrice live in the shared module (util/Rules.kt).
