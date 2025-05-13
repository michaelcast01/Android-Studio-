package com.example.tiendasuplementacion.interfaces


import com.example.tiendasuplementacion.model.Category
import retrofit2.http.*

interface CategoryApiService {
    @GET("/api/categories")
    suspend fun getAllCategories(): List<Category>

    @GET("/api/categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Long): Category

    @POST("/api/categories")
    suspend fun createCategory(@Body category: Category): Category

    @PUT("/api/categories/{id}")
    suspend fun updateCategory(@Path("id") id: Long, @Body category: Category): Category

    @DELETE("/api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long)
}
