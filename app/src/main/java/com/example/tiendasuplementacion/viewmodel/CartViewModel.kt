package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendasuplementacion.model.CartItem
import com.example.tiendasuplementacion.model.CreateOrderProductRequest
import com.example.tiendasuplementacion.model.OrderProductDetail
import com.example.tiendasuplementacion.model.Product
import com.example.tiendasuplementacion.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class CartViewModel : ViewModel() {
    private val productRepository = ProductRepository()
    
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // Mapeo de items del carrito a OrderProducts creados (para poder eliminarlos/actualizarlos)
    private val _orderProductIds = MutableStateFlow<Map<Long, Long>>(emptyMap()) // productId -> orderProductId
    val orderProductIds: StateFlow<Map<Long, Long>> = _orderProductIds

    /**
     * Añade un producto al carrito.
     * Solo valida el stock localmente, la validación real ocurre en el checkout.
     */
    fun addToCart(product: Product) {
        try {
            if (product.stock <= 0) {
                _error.value = "No hay stock disponible para este producto"
                viewModelScope.launch { _events.emit(UiEvent.ShowError(_error.value ?: "No hay stock disponible")) }
                return
            }

            _cartItems.update { currentCart ->
                val index = currentCart.indexOfFirst { it.product.id == product.id }
                if (index >= 0) {
                    val currentItem = currentCart[index]
                    if (currentItem.quantity >= product.stock) {
                        _error.value = "No hay suficiente stock disponible"
                        return@update currentCart
                    }
                    currentCart.toMutableList().apply {
                        this[index] = this[index].copy(quantity = this[index].quantity + 1)
                    }
                } else {
                    currentCart + CartItem(product)
                }
            }
            _error.value = null
            viewModelScope.launch { _events.emit(UiEvent.ShowSnackbar("Producto agregado al carrito")) }
        } catch (e: Exception) {
            _error.value = "Error al añadir producto al carrito: ${e.message}"
            viewModelScope.launch { _events.emit(UiEvent.ShowError(_error.value ?: "Error al añadir producto al carrito")) }
        }
    }

    /**
     * Remueve completamente un producto del carrito.
     */
    fun removeFromCart(productId: Long) {
        try {
            _cartItems.update { currentCart ->
                currentCart.filterNot { it.product.id == productId }
            }
            
            // Limpiar el mapeo de orderProductIds
            _orderProductIds.update { current ->
                current.filterNot { it.key == productId }
            }
            
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Error al remover producto del carrito: ${e.message}"
        }
    }

    /**
     * Actualiza la cantidad de un producto en el carrito.
     */
    fun updateQuantity(productId: Long, newQuantity: Int) {
        try {
            if (newQuantity < 0) {
                _error.value = "La cantidad no puede ser negativa"
                return
            }

            _cartItems.update { currentCart ->
                val item = currentCart.find { it.product.id == productId }
                if (item != null && newQuantity > item.product.stock) {
                    _error.value = "No hay suficiente stock disponible"
                    return@update currentCart
                }

                currentCart.map {
                    if (it.product.id == productId) it.copy(quantity = newQuantity) else it
                }.filter { it.quantity > 0 }
            }
            
            // Si la cantidad llegó a 0, limpiar el mapeo
            if (newQuantity == 0) {
                _orderProductIds.update { current ->
                    current.filterNot { it.key == productId }
                }
            }
            
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Error al actualizar cantidad: ${e.message}"
        }
    }

    /**
     * Limpia todo el carrito.
     */
    fun clearCart() {
        try {
            _cartItems.value = emptyList()
            _orderProductIds.value = emptyMap()
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Error al limpiar carrito: ${e.message}"
        }
    }

    /**
     * Calcula el precio total del carrito.
     */
    fun getTotalPrice(): Double {
        return try {
            _cartItems.value.sumOf { it.product.price * it.quantity }
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * Actualiza el stock de un producto específico consultando el servidor.
     * Útil después de operaciones que pueden haber cambiado el stock.
     */
    fun refreshProductStock(productId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val updatedProduct = productRepository.getById(productId)
                
                // Actualizar el producto en el carrito con el nuevo stock
                _cartItems.update { currentCart ->
                    currentCart.map { cartItem ->
                        if (cartItem.product.id == productId) {
                            cartItem.copy(product = updatedProduct)
                        } else {
                            cartItem
                        }
                    }
                }
                
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al actualizar stock: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Actualiza el stock de todos los productos en el carrito.
     */
    fun refreshAllProductsStock() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val productIds = _cartItems.value.map { it.product.id }
                
                val updatedProducts = mutableMapOf<Long, Product>()
                for (productId in productIds) {
                    try {
                        val product = productRepository.getById(productId)
                        updatedProducts[productId] = product
                    } catch (e: Exception) {
                        // Si un producto falla, continuar con los demás
                        continue
                    }
                }
                
                // Actualizar los productos en el carrito
                _cartItems.update { currentCart ->
                    currentCart.mapNotNull { cartItem ->
                        val updatedProduct = updatedProducts[cartItem.product.id]
                        if (updatedProduct != null) {
                            // Si el stock es menor que la cantidad en el carrito, ajustar
                            val adjustedQuantity = minOf(cartItem.quantity, updatedProduct.stock)
                            if (adjustedQuantity > 0) {
                                cartItem.copy(
                                    product = updatedProduct,
                                    quantity = adjustedQuantity
                                )
                            } else {
                                null // Remover items sin stock
                            }
                        } else {
                            cartItem // Mantener el item original si no se pudo actualizar
                        }
                    }
                }
                
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al actualizar stock de productos: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Establece el mapeo de un producto del carrito con su OrderProduct creado.
     */
    fun setOrderProductId(productId: Long, orderProductId: Long) {
        _orderProductIds.update { current ->
            current + (productId to orderProductId)
        }
    }

    /**
     * Obtiene el OrderProduct ID asociado a un producto del carrito.
     */
    fun getOrderProductId(productId: Long): Long? {
        return _orderProductIds.value[productId]
    }

    /**
     * Convierte un item del carrito en un CreateOrderProductRequest.
     */
    fun cartItemToOrderProductRequest(cartItem: CartItem, orderId: Long): CreateOrderProductRequest {
        return CreateOrderProductRequest(
            order_id = orderId,
            product_id = cartItem.product.id,
            quantity = cartItem.quantity,
            price = cartItem.product.price
        )
    }

    /**
     * Manejo de errores.
     */
    fun setError(message: String?) {
        _error.value = message
    }

    fun clearError() {
        _error.value = null
    }
}
