package com.neilb.synapcart.data.model

import com.google.gson.annotations.SerializedName

data class AddFavoriteRequest(
    @SerializedName("title") val productTitle: String,
    @SerializedName("link") val productLink: String,
    @SerializedName("price") val price: String,
    @SerializedName("source") val source: String,
    @SerializedName("thumbnail") val thumbnailUrl: String?
)

data class FavoriteResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val productTitle: String?,
    @SerializedName("link") val productLink: String?,
    @SerializedName("price") val price: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("thumbnail") val thumbnailUrl: String?,
    @SerializedName("added_at") val addedAt: String
)