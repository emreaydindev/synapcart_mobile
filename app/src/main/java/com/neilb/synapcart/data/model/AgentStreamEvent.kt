package com.neilb.synapcart.data.model

import com.google.gson.annotations.SerializedName

data class AgentStreamEvent(
    @SerializedName("analysis") val analysis: String? = null,
    @SerializedName("products") val products: List<ProductDTO>? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("debug") val debug: String? = null
)

