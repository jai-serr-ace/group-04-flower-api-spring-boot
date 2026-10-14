package com.example.group04flowerapispringboot.model

data class User(
    val userId: Long,
    val username: String,
    val email: String,
    val providerId: String?,
    val isAdmin: Boolean
)
