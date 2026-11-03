package com.neilb.synapcart.data.model

import com.google.gson.annotations.SerializedName

data class SessionResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("user_id") val userId: String
)

data class SessionUpdateRequest(
    @SerializedName("title") val title: String
)
