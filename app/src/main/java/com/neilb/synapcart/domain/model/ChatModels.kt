package com.neilb.synapcart.domain.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class Product(
    val title: String,
    val price: String,
    val source: String,
    val imageUrl: String,
    val link: String
)