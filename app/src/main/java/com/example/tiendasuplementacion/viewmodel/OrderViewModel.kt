package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Order
import com.example.tiendasuplementacion.repository.OrderRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
                _orders.value = list
                _uiState.value = UiState.Success(list)
            } catch (e: Exception) {
                val msg = e.message ?: "Error al cargar pedidos"
                _uiState.value = UiState.Error(msg)
            }
        }
    }

    suspend fun createOrder(order: Order): Order {
        return repository.create(order)
    }
}
