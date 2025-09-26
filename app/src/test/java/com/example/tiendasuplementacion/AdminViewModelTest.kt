package com.example.tiendasuplementacion

import com.example.tiendasuplementacion.model.Order
import com.example.tiendasuplementacion.repository.OrderRepository
import com.example.tiendasuplementacion.viewmodel.AdminViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Unconfined
import org.junit.Assert.assertEquals
import org.junit.Test

class AdminViewModelTest {
    @Test
    fun testBulkMarkAndUndo() = runBlocking {
        val orders = mutableListOf(
            Order(order_id = 1, total = 10.0, date_order = "2025-09-24", user_id = 1, status_id = 1, total_products = 1, additional_info_payment_id = null),
            Order(order_id = 2, total = 20.0, date_order = "2025-09-24", user_id = 2, status_id = 1, total_products = 2, additional_info_payment_id = null)
        )

        val repo = object : OrderRepository() {
            override suspend fun getAll(): List<Order> = orders.toList()
            override suspend fun updateStatus(id: Long, statusId: Long): Order {
                val idx = orders.indexOfFirst { it.order_id == id }
                if (idx >= 0) {
                    orders[idx] = orders[idx].copy(status_id = statusId)
                    return orders[idx]
                }
                return Order(order_id = id, total = 0.0, date_order = "", user_id = 0, status_id = statusId, total_products = 0, additional_info_payment_id = null)
            }
        }

    val vm = AdminViewModel(orderRepository = repo, externalScope = CoroutineScope(Unconfined))

        // Select both and mark shipped
        vm.select(1)
        vm.select(2)
        vm.markSelectedAsShipped()

        // After marking, both should have status 3
        val updated = vm.orders.value
        assertEquals(2, updated.size)
        assertEquals(3L, updated[0].status_id)

        // Undo
        vm.undoLastBulkAction()
        val reverted = vm.orders.value
        assertEquals(1L, reverted[0].status_id)
    }
}
