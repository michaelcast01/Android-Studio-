package com.example.tiendasuplementacion.model

data class PaymentDetail(
    val id: Long,
    val payment: Payment,
    val payment_id: Long,
    val user: User,
    val user_id: Long,
    val cardNumber: String?,
    val expirationDate: String?,
    val cvc: String?,
    val cardholderName: String?,
    val country: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val city: String?,
    val stateOrProvince: String?,
    val postalCode: String?
) 