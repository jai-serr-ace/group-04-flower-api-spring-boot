package com.example.group04flowerapispringboot.models


data class Default(
    val message: String
)
data class Flower(
    val name: String,
    val scientificName: String? = null,
    val color: String? = null,
    val notes: String? = null
)