package com.neilb.synapcart.util

/** Mirrors the backend's `validate_new_password`: 8–72 bytes, with letters and digits. */
object PasswordRules {
    const val MIN_LENGTH = 8
    private const val MAX_BYTES = 72

    /** Returns a user-facing error, or null if the password is acceptable. */
    fun validate(password: String): String? = when {
        password.length < MIN_LENGTH -> "Şifre en az $MIN_LENGTH karakter olmalıdır."
        password.toByteArray(Charsets.UTF_8).size > MAX_BYTES -> "Şifre çok uzun."
        password.none { it.isLetter() } || password.none { it.isDigit() } ->
            "Şifre hem harf hem rakam içermelidir."
        else -> null
    }
}
