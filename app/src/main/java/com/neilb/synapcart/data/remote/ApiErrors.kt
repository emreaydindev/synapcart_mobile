package com.neilb.synapcart.data.remote

import androidx.annotation.StringRes
import com.neilb.synapcart.R
import com.neilb.synapcart.util.UiText
import com.neilb.synapcart.util.ValidationReasons

/**
 * Android presentation of the shared [ApiException]: the backend's `detail` when present
 * (already in the user's language), otherwise a localized fallback for the failure kind.
 */
val ApiException.uiText: UiText
    get() = detail?.let { UiText.Raw(it) } ?: UiText.Res(fallbackRes)

@get:StringRes
private val ApiException.fallbackRes: Int
    get() = when (this) {
        is ApiException.Network -> R.string.error_network
        is ApiException.Unauthorized -> R.string.error_unauthorized
        is ApiException.QuotaExceeded -> R.string.error_quota
        is ApiException.RateLimited -> R.string.error_rate_limited
        is ApiException.Http -> R.string.error_generic
        is ApiException.Validation -> validationRes(reason)
    }

/** String resource for a shared validation reason (`ValidationReasons`, `ApiException.REASON_*`). */
@StringRes
fun validationRes(reason: String): Int = when (reason) {
    ValidationReasons.EMAIL_EMPTY -> R.string.error_email_empty
    ValidationReasons.EMAIL_PASSWORD_EMPTY -> R.string.error_email_password_empty
    ValidationReasons.NAME_EMPTY -> R.string.error_name_empty
    ValidationReasons.PASSWORD_FIELDS_EMPTY -> R.string.error_password_fields_empty
    ValidationReasons.PASSWORD_TOO_SHORT -> R.string.password_too_short
    ValidationReasons.PASSWORD_TOO_LONG -> R.string.password_too_long
    ValidationReasons.PASSWORD_NEEDS_LETTERS_DIGITS -> R.string.password_needs_letters_digits
    ApiException.REASON_PRODUCT_NO_LINK -> R.string.error_product_no_link
    else -> R.string.error_generic
}

/** The user-facing message for any failure, using [fallback] for non-API errors. */
fun Throwable.toUiText(@StringRes fallback: Int = R.string.error_generic): UiText =
    (this as? ApiException)?.uiText ?: UiText.Res(fallback)
