package com.neilb.synapcart.data.model

import com.google.gson.annotations.SerializedName

data class UserProfileUpdateRequest(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("currency") val currency: String? = null
)

data class UserProfile(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("language") val language: String,
    @SerializedName("currency") val currency: String
)