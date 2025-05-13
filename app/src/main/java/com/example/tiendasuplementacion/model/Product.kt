package com.example.tiendasuplementacion.model


data class Product(
    val id: Long = 0,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val url_image: String
)
