package com.example.tiendasuplementacion.model


data class User(
    val id: Long = 0,
    val username: String,
    val email: String,
    val password: String,
    val role_id: Long,
    val setting_id: Long? = null
)
