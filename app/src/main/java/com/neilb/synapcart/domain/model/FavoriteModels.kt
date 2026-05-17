package com.neilb.synapcart.domain.model

data class FavoriteProduct(
    val id: Int,
    val title: String,
    val price: String,
    val source: String,
    val imageUrl: String,
    val link: String
)