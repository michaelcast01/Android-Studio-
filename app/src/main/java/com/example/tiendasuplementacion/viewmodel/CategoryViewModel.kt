package com.example.tiendasuplementacion.viewmodel

import androidx.lifecycle.*
import com.example.tiendasuplementacion.model.Category
import com.example.tiendasuplementacion.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel : ViewModel() {
    private val repository = CategoryRepository()
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

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
