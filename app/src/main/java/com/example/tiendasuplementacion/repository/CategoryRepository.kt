package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.Category
import com.example.tiendasuplementacion.network.RetrofitClient

class CategoryRepository {
    private val service = RetrofitClient.categoryService

    suspend fun getAll() = service.getAllCategories()
    suspend fun getById(id: Long) = service.getCategoryById(id)
    suspend fun create(category: Category) = service.createCategory(category)
    suspend fun update(id: Long, category: Category) = service.updateCategory(id, category)
    suspend fun delete(id: Long) = service.deleteCategory(id)
}
