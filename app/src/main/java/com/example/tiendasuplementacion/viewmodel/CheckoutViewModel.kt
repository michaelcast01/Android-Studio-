package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendasuplementacion.model.CartItem
import com.example.tiendasuplementacion.model.CreateOrderProductRequest
import com.example.tiendasuplementacion.model.Order
import com.example.tiendasuplementacion.model.OrderProductDetail
import com.example.tiendasuplementacion.repository.OrderProductRepository
import com.example.tiendasuplementacion.repository.OrderRepository
import com.example.tiendasuplementacion.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CheckoutResult(
    val success: Boolean,
    val orderId: Long? = null,
    val createdOrderProducts: List<OrderProductDetail> = emptyList(),
    val failedItems: List<Pair<CartItem, String>> = emptyList() // Item y mensaje de error
)

class CheckoutViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val orderProductRepository = OrderProductRepository()
    private val productRepository = ProductRepository()
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _checkoutResult = MutableStateFlow<CheckoutResult?>(null)
    val checkoutResult: StateFlow<CheckoutResult?> = _checkoutResult

    /**
     * Procesa el checkout completo:
     * 1. Crea una Order
     * 2. Crea OrderProducts para cada item del carrito usando el nuevo API
     * 3. Maneja errores de stock automáticamente
     */
    fun processCheckout(cartItems: List<CartItem>, userId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                if (cartItems.isEmpty()) {
                    _error.value = "El carrito está vacío"
                    return@launch
                }

                // Paso 1: Crear la Order
                val newOrder = Order(
                    order_id = 0, // Se asignará automáticamente
                    user_id = userId,
                    date_order = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                    total = cartItems.sumOf { it.product.price * it.quantity },
                    status_id = 1, // ID del estado "PENDING" o el que uses
                    total_products = cartItems.sumOf { it.quantity },
                    additional_info_payment_id = null
                )

                val createdOrder = orderRepository.create(newOrder)
                
                // Paso 2: Crear OrderProducts uno por uno
                val createdOrderProducts = mutableListOf<OrderProductDetail>()
                val failedItems = mutableListOf<Pair<CartItem, String>>()

                for (cartItem in cartItems) {
                    try {
                        val request = CreateOrderProductRequest(
                            order_id = createdOrder.order_id,
                            product_id = cartItem.product.id,
                            quantity = cartItem.quantity,
                            price = cartItem.product.price
                        )

                        val orderProduct = orderProductRepository.create(request)
                        createdOrderProducts.add(orderProduct)

                    } catch (e: Exception) {
                        val errorMessage = when {
                            e.message?.contains("Stock insuficiente", ignoreCase = true) == true -> 
                                "Stock insuficiente para ${cartItem.product.name}"
                            else -> "Error al procesar ${cartItem.product.name}: ${e.message}"
                        }
                        
                        failedItems.add(cartItem to errorMessage)
                    }
                }

                // Paso 3: Evaluar resultado
                val result = CheckoutResult(
                    success = failedItems.isEmpty(),
                    orderId = createdOrder.order_id,
                    createdOrderProducts = createdOrderProducts,
                    failedItems = failedItems
                )

                _checkoutResult.value = result

                if (failedItems.isNotEmpty()) {
                    val failedProductNames = failedItems.map { it.first.product.name }
                    _error.value = "Algunos productos no pudieron procesarse: ${failedProductNames.joinToString(", ")}"
                }

            } catch (e: Exception) {
                _error.value = "Error durante el checkout: ${e.message}"
                _checkoutResult.value = CheckoutResult(success = false)
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Procesa el checkout con manejo de rollback en caso de errores.
     * Si algunos items fallan, elimina los OrderProducts ya creados.
     */
    fun processCheckoutWithRollback(cartItems: List<CartItem>, userId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                if (cartItems.isEmpty()) {
                    _error.value = "El carrito está vacío"
                    return@launch
                }

                // Paso 1: Crear la Order
                val newOrder = Order(
                    order_id = 0,
                    user_id = userId,
                    date_order = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                    total = cartItems.sumOf { it.product.price * it.quantity },
                    status_id = 1,
                    total_products = cartItems.sumOf { it.quantity },
                    additional_info_payment_id = null
                )

                val createdOrder = orderRepository.create(newOrder)
                val createdOrderProducts = mutableListOf<OrderProductDetail>()
                
                // Paso 2: Intentar crear todos los OrderProducts
                var hasErrors = false
                val failedItems = mutableListOf<Pair<CartItem, String>>()

                for (cartItem in cartItems) {
                    try {
                        val request = CreateOrderProductRequest(
                            order_id = createdOrder.order_id,
                            product_id = cartItem.product.id,
                            quantity = cartItem.quantity,
                            price = cartItem.product.price
                        )

                        val orderProduct = orderProductRepository.create(request)
                        createdOrderProducts.add(orderProduct)

                    } catch (e: Exception) {
                        hasErrors = true
                        val errorMessage = when {
                            e.message?.contains("Stock insuficiente", ignoreCase = true) == true -> 
                                "Stock insuficiente para ${cartItem.product.name}"
                            else -> "Error al procesar ${cartItem.product.name}: ${e.message}"
                        }
                        
                        failedItems.add(cartItem to errorMessage)
                        break // Parar al primer error para hacer rollback
                    }
                }

                // Paso 3: Si hay errores, hacer rollback
                if (hasErrors) {
                    // Eliminar OrderProducts creados para restaurar stock
                    for (orderProduct in createdOrderProducts) {
                        try {
                            orderProductRepository.delete(orderProduct.id)
                        } catch (e: Exception) {
                            // Log del error, pero continuar con el rollback
                        }
                    }

                    // Podríamos también eliminar la Order aquí si es necesario
                    // orderRepository.delete(createdOrder.id)

                    _error.value = failedItems.firstOrNull()?.second ?: "Error durante el procesamiento"
                    _checkoutResult.value = CheckoutResult(success = false, failedItems = failedItems)
                } else {
                    // Todo salió bien
                    _checkoutResult.value = CheckoutResult(
                        success = true,
                        orderId = createdOrder.order_id,
                        createdOrderProducts = createdOrderProducts
                    )
                }

            } catch (e: Exception) {
                _error.value = "Error durante el checkout: ${e.message}"
                _checkoutResult.value = CheckoutResult(success = false)
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Actualiza la cantidad de un OrderProduct existente.
     */
    fun updateOrderProductQuantity(orderProductId: Long, newQuantity: Int, onSuccess: (OrderProductDetail) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val updateRequest = com.example.tiendasuplementacion.model.UpdateOrderProductRequest(
                    quantity = newQuantity
                )

                val updatedOrderProduct = orderProductRepository.update(orderProductId, updateRequest)
                onSuccess(updatedOrderProduct)

            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("Stock insuficiente", ignoreCase = true) == true -> 
                        "No hay suficiente stock para la cantidad solicitada"
                    else -> "Error al actualizar cantidad: ${e.message}"
                }
                _error.value = errorMessage
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Elimina un OrderProduct (restaura automáticamente el stock).
     */
    fun removeOrderProduct(orderProductId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val success = orderProductRepository.delete(orderProductId)
                if (success) {
                    onSuccess()
                } else {
                    _error.value = "Error al eliminar el producto del pedido"
                }

            } catch (e: Exception) {
                _error.value = "Error al eliminar producto: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearResult() {
        _checkoutResult.value = null
    }
}