package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.tiendasuplementacion.model.Category
import com.example.tiendasuplementacion.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {
    private val repository = CategoryRepository()
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    fun fetchCategories() {
        viewModelScope.launch {
            _categories.value = repository.getAll()
        }
    }

    fun createCategory(category: Category) {
        viewModelScope.launch {
            repository.create(category)
            fetchCategories()
        }
    }
}
