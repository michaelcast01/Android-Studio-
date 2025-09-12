package com.example.tiendasuplementacion.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cart_preferences")

class CartDataStore(private val context: Context) {
    private val gson = Gson()
    
    companion object {
        private val CART_ITEMS_KEY = stringPreferencesKey("cart_items")
    }
    
    suspend fun saveCartItems(cartItems: List<CartItem>) {
        val json = gson.toJson(cartItems)
        context.dataStore.edit { preferences ->
            preferences[CART_ITEMS_KEY] = json
        }
    }
    
    fun getCartItems(): Flow<List<CartItem>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[CART_ITEMS_KEY] ?: ""
            if (json.isBlank()) {
                emptyList()
            } else {
                try {
                    val type = object : TypeToken<List<CartItem>>() {}.type
                    gson.fromJson(json, type) ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }
    
    suspend fun clearCart() {
        context.dataStore.edit { preferences ->
            preferences.remove(CART_ITEMS_KEY)
        }
    }
}

data class CartItem(
    val productId: Int,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)