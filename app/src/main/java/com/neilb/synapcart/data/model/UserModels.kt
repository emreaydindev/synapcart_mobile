package com.neilb.synapcart.data.model

import com.google.gson.annotations.SerializedName

data class UserProfileUpdateRequest(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("currency") val currency: String? = null
)