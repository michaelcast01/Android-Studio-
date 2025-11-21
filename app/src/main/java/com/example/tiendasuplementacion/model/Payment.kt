package com.example.tiendasuplementacion.model

import androidx.annotation.Keep

/**
 * Modelo de método de pago
 * @property id Identificador único del método de pago
 * @property name Nombre descriptivo del método de pago
 * @property method Tipo de método de pago (usando PaymentMethodType)
 * @property isActive Indica si el método de pago está activo
 */
@Keep
data class Payment(
    val id: Long = 0,
    val name: String,
    val method: String? = null,
    val isActive: Boolean = true
) {
    /**
     * Verifica si el método de pago requiere información de tarjeta
     */
    fun requiresCardInfo(): Boolean {
        val lowerName = name.lowercase()
        return lowerName in listOf(
            "credito", "credit", "credit_card", "credito_tarjeta",
            "debito", "debit", "debit_card", "debito_tarjeta",
            "tarjeta", "card"
        )
    }

    /**
     * Obtiene el tipo de método de pago
     */
    fun getPaymentMethodType(): PaymentMethodType {
        return PaymentMethodType.fromString(name) ?: PaymentMethodType.OTHER
    }
}

/**
 * Enumeración de tipos de métodos de pago soportados
 */
enum class PaymentMethodType(val displayName: String, val requiresCard: Boolean) {
    PSE("PSE", false),
    CREDIT_CARD("Tarjeta de Crédito", true),
    DEBIT_CARD("Tarjeta de Débito", true),
    CASH("Efectivo", false),
    BANK_TRANSFER("Transferencia Bancaria", false),
    DIGITAL_WALLET("Billetera Digital", false),
    OTHER("Otro", false);

    companion object {
        /**
         * Obtiene el tipo de pago desde un string
         */
        fun fromString(value: String): PaymentMethodType? {
            val normalized = value.lowercase().trim()
            return when {
                normalized.contains("pse") -> PSE
                normalized.contains("credit") || normalized.contains("credito") -> CREDIT_CARD
                normalized.contains("debit") || normalized.contains("debito") -> DEBIT_CARD
                normalized.contains("cash") || normalized.contains("efectivo") -> CASH
                normalized.contains("transfer") || normalized.contains("transferencia") -> BANK_TRANSFER
                normalized.contains("wallet") || normalized.contains("billetera") -> DIGITAL_WALLET
                else -> OTHER
            }
        }

        /**
         * Obtiene todos los tipos que requieren tarjeta
         */
        fun cardPaymentTypes(): List<PaymentMethodType> {
            return values().filter { it.requiresCard }
        }
    }
}

/**
 * Constantes de métodos de pago para retrocompatibilidad
 */
object PaymentMethods {
    const val PSE = "PSE"
    const val CREDIT_CARD = "CREDIT_CARD"
    const val DEBIT_CARD = "DEBIT_CARD"
    const val CASH = "CASH"
    const val BANK_TRANSFER = "BANK_TRANSFER"
    const val DIGITAL_WALLET = "DIGITAL_WALLET"

    /**
     * Lista de todos los métodos de pago disponibles
     */
    val ALL_METHODS = listOf(
        PSE, CREDIT_CARD, DEBIT_CARD, CASH, BANK_TRANSFER, DIGITAL_WALLET
    )
}
