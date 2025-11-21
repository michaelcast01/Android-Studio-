package com.example.tiendasuplementacion.util

import java.util.regex.Pattern

/**
 * Utilidades de validación para campos de pago
 */
object PaymentValidation {

    /**
     * Resultado de validación
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success() = ValidationResult(true, null)
            fun error(message: String) = ValidationResult(false, message)
        }
    }

    /**
     * Configuración de límites para campos de pago
     */
    object FieldLimits {
        const val CARD_NUMBER_MIN = 13
        const val CARD_NUMBER_MAX = 19
        const val CVC_MIN = 3
        const val CVC_MAX = 4
        const val CARDHOLDER_NAME_MIN = 3
        const val CARDHOLDER_NAME_MAX = 100
        const val ADDRESS_MIN = 5
        const val ADDRESS_MAX = 200
        const val CITY_MIN = 2
        const val CITY_MAX = 50
        const val STATE_MIN = 2
        const val STATE_MAX = 50
        const val POSTAL_CODE_MIN = 3
        const val POSTAL_CODE_MAX = 10
        const val COUNTRY_MIN = 2
        const val COUNTRY_MAX = 50
        const val PAYMENT_NAME_MIN = 2
        const val PAYMENT_NAME_MAX = 50
    }

    /**
     * Valida el número de tarjeta de crédito/débito usando el algoritmo de Luhn
     */
    fun validateCardNumber(cardNumber: String): ValidationResult {
        // Eliminar espacios y guiones
        val cleaned = cardNumber.replace(Regex("[\\s-]"), "")

        // Verificar que solo contenga dígitos
        if (!cleaned.matches(Regex("^\\d+$"))) {
            return ValidationResult.error("El número de tarjeta solo debe contener dígitos")
        }

        // Verificar longitud
        if (cleaned.length < FieldLimits.CARD_NUMBER_MIN) {
            return ValidationResult.error("El número de tarjeta debe tener al menos ${FieldLimits.CARD_NUMBER_MIN} dígitos")
        }

        if (cleaned.length > FieldLimits.CARD_NUMBER_MAX) {
            return ValidationResult.error("El número de tarjeta no debe exceder ${FieldLimits.CARD_NUMBER_MAX} dígitos")
        }

        // Algoritmo de Luhn
        var sum = 0
        var alternate = false
        for (i in cleaned.length - 1 downTo 0) {
            var digit = cleaned[i].toString().toInt()
            if (alternate) {
                digit *= 2
                if (digit > 9) {
                    digit -= 9
                }
            }
            sum += digit
            alternate = !alternate
        }

        return if (sum % 10 == 0) {
            ValidationResult.success()
        } else {
            ValidationResult.error("El número de tarjeta no es válido")
        }
    }

    /**
     * Valida y formatea la fecha de expiración (MM/AA o MM/YYYY)
     */
    fun validateExpirationDate(expirationDate: String): ValidationResult {
        val cleaned = expirationDate.replace("/", "").trim()

        // Si está vacío, no validar aún
        if (cleaned.isEmpty()) {
            return ValidationResult.error("Ingrese la fecha de vencimiento")
        }

        // Si tiene menos de 4 dígitos, aún está incompleto
        if (cleaned.length < 4) {
            return ValidationResult.error("Formato incompleto. Use MM/AA")
        }

        // Verificar formato MM/AA o MMYY
        if (!cleaned.matches(Regex("^\\d{4}$")) && !cleaned.matches(Regex("^\\d{6}$"))) {
            return ValidationResult.error("Formato inválido. Use MM/AA o MM/AAAA")
        }

        val month = cleaned.substring(0, 2).toIntOrNull()
        val yearPart = if (cleaned.length == 4) {
            cleaned.substring(2, 4)
        } else {
            cleaned.substring(2, 6)
        }

        // Validar mes
        if (month == null || month < 1 || month > 12) {
            return ValidationResult.error("Mes inválido. Debe estar entre 01 y 12")
        }

        // Validar año
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        
        val year = if (yearPart.length == 2) {
            2000 + yearPart.toInt()
        } else {
            yearPart.toInt()
        }

        if (year < currentYear || (year == currentYear && month < currentMonth)) {
            return ValidationResult.error("La tarjeta ha expirado")
        }

        if (year > currentYear + 20) {
            return ValidationResult.error("La fecha de expiración es demasiado lejana")
        }

        return ValidationResult.success()
    }

    /**
     * Valida el código CVC/CVV
     */
    fun validateCVC(cvc: String): ValidationResult {
        val cleaned = cvc.trim()

        if (!cleaned.matches(Regex("^\\d+$"))) {
            return ValidationResult.error("El CVC solo debe contener dígitos")
        }

        if (cleaned.length < FieldLimits.CVC_MIN || cleaned.length > FieldLimits.CVC_MAX) {
            return ValidationResult.error("El CVC debe tener entre ${FieldLimits.CVC_MIN} y ${FieldLimits.CVC_MAX} dígitos")
        }

        return ValidationResult.success()
    }

    /**
     * Valida el nombre del titular de la tarjeta
     */
    fun validateCardholderName(name: String): ValidationResult {
        val cleaned = name.trim()

        if (cleaned.length < FieldLimits.CARDHOLDER_NAME_MIN) {
            return ValidationResult.error("El nombre debe tener al menos ${FieldLimits.CARDHOLDER_NAME_MIN} caracteres")
        }

        if (cleaned.length > FieldLimits.CARDHOLDER_NAME_MAX) {
            return ValidationResult.error("El nombre no debe exceder ${FieldLimits.CARDHOLDER_NAME_MAX} caracteres")
        }

        // Solo permitir letras, espacios, guiones y apóstrofos
        if (!cleaned.matches(Regex("^[a-zA-ZÀ-ÿ\\s'-]+$"))) {
            return ValidationResult.error("El nombre solo debe contener letras, espacios, guiones y apóstrofos")
        }

        return ValidationResult.success()
    }

    /**
     * Valida la dirección
     */
    fun validateAddress(address: String): ValidationResult {
        val cleaned = address.trim()

        if (cleaned.length < FieldLimits.ADDRESS_MIN) {
            return ValidationResult.error("La dirección debe tener al menos ${FieldLimits.ADDRESS_MIN} caracteres")
        }

        if (cleaned.length > FieldLimits.ADDRESS_MAX) {
            return ValidationResult.error("La dirección no debe exceder ${FieldLimits.ADDRESS_MAX} caracteres")
        }

        return ValidationResult.success()
    }

    /**
     * Valida la ciudad
     */
    fun validateCity(city: String): ValidationResult {
        val cleaned = city.trim()

        if (cleaned.length < FieldLimits.CITY_MIN) {
            return ValidationResult.error("La ciudad debe tener al menos ${FieldLimits.CITY_MIN} caracteres")
        }

        if (cleaned.length > FieldLimits.CITY_MAX) {
            return ValidationResult.error("La ciudad no debe exceder ${FieldLimits.CITY_MAX} caracteres")
        }

        // Solo permitir letras, espacios y algunos caracteres especiales
        if (!cleaned.matches(Regex("^[a-zA-ZÀ-ÿ\\s.'-]+$"))) {
            return ValidationResult.error("La ciudad contiene caracteres inválidos")
        }

        return ValidationResult.success()
    }

    /**
     * Valida el estado/provincia
     */
    fun validateState(state: String): ValidationResult {
        val cleaned = state.trim()

        if (cleaned.length < FieldLimits.STATE_MIN) {
            return ValidationResult.error("El estado debe tener al menos ${FieldLimits.STATE_MIN} caracteres")
        }

        if (cleaned.length > FieldLimits.STATE_MAX) {
            return ValidationResult.error("El estado no debe exceder ${FieldLimits.STATE_MAX} caracteres")
        }

        return ValidationResult.success()
    }

    /**
     * Valida el código postal
     */
    fun validatePostalCode(postalCode: String): ValidationResult {
        val cleaned = postalCode.trim()

        if (cleaned.length < FieldLimits.POSTAL_CODE_MIN) {
            return ValidationResult.error("El código postal debe tener al menos ${FieldLimits.POSTAL_CODE_MIN} caracteres")
        }

        if (cleaned.length > FieldLimits.POSTAL_CODE_MAX) {
            return ValidationResult.error("El código postal no debe exceder ${FieldLimits.POSTAL_CODE_MAX} caracteres")
        }

        // Permitir dígitos, letras y guiones
        if (!cleaned.matches(Regex("^[a-zA-Z0-9\\s-]+$"))) {
            return ValidationResult.error("El código postal contiene caracteres inválidos")
        }

        return ValidationResult.success()
    }

    /**
     * Valida el país
     */
    fun validateCountry(country: String): ValidationResult {
        val cleaned = country.trim()

        if (cleaned.length < FieldLimits.COUNTRY_MIN) {
            return ValidationResult.error("El país debe tener al menos ${FieldLimits.COUNTRY_MIN} caracteres")
        }

        if (cleaned.length > FieldLimits.COUNTRY_MAX) {
            return ValidationResult.error("El país no debe exceder ${FieldLimits.COUNTRY_MAX} caracteres")
        }

        return ValidationResult.success()
    }

    /**
     * Valida el nombre del método de pago
     */
    fun validatePaymentName(name: String): ValidationResult {
        val cleaned = name.trim()

        if (cleaned.isEmpty()) {
            return ValidationResult.error("El nombre del método de pago es requerido")
        }

        if (cleaned.length < FieldLimits.PAYMENT_NAME_MIN) {
            return ValidationResult.error("El nombre debe tener al menos ${FieldLimits.PAYMENT_NAME_MIN} caracteres")
        }

        if (cleaned.length > FieldLimits.PAYMENT_NAME_MAX) {
            return ValidationResult.error("El nombre no debe exceder ${FieldLimits.PAYMENT_NAME_MAX} caracteres")
        }

        return ValidationResult.success()
    }

    /**
     * Formatea el número de tarjeta con espacios cada 4 dígitos
     */
    fun formatCardNumber(cardNumber: String): String {
        val cleaned = cardNumber.replace(Regex("[\\s-]"), "")
        return cleaned.chunked(4).joinToString(" ")
    }

    /**
     * Formatea la fecha de expiración a formato MM/AA
     */
    fun formatExpirationDate(expirationDate: String): String {
        val cleaned = expirationDate.replace("/", "")
        if (cleaned.length >= 2) {
            val month = cleaned.substring(0, 2)
            val year = if (cleaned.length > 2) {
                cleaned.substring(2, minOf(4, cleaned.length))
            } else {
                ""
            }
            return if (year.isNotEmpty()) "$month/$year" else month
        }
        return cleaned
    }

    /**
     * Enmascara el número de tarjeta mostrando solo los últimos 4 dígitos
     */
    fun maskCardNumber(cardNumber: String): String {
        val cleaned = cardNumber.replace(Regex("[\\s-]"), "")
        if (cleaned.length < 4) return "****"
        
        val lastFour = cleaned.takeLast(4)
        val masked = "*".repeat(cleaned.length - 4)
        return formatCardNumber(masked + lastFour)
    }
}
