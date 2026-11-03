package com.neilb.synapcart.data.model

import com.google.gson.annotations.SerializedName

/**
 * One SSE `data:` event from `POST /chat/agent/{id}/stream`.
 * status: routing → searching → thinking (+products) → completed (+analysis, products);
 * or out_of_scope (+analysis) / error (+analysis). Only final events carry analysis.
 */
data class AgentStreamEvent(
    @SerializedName("analysis") val analysis: String? = null,
    @SerializedName("products") val products: List<ProductDTO>? = null,
    @SerializedName("status") val status: String? = null
) {
    val isFinal: Boolean get() = status in FINAL_STATUSES

    companion object {
        const val ROUTING = "routing"
        const val SEARCHING = "searching"
        const val THINKING = "thinking"
        const val COMPLETED = "completed"
        const val OUT_OF_SCOPE = "out_of_scope"
        const val ERROR = "error"
        private val FINAL_STATUSES = setOf(COMPLETED, OUT_OF_SCOPE, ERROR)
    }
}
