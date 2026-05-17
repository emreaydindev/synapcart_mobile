package com.neilb.synapcart.domain.model

data class User(
    val fullName: String,
    val email: String,
    val language: String,
    val currency: String
)