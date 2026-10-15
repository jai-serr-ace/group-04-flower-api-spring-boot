package com.example.group04flowerapispringboot.model

data class Flower(
    val flowerId: Long,
    val scientificName: String?,
    val userId: Long,
    val name: String,
    val locationCoord: String?,
    val notes: String?
)
