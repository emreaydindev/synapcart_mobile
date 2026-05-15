package com.neilb.synapcart.data.model

import com.google.gson.annotations.SerializedName

data class SessionResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("user_id") val userId: String
)

data class MessageResponse(
    @SerializedName("role") val role: String, // 'user' or 'assistant'
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val createdAt: String
)