package com.example.tiendasuplementacion.model

data class OrderProductDetail(
    val id: Long = 0, // ID del OrderProduct (0 para nuevos registros)
    val orderId: Long? = null, // Nombre del campo del endpoint real
    val order_id: Long? = null, // Compatibilidad hacia atrás
    val product_id: Long? = null,
    val product: Product, // Producto completo desde el backend
    val quantity: Int = 1, // Por defecto 1 si se omite
    val price: Double = 0.0, // Precio usado en cálculos (puede ser 0)
    val unitPrice: Double? = null, // Campo del endpoint real
    val subtotal: Double? = null // Campo del endpoint real
) {
    // Propiedad computada para obtener el orderId correcto
    val effectiveOrderId: Long
        get() = orderId ?: order_id ?: 0L
}

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