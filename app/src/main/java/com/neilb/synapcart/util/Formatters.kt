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

/** Medium date (e.g. "4 Kas 2026") in [locale]. */
fun formatDate(epochMillis: Long, locale: Locale = Locale.getDefault()): String =
    java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, locale).format(java.util.Date(epochMillis))

/**
 * Parses a price typed by the user ("1299", "1.299,90", "1299.9", "1 299"). Returns null for
 * anything that isn't a positive number.
 */
fun parsePriceInput(text: String): Double? {
    var cleaned = text.trim().replace(" ", "").replace(" ", "")
    if (cleaned.isEmpty()) return null
    val lastComma = cleaned.lastIndexOf(',')
    val lastDot = cleaned.lastIndexOf('.')
    cleaned = when {
        // Both present: the last one is the decimal separator.
        lastComma >= 0 && lastDot >= 0 ->
            if (lastComma > lastDot) cleaned.replace(".", "").replace(',', '.') else cleaned.replace(",", "")
        // Only commas: decimal if followed by 1-2 digits, otherwise thousands.
        lastComma >= 0 ->
            if (cleaned.length - lastComma - 1 in 1..2 && cleaned.count { it == ',' } == 1) cleaned.replace(',', '.')
            else cleaned.replace(",", "")
        // Only dots: thousands if several or followed by exactly 3 digits.
        lastDot >= 0 ->
            if (cleaned.count { it == '.' } > 1 || cleaned.length - lastDot - 1 == 3) cleaned.replace(".", "") else cleaned
        else -> cleaned
    }
    return cleaned.toDoubleOrNull()?.takeIf { it > 0 && it < 1e9 }
}

/** A suggested alert target: about 10% under [price], rounded down to a whole amount. */
fun suggestTargetPrice(price: Double?): Double? =
    price?.takeIf { it > 0 }?.let { kotlin.math.floor(it * 0.9).coerceAtLeast(1.0) }
