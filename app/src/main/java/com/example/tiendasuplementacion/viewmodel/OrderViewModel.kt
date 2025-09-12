package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Order
import com.example.tiendasuplementacion.repository.OrderRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.tiendasuplementacion.repository.OrderPagingSource
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _uiState = MutableStateFlow<UiState<List<Order>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Order>>> = _uiState.asStateFlow()

    fun fetchOrders() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val list = repository.getAll()
                // Ordenar por fecha descendente si es posible, fallback por order_id
                val sorted = list.sortedWith(compareByDescending { o ->
                    parseOrderDateOrFallback(o.date_order, o.order_id)
                })
                _orders.value = sorted
                _uiState.value = UiState.Success(sorted)
            } catch (e: Exception) {
                val msg = e.message ?: "Error al cargar pedidos"
                _uiState.value = UiState.Error(msg)
            }
        }
    }

    private fun parseOrderDateOrFallback(dateStr: String?, orderId: Long): Instant {
        if (dateStr.isNullOrBlank()) return Instant.ofEpochMilli(orderId)
        val candidates = listOf(
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )
        for (fmt in candidates) {
            try {
                val temporal = fmt.parse(dateStr)
                return Instant.from(temporal)
            } catch (_: DateTimeParseException) {
            }
        }
        return try {
            Instant.ofEpochMilli(dateStr.toLong())
        } catch (_: Exception) {
            Instant.ofEpochMilli(orderId)
        }
    }

    suspend fun createOrder(order: Order): Order {
        return repository.create(order)
    }

    // Admin actions
    fun updateOrderStatusOptimistic(orderId: Long, newStatusId: Long) {
        viewModelScope.launch {
            // optimistic update in LiveData list
            val current = _orders.value ?: emptyList()
            val updated = current.map { if (it.order_id == orderId) it.copy(status_id = newStatusId) else it }
            _orders.value = updated

            try {
                withContext(Dispatchers.IO) { repository.updateStatus(orderId, newStatusId) }
            } catch (e: Exception) {
                // revert on error
                _orders.value = current
            }
        }
    }

    fun refundOrder(orderId: Long, amount: Double?, reason: String? = null, onResult: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val updated = withContext(Dispatchers.IO) { repository.refund(orderId, amount, reason) }
                // replace in list
                val current = _orders.value ?: emptyList()
                _orders.value = current.map { if (it.order_id == orderId) updated else it }
                onResult?.invoke(true, null)
            } catch (e: Exception) {
                onResult?.invoke(false, e.message)
            }
        }
    }

    fun assignTracking(orderId: Long, tracking: String, onResult: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val updated = withContext(Dispatchers.IO) { repository.assignTracking(orderId, tracking) }
                val current = _orders.value ?: emptyList()
                _orders.value = current.map { if (it.order_id == orderId) updated else it }
                onResult?.invoke(true, null)
            } catch (e: Exception) {
                onResult?.invoke(false, e.message)
            }
        }
    }

    // Paging + filters
    private val _filters = MutableStateFlow(FilterParams())
    val filters = _filters

    data class FilterParams(val statusId: Long? = null, val search: String? = null)

    // Expose paged flow for Compose
    val pagedOrdersFlow: Flow<PagingData<Order>> = _filters
        .flatMapLatest { f ->
            Pager<Int, Order>(PagingConfig(pageSize = 20, enablePlaceholders = false)) {
                OrderPagingSource(repository, f.statusId, f.search)
            }.flow.cachedIn(viewModelScope)
        }

    fun setFilter(statusId: Long? = null, search: String? = null) {
        _filters.value = FilterParams(statusId = statusId, search = search)
    }
}
