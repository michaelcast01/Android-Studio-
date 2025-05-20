package com.example.tiendasuplementacion.model

data class Payment(
    val id: Long = 0,
    val name: String,
    val method: String,
    val isActive: Boolean = true
)

object PaymentMethods {
    const val PSE = "PSE"
    const val CREDIT_CARD = "CREDIT_CARD"
    const val DEBIT_CARD = "DEBIT_CARD"
    const val CASH = "CASH"
}
