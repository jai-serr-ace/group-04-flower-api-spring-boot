package com.example.group04flowerapispringboot.model


data class Flower(
    val name: String,
    val scientificName: String? = null,
    val locationCoord: String? = null,
    val notes: String? = null
)