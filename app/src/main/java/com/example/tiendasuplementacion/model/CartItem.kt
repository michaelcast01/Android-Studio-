package com.example.tiendasuplementacion.model

data class CartItem(
    val product: Product,
    var quantity: Int = 1
)
