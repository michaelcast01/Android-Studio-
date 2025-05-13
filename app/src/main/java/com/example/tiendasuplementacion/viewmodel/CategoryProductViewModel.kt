package com.example.tiendasuplementacion.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiendasuplementacion.model.CategoryProduct
import com.example.tiendasuplementacion.repository.CategoryProductRepository
import kotlinx.coroutines.launch

class CategoryProductViewModel : ViewModel() {
    private val repository = CategoryProductRepository()
    val relations = MutableLiveData<List<CategoryProduct>>()

    fun fetchAll() {
        viewModelScope.launch {
            relations.value = repository.getAll()
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
