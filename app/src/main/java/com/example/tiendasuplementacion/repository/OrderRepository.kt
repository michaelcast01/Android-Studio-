package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.Order
import com.example.tiendasuplementacion.network.RetrofitClient

class OrderRepository {
    private val service = RetrofitClient.orderService

    suspend fun getAll() = service.getAll()
    suspend fun getById(id: Long) = service.getById(id)
    suspend fun create(order: Order) = service.create(order)
    suspend fun update(id: Long, order: Order) = service.update(id, order)
    suspend fun delete(id: Long) = service.delete(id)

    // Admin helpers
    suspend fun updateStatus(id: Long, statusId: Long) = service.updateStatus(id, mapOf("status_id" to statusId))
    suspend fun refund(id: Long, amount: Double?, reason: String?) = service.refund(id, mapOf("amount" to (amount ?: 0.0), "reason" to (reason ?: "")))
    suspend fun assignTracking(id: Long, tracking: String) = service.assignTracking(id, mapOf("tracking" to tracking))

    // Paged fetch: if the backend supports page/size params this should call that endpoint.
    // Fallback: fetch all and slice client-side (only for small datasets).
    suspend fun getPaged(page: Int, size: Int, statusId: Long? = null, search: String? = null): List<Order> {
        return try {
            // Try calling service.getAll with params if signature supports it (not implemented in interface by default)
            // Fallback to client-side slicing
            val all = getAll()
            val filtered = all.filter { o ->
                val statusOk = statusId == null || o.status_id == statusId
                val searchOk = search.isNullOrBlank() || o.order_id.toString().contains(search) || o.user_id.toString().contains(search)
                statusOk && searchOk
            }
            val from = (page - 1) * size
            if (from >= filtered.size) emptyList() else filtered.subList(from, kotlin.math.min(filtered.size, from + size))
        } catch (e: Exception) {
            emptyList()
        }
    }
}

