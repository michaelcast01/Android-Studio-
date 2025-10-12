package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Product
import com.example.tiendasuplementacion.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()
    // Using StateFlow for Compose compatibility
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    // New unified UI state
    private val _uiState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Product>>> = _uiState.asStateFlow()

    fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = UiState.Loading
            try {
                val list = repository.getAll()
                _products.value = list
                _error.value = null
                _uiState.value = UiState.Success(list)
            } catch (e: Exception) {
                val msg = "Error al cargar los productos: ${e.message}"
                _error.value = msg
                _events.emit(UiEvent.ShowError(msg))
                _uiState.value = UiState.Error(msg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshProducts() {
        // Limpiar cache antes de refrescar
        repository.clearCache()
        fetchProducts()
    }

    fun createProduct(product: Product): Product {
        var createdProduct: Product? = null
        viewModelScope.launch {
            try {
                validateProduct(product)
                createdProduct = repository.create(product)
                fetchProducts()
                _error.value = null
                _events.emit(UiEvent.ShowSnackbar("Producto creado"))
            } catch (e: Exception) {
                _error.value = "Error al crear el producto: ${e.message}"
                _events.emit(UiEvent.ShowError(_error.value ?: "Error al crear el producto"))
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
                _events.emit(UiEvent.ShowSnackbar("Producto eliminado"))
            } catch (e: Exception) {
                _error.value = "Error al eliminar el producto: ${e.message}"
                _events.emit(UiEvent.ShowError(_error.value ?: "Error al eliminar el producto"))
            }
        }
    }

    suspend fun getProductById(id: Long): Product {
        return try {
            repository.getById(id)
        } catch (e: Exception) {
            _error.value = "Error al obtener el producto: ${e.message}"
            _events.emit(UiEvent.ShowError(_error.value ?: "Error al obtener el producto"))
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
                _events.emit(UiEvent.ShowSnackbar("Producto actualizado"))
            } catch (e: Exception) {
                _error.value = "Error al actualizar el producto: ${e.message}"
                _events.emit(UiEvent.ShowError(_error.value ?: "Error al actualizar el producto"))
            }
        }
    }
}
