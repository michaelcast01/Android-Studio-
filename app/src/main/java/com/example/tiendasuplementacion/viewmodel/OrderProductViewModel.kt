package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendasuplementacion.model.OrderProductDetail
import com.example.tiendasuplementacion.repository.OrderProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderProductViewModel : ViewModel() {
    private val repository = OrderProductRepository()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun createOrderProduct(orderProduct: OrderProductDetail) {
        viewModelScope.launch {
            try {
                repository.create(orderProduct)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al crear el detalle de la orden: ${e.message}"
            }
        }
    }
} 