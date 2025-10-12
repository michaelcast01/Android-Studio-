package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendasuplementacion.model.CreateOrderProductRequest
import com.example.tiendasuplementacion.model.OrderProductDetail
import com.example.tiendasuplementacion.model.UpdateOrderProductRequest
import com.example.tiendasuplementacion.repository.OrderProductRepository
import com.example.tiendasuplementacion.utils.OrderProductError
import com.example.tiendasuplementacion.utils.OrderProductErrorHandler
import com.example.tiendasuplementacion.utils.toOrderProductError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class OrderProductViewModel : ViewModel() {
    private val repository = OrderProductRepository()
    
    private val _error = MutableStateFlow<OrderProductError?>(null)
    val error: StateFlow<OrderProductError?> = _error

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _orderProduct = MutableStateFlow<OrderProductDetail?>(null)
    val orderProduct: StateFlow<OrderProductDetail?> = _orderProduct

    /**
     * Crea un nuevo detalle de pedido.
     * El backend automáticamente reduce el stock del producto.
     */
    suspend fun createOrderProduct(request: CreateOrderProductRequest): OrderProductDetail? {
        return try {
            _loading.value = true
            _error.value = null
            
            val result = repository.create(request)
            _orderProduct.value = result
            result
        } catch (e: Exception) {
            _error.value = e.toOrderProductError()
            viewModelScope.launch { _events.emit(UiEvent.ShowError("Error en operación de OrderProduct: ${e.message}")) }
            null
        } finally {
            _loading.value = false
        }
    }

    /**
     * Actualiza un detalle de pedido existente.
     * El backend ajusta automáticamente el stock por la diferencia.
     */
    suspend fun updateOrderProduct(id: Long, request: UpdateOrderProductRequest): OrderProductDetail? {
        return try {
            _loading.value = true
            _error.value = null
            
            val result = repository.update(id, request)
            _orderProduct.value = result
            result
        } catch (e: Exception) {
            _error.value = e.toOrderProductError()
            viewModelScope.launch { _events.emit(UiEvent.ShowError("Error en operación de OrderProduct: ${e.message}")) }
            null
        } finally {
            _loading.value = false
        }
    }

    /**
     * Elimina un detalle de pedido.
     * El backend automáticamente restaura el stock sumando la cantidad.
     */
    suspend fun deleteOrderProduct(id: Long): Boolean {
        return try {
            _loading.value = true
            _error.value = null
            
            val success = repository.delete(id)
            if (success) {
                _orderProduct.value = null
            }
            success
        } catch (e: Exception) {
            _error.value = e.toOrderProductError()
            viewModelScope.launch { _events.emit(UiEvent.ShowError("Error en operación de OrderProduct: ${e.message}")) }
            false
        } finally {
            _loading.value = false
        }
    }

    /**
     * Obtiene un detalle de pedido por su ID.
     */
    suspend fun getOrderProduct(id: Long): OrderProductDetail? {
        return try {
            _loading.value = true
            _error.value = null
            
            val result = repository.getById(id)
            _orderProduct.value = result
            result
        } catch (e: Exception) {
            _error.value = e.toOrderProductError()
            viewModelScope.launch { _events.emit(UiEvent.ShowError("Error en operación de OrderProduct: ${e.message}")) }
            null
        } finally {
            _loading.value = false
        }
    }

    /**
     * Actualiza solo la cantidad de un detalle de pedido.
     */
    suspend fun updateQuantity(id: Long, newQuantity: Int): OrderProductDetail? {
        return updateOrderProduct(id, UpdateOrderProductRequest(quantity = newQuantity))
    }

    /**
     * Limpia los errores.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Limpia el estado del order product.
     */
    fun clearOrderProduct() {
        _orderProduct.value = null
    }
} 