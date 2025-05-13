package com.example.tiendasuplementacion.model


data class OrderDetail(
    val detail_order_id: Long = 0,
    val order_id: Long,
    val product_id: Long,
    val quantity: Int = 1,
    val price: Double,
    val productName: String
)
