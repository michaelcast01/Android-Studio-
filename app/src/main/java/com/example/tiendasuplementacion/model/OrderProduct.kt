package com.example.tiendasuplementacion.model

data class OrderProductDetail(
    val id: Long = 0, // ID del OrderProduct (0 para nuevos registros)
    val order_id: Long,
    val product_id: Long,
    val quantity: Int = 1, // Por defecto 1 si se omite
    val price: Double
)

// Clase para crear nuevos OrderProducts (sin ID)
data class CreateOrderProductRequest(
    val order_id: Long,
    val product_id: Long,
    val quantity: Int = 1,
    val price: Double
)

// Clase para actualizaciones parciales
data class UpdateOrderProductRequest(
    val order_id: Long? = null,
    val product_id: Long? = null,
    val quantity: Int? = null,
    val price: Double? = null
) 