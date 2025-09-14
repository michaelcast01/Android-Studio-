package com.example.tiendasuplementacion.model

data class EmailVerification(
    val endpoints: Map<String, String>,
    val email_statuses: Map<String, String>,
    val service: String,
    val description: String,
    val verification_statuses: Map<String, String>,
    val version: String
)