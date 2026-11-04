package com.neilb.synapcart.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.floor
import kotlin.time.Clock
import kotlin.time.Instant

/** Reasons carried by `ApiException.Validation`; each platform maps them to localized text. */
object ValidationReasons {
    const val EMAIL_EMPTY = "email_empty"
    const val EMAIL_PASSWORD_EMPTY = "email_password_empty"
    const val NAME_EMPTY = "name_empty"
    const val PASSWORD_FIELDS_EMPTY = "password_fields_empty"
    const val PASSWORD_TOO_SHORT = "password_too_short"
    const val PASSWORD_TOO_LONG = "password_too_long"
    const val PASSWORD_NEEDS_LETTERS_DIGITS = "password_needs_letters_digits"
}

/** Mirrors the backend's password rules (src/lib/validation.ts): 8–72 bytes, with letters and digits. */
object PasswordPolicy {
    const val MIN_LENGTH = 8
    private const val MAX_BYTES = 72

    /** One of the `ValidationReasons.PASSWORD_*` reasons, or null if the password is acceptable. */
    fun check(password: String): String? = when {
        password.length < MIN_LENGTH -> ValidationReasons.PASSWORD_TOO_SHORT
        password.encodeToByteArray().size > MAX_BYTES -> ValidationReasons.PASSWORD_TOO_LONG
        password.none { it.isLetter() } || password.none { it.isDigit() } -> ValidationReasons.PASSWORD_NEEDS_LETTERS_DIGITS
        else -> null
    }
}

fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

/**
 * Parses a backend timestamp to epoch millis. The backend sends ISO-8601 UTC
 * (`2026-11-04T10:00:00.000Z`); older naive values (`2026-01-02T03:04:05.123456`) are UTC too.
 */
fun parseServerTimestamp(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return try {
        Instant.parse(value).toEpochMilliseconds()
    } catch (_: IllegalArgumentException) {
        try {
            LocalDateTime.parse(value).toInstant(TimeZone.UTC).toEpochMilliseconds()
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

/**
 * Parses a price typed by the user ("1299", "1.299,90", "1299.9", "1 299"). Returns null for
 * anything that isn't a positive number.
 */
fun parsePriceInput(text: String): Double? {
    var cleaned = text.trim().replace(" ", "").replace("\u00A0", "")
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
    price?.takeIf { it > 0 }?.let { floor(it * 0.9).coerceAtLeast(1.0) }

/** Relative-date buckets for the chat history drawer. */
enum class SessionGroup { TODAY, YESTERDAY, LAST_7_DAYS, LAST_30_DAYS, OLDER }

fun sessionGroupOf(
    createdAtMillis: Long?,
    nowMillis: Long = currentTimeMillis(),
    zone: TimeZone = TimeZone.currentSystemDefault()
): SessionGroup {
    if (createdAtMillis == null) return SessionGroup.OLDER
    val day = Instant.fromEpochMilliseconds(createdAtMillis).toLocalDateTime(zone).date
    val today = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(zone).date
    val days = day.daysUntil(today)
    return when {
        days <= 0 -> SessionGroup.TODAY
        days == 1 -> SessionGroup.YESTERDAY
        days < 7 -> SessionGroup.LAST_7_DAYS
        days < 30 -> SessionGroup.LAST_30_DAYS
        else -> SessionGroup.OLDER
    }
}
