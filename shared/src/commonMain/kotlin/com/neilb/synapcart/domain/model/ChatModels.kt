package com.neilb.synapcart.domain.model

import com.neilb.synapcart.data.model.ProductDTO
import com.neilb.synapcart.util.currentTimeMillis

data class ChatMessage(
    val id: String = kotlin.uuid.Uuid.random().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = currentTimeMillis(),
    /** Products the assistant found for this answer (empty for user messages). */
    val products: List<ProductDTO> = emptyList()
)
