package com.example.tiendasuplementacion.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.tiendasuplementacion.model.CategoryProduct
import com.example.tiendasuplementacion.repository.CategoryProductRepository
import kotlinx.coroutines.launch

class CategoryProductViewModel : ViewModel() {
    private val repository = CategoryProductRepository()
    private val _relations = MutableStateFlow<List<CategoryProduct>>(emptyList())
    val relations: StateFlow<List<CategoryProduct>> = _relations

    fun fetchAll() {
        viewModelScope.launch {
            _relations.value = repository.getAll()
        }
    }

    fun create(data: CategoryProduct) {
        viewModelScope.launch {
            repository.create(data)
            fetchAll()
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            fetchAll()
        }
    }
}
