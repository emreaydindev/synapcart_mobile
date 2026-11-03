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
    @SerializedName("currency") val currency: String?
)

data class PasswordChangeRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)

data class AccountDeleteRequest(
    @SerializedName("password") val password: String?
)
