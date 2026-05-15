package com.neilb.synapcart.data.model

import com.google.gson.annotations.SerializedName

data class AgentResponse(
    @SerializedName("analysis") val analysis: String,
    @SerializedName("products") val products: List<ProductDTO>,
    @SerializedName("status") val status: String // 'searching', 'out_of_scope', 'completed'
)

data class ProductDTO(
    @SerializedName("title") val title: String,
    @SerializedName("price") val price: Double?,
    @SerializedName("source") val source: String?,
    @SerializedName("link") val link: String?,
    @SerializedName("thumbnail") val thumbnail: String?
)

data class ChatRequest(
    @SerializedName("message") val message: String
)