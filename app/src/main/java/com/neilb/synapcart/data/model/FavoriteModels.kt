package com.neilb.synapcart.data.model

import com.google.gson.annotations.SerializedName

data class AddFavoriteRequest(
    @SerializedName("product_title") val productTitle: String,
    @SerializedName("product_link") val productLink: String,
    @SerializedName("price") val price: String,
    @SerializedName("source") val source: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?
)

data class FavoriteResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("product_title") val productTitle: String,
    @SerializedName("product_link") val productLink: String,
    @SerializedName("price") val price: String,
    @SerializedName("source") val source: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    @SerializedName("added_at") val addedAt: String
)