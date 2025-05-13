package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Order
import com.example.tiendasuplementacion.repository.OrderRepository
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    fun fetchOrders() {
        viewModelScope.launch {
            _orders.value = repository.getAll()
        }
    }

    fun createOrder(order: Order) {
        viewModelScope.launch {
            repository.create(order)
            fetchOrders()
        }
    }
}
