package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.tiendasuplementacion.model.OrderDetail
import com.example.tiendasuplementacion.repository.OrderDetailRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class OrderDetailViewModel : ViewModel() {
    private val repository = OrderDetailRepository()
    
    private val _details = MutableStateFlow<List<OrderDetail>>(emptyList())
    val details: StateFlow<List<OrderDetail>> = _details.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun fetchOrderDetails() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _details.value = repository.getAll()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar los detalles del pedido"
                viewModelScope.launch { _events.emit(UiEvent.ShowError(_error.value ?: "Error al cargar los detalles del pedido")) }
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
                viewModelScope.launch { _events.emit(UiEvent.ShowError(_error.value ?: "Error al crear el detalle del pedido")) }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
