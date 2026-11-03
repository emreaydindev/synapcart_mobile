package com.neilb.synapcart.util

import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.util.Currency
import java.util.Locale

/**
 * Parses a backend timestamp to epoch millis. The backend stores naive UTC
 * (`2026-01-02T03:04:05.123456`), but tolerate an explicit offset too.
 * java.time is available on API 24+ through core library desugaring.
 */
fun parseServerTimestamp(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return try {
        OffsetDateTime.parse(value).toInstant().toEpochMilli()
    } catch (_: DateTimeParseException) {
        try {
            LocalDateTime.parse(value).toInstant(ZoneOffset.UTC).toEpochMilli()
        } catch (_: DateTimeParseException) {
            null
        }
    }
}

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
