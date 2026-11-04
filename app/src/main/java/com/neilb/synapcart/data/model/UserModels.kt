package com.neilb.synapcart.data.model

import com.google.gson.annotations.SerializedName

data class UserProfileUpdateRequest(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("currency") val currency: String? = null
)

data class UserProfile(
    @SerializedName("id") val id: String?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("email") val email: String,
    @SerializedName("language") val language: String?,
    @SerializedName("currency") val currency: String?,
    // Effective plan ("free" | "premium") and today's agent usage.
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("plan_expires_at") val planExpiresAt: String? = null,
    @SerializedName("usage_today") val usageToday: Int? = null,
    @SerializedName("daily_limit") val dailyLimit: Int? = null
) {
    val isPremium: Boolean get() = plan == PLAN_PREMIUM

    companion object {
        const val PLAN_PREMIUM = "premium"
    }
}

data class PasswordChangeRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)

data class AccountDeleteRequest(
    @SerializedName("password") val password: String?
)
