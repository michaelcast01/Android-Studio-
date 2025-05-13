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
        _cartItems.update { currentCart ->
            val index = currentCart.indexOfFirst { it.product.id == product.id }
            if (index >= 0) {
                currentCart.toMutableList().apply {
                    this[index] = this[index].copy(quantity = this[index].quantity + 1)
                }
            } else {
                currentCart + CartItem(product)
            }
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.update { currentCart ->
            currentCart.filterNot { it.product.id == productId }
        }
    }

    fun updateQuantity(productId: Long, newQuantity: Int) {
        _cartItems.update { currentCart ->
            currentCart.map {
                if (it.product.id == productId) it.copy(quantity = newQuantity) else it
            }.filter { it.quantity > 0 }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getTotalPrice(): Double {
        return _cartItems.value.sumOf { it.product.price * it.quantity }
    }
}
