package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tiendasuplementacion.model.CartItem
import com.example.tiendasuplementacion.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    fun addToCart(product: Product) {
        try {
            if (product.stock <= 0) {
                throw IllegalStateException("No hay stock disponible para este producto")
            }

            _cartItems.update { currentCart ->
                val index = currentCart.indexOfFirst { it.product.id == product.id }
                if (index >= 0) {
                    val currentItem = currentCart[index]
                    if (currentItem.quantity >= product.stock) {
                        throw IllegalStateException("No hay suficiente stock disponible")
                    }
                    currentCart.toMutableList().apply {
                        this[index] = this[index].copy(quantity = this[index].quantity + 1)
                    }
                } else {
                    currentCart + CartItem(product)
                }
            }
        } catch (e: Exception) {
            // Aquí podrías manejar el error de alguna manera, por ejemplo:
            // - Mostrar un mensaje al usuario
            // - Registrar el error
            // - Lanzar el error para que la UI lo maneje
            throw e
        }
    }

    fun removeFromCart(productId: Long) {
        try {
            _cartItems.update { currentCart ->
                currentCart.filterNot { it.product.id == productId }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun updateQuantity(productId: Long, newQuantity: Int) {
        try {
            if (newQuantity < 0) {
                throw IllegalArgumentException("La cantidad no puede ser negativa")
            }

            _cartItems.update { currentCart ->
                val item = currentCart.find { it.product.id == productId }
                if (item != null && newQuantity > item.product.stock) {
                    throw IllegalStateException("No hay suficiente stock disponible")
                }

                currentCart.map {
                    if (it.product.id == productId) it.copy(quantity = newQuantity) else it
                }.filter { it.quantity > 0 }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun clearCart() {
        try {
            _cartItems.value = emptyList()
        } catch (e: Exception) {
            throw e
        }
    }

    fun getTotalPrice(): Double {
        return try {
            _cartItems.value.sumOf { it.product.price * it.quantity }
        } catch (e: Exception) {
            0.0
        }
    }
}
