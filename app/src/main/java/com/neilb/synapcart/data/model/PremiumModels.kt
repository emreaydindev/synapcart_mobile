package com.neilb.synapcart.data.model

import com.google.gson.annotations.SerializedName

data class VerifyPurchaseRequest(
    @SerializedName("purchase_token") val purchaseToken: String,
    @SerializedName("product_id") val productId: String
)

data class DeviceTokenRequest(
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String = "android"
)

/** Serialized with nulls kept: `target_price: null` clears the alert. */
data class FavoriteUpdateRequest(
    @SerializedName("target_price") val targetPrice: Double?
)

data class CompareProduct(
    @SerializedName("title") val title: String,
    @SerializedName("price") val price: Double?,
    @SerializedName("source") val source: String?,
    @SerializedName("link") val link: String?,
    @SerializedName("thumbnail") val thumbnail: String?
)

data class CompareRequest(
    @SerializedName("products") val products: List<CompareProduct>
)

data class CompareItem(
    @SerializedName("title") val title: String?,
    @SerializedName("link") val link: String?,
    @SerializedName("pros") val pros: List<String>?,
    @SerializedName("cons") val cons: List<String>?
)

data class CompareResponse(
    @SerializedName("items") val items: List<CompareItem>?,
    @SerializedName("verdict") val verdict: String?,
    @SerializedName("best_index") val bestIndex: Int?
)
