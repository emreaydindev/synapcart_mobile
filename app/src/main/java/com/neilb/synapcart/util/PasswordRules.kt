package com.neilb.synapcart.util

import androidx.annotation.StringRes
import com.neilb.synapcart.data.remote.validationRes

/** Android wrapper over the shared [PasswordPolicy] (mirrors the backend's rules). */
object PasswordRules {
    const val MIN_LENGTH = PasswordPolicy.MIN_LENGTH

    /** Returns the string resource of a user-facing error, or null if the password is acceptable. */
    @StringRes
    fun validate(password: String): Int? = PasswordPolicy.check(password)?.let(::validationRes)
}
