package com.example.tiendasuplementacion.model

data class TestPaymentRequest(
    val amount: Int,
    val currency: String,
    val description: String,
    val customerEmail: String,
    val customerName: String,
    val testToken: String
)

data class TestPaymentResponse(
    val clientSecret: String? = null,
    val id: String? = null,
    val status: String? = null,
    val amount: Int? = null,
    val currency: String? = null,
    val description: String? = null,
    val error: String? = null,
    val message: String? = null
)

object TestTokens {
    const val CHARGE_DECLINED_INSUFFICIENT_FUNDS = "tok_chargeDeclinedInsufficientFunds"
    const val CHARGE_DECLINED = "tok_chargeDeclined"
    const val VISA = "tok_visa"
}