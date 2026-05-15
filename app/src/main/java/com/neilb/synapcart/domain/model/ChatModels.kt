package com.neilb.synapcart.domain.model

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val products: List<Product> = emptyList(),
    val status: String = "completed"
)

data class Product(
    val title: String,
    val price: String,
    val source: String,
    val imageUrl: String,
    val link: String
)