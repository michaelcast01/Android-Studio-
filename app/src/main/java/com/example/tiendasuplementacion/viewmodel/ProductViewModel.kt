package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Product
import com.example.tiendasuplementacion.repository.ProductRepository
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    fun fetchProducts() {
        viewModelScope.launch {
            _products.value = repository.getAll()
        }
    }

    fun createProduct(product: Product) {
        viewModelScope.launch {
            repository.create(product)
            fetchProducts()
        }
    }
}
