package com.example.tiendasuplementacion.model


data class Setting(
    val id: Long = 0,
    val payment_id: Long,
    val name: String,
    val nickname: String,
    val phone: Long,
    val city: String,
    val address: String
)
