package com.example.tiendasuplementacion.interfaces

import com.example.tiendasuplementacion.model.CategoryProduct
import retrofit2.http.*

interface CategoryProductApiService {
    @GET("/api/categories-products")
    suspend fun getAll(): List<CategoryProduct>

    @GET("/api/categories-products/{id}")
    suspend fun getById(@Path("id") id: Long): CategoryProduct

    @POST("/api/categories-products")
    suspend fun create(@Body data: CategoryProduct): CategoryProduct

    @PUT("/api/categories-products/{id}")
    suspend fun update(@Path("id") id: Long, @Body data: CategoryProduct): CategoryProduct

    @DELETE("/api/categories-products/{id}")
    suspend fun delete(@Path("id") id: Long)
}
