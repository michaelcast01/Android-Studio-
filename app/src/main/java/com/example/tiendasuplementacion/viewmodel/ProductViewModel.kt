package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Product
import com.example.tiendasuplementacion.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchProducts() {
        viewModelScope.launch {
            try {
                _products.value = repository.getAll()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cargar los productos: ${e.message}"
            }
        }
    }

    fun createProduct(product: Product): Product {
        var createdProduct: Product? = null
        viewModelScope.launch {
            try {
                validateProduct(product)
                createdProduct = repository.create(product)
                fetchProducts()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al crear el producto: ${e.message}"
                throw e
            }
        }
        return createdProduct ?: throw IllegalStateException("Error al crear el producto")
    }

    private fun validateProduct(product: Product) {
        when {
            product.name.isBlank() -> throw IllegalArgumentException("El nombre no puede estar vacío")
            product.description.isBlank() -> throw IllegalArgumentException("La descripción no puede estar vacía")
            product.price <= 0 -> throw IllegalArgumentException("El precio debe ser mayor a 0")
            product.stock < 0 -> throw IllegalArgumentException("El stock no puede ser negativo")
            product.url_image.isBlank() -> throw IllegalArgumentException("La URL de la imagen no puede estar vacía")
        }
    }

    suspend fun createProductSuspend(product: Product): Product {
        validateProduct(product)
        return repository.create(product)
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            try {
                repository.delete(id)
                fetchProducts()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al eliminar el producto: ${e.message}"
            }
        }
    }

    suspend fun getProductById(id: Long): Product {
        return try {
            repository.getById(id)
        } catch (e: Exception) {
            _error.value = "Error al obtener el producto: ${e.message}"
            throw e
        }
    }

    fun updateProduct(id: Long, product: Product) {
        viewModelScope.launch {
            try {
                validateProduct(product)
                repository.update(id, product)
                fetchProducts()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al actualizar el producto: ${e.message}"
            }
        }
    }
}
