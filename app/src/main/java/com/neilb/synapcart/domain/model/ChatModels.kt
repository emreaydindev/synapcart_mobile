package com.neilb.synapcart.domain.model

import com.neilb.synapcart.data.model.ProductDTO

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    /** Products the assistant found for this answer (empty for user messages). */
    val products: List<ProductDTO> = emptyList()
)
