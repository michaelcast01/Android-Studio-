package com.example.tiendasuplementacion.model

data class OrderProductDetail(
    val order_id: Long,
    val product_id: Long,
    val quantity: Int,
    val price: Double
) 