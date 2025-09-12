package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.OrderDetail
import com.example.tiendasuplementacion.repository.OrderDetailRepository
import kotlinx.coroutines.launch

class OrderDetailViewModel : ViewModel() {
    private val repository = OrderDetailRepository()
    
    private val _details = MutableLiveData<List<OrderDetail>>()
    val details: LiveData<List<OrderDetail>> = _details

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchOrderDetails() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _details.value = repository.getAll()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar los detalles del pedido"
                _details.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createDetail(detail: OrderDetail) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.create(detail)
                fetchOrderDetails()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear el detalle del pedido"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
