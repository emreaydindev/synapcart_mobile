package com.neilb.synapcart.util

import androidx.annotation.StringRes
import com.neilb.synapcart.R

/** Mirrors the backend's `validate_new_password`: 8–72 bytes, with letters and digits. */
object PasswordRules {
    const val MIN_LENGTH = 8
    private const val MAX_BYTES = 72

    /** Returns the string resource of a user-facing error, or null if the password is acceptable. */
    @StringRes
    fun validate(password: String): Int? = when {
        password.length < MIN_LENGTH -> R.string.password_too_short
        password.toByteArray(Charsets.UTF_8).size > MAX_BYTES -> R.string.password_too_long
        password.none { it.isLetter() } || password.none { it.isDigit() } -> R.string.password_needs_letters_digits
        else -> null
    }
}
