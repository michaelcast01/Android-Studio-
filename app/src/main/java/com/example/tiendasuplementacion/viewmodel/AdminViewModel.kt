package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.example.tiendasuplementacion.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.tiendasuplementacion.model.Order
import java.util.concurrent.ConcurrentHashMap

class AdminViewModel(
    private val orderRepository: OrderRepository = OrderRepository(),
    private val externalScope: CoroutineScope? = null
) : ViewModel() {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _selected = ConcurrentHashMap<Long, Boolean>()
    val selectedOrders: Set<Long>
        get() = _selected.keys

    // Store last bulk action for undo
    private var lastBulkIds: List<Long> = emptyList()

    init {
        refresh()
    }

    fun refresh() {
        val scope = externalScope ?: viewModelScope
        scope.launch {
            try {
                val fetched = orderRepository.getAll()
                _orders.value = fetched
            } catch (e: Exception) {
                _orders.value = emptyList()
            }
        }
    }

    fun select(id: Long) {
        _selected[id] = true
    }

    fun deselect(id: Long) {
        _selected.remove(id)
    }

    fun clearSelection() {
        _selected.clear()
    }

    fun markSelectedAsShipped() {
        val ids = _selected.keys.toList()
        if (ids.isEmpty()) return
        lastBulkIds = ids
        val scope = externalScope ?: viewModelScope
        scope.launch {
            ids.forEach { id ->
                    try {
                        orderRepository.updateStatus(id, 3L) // assuming 3 = Shipped
                    } catch (ignored: Exception) {
                    }
            }
            refresh()
            clearSelection()
        }
    }

    fun undoLastBulkAction() {
        val ids = lastBulkIds
        if (ids.isEmpty()) return
        val scope = externalScope ?: viewModelScope
        scope.launch {
            ids.forEach { id ->
                try {
                    orderRepository.updateStatus(id, 1L) // revert to status 1 = Pending (example)
                } catch (ignored: Exception) {
                }
            }
            refresh()
            lastBulkIds = emptyList()
        }
    }
}
