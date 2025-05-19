package com.example.tiendasuplementacion.model

data class UserDetail(
    val id: Long,
    val username: String,
    val email: String,
    val role: UserRole,
    val role_id: Long,
    val settings: Settings?,
    val setting_id: Long?,
    val orders: List<UserOrder>
)

data class UserRole(
    val id: Long,
    val name: String
)

data class Settings(
    val id: Long,
    val name: String,
    val nickname: String,
    val phone: Long,
    val city: String,
    val address: String,
    val payments: List<PaymentMethod>
)

data class PaymentMethod(
    val id: Long,
    val name: String
)

data class UserOrder(
    val order_id: Long,
    val date_order: String,
    val status: OrderStatus,
    val status_id: Long,
    val total_products: Int,
    val total: Double,
    val payment_id: Long?,
    val products: List<OrderProduct>
)

data class OrderStatus(
    val id: Long,
    val name: String
)

data class OrderProduct(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val url_image: String
) 