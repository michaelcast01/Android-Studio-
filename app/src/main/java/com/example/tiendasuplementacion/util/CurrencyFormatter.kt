package com.example.tiendasuplementacion.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    
    fun format(amount: Double): String {
        return currencyFormat.format(amount)
    }
    
    fun format(amount: Float): String {
        return currencyFormat.format(amount.toDouble())
    }
}