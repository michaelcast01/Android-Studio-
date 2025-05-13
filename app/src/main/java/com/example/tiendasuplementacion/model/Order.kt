package com.example.tiendasuplementacion.model


import java.time.ZonedDateTime

data class Order(
    val order_id: Long,
    val total: Double,
    val date_order: String,
    val user_id: Long
)

