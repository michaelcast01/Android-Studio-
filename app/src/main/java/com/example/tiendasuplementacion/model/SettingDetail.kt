package com.example.tiendasuplementacion.model

data class SettingDetail(
    val id: Long,
    val name: String,
    val nickname: String,
    val phone: Long,
    val city: String,
    val address: String,
    val payments: List<Payment>
) 