package com.example.tiendasuplementacion.interfaces

import com.example.tiendasuplementacion.model.Product
import retrofit2.http.*

interface ProductApiService {
    @GET("/api/products")
    suspend fun getAll(@Query("include_disabled") includeDisabled: Boolean? = null): List<Product>

    @GET("/api/products/{id}")
    suspend fun getById(@Path("id") id: Long): Product

    @POST("/api/products")
    suspend fun create(@Body product: Product): Product

    @PUT("/api/products/{id}")
    suspend fun update(@Path("id") id: Long, @Body product: Product): Product

    @DELETE("/api/products/{id}")
    suspend fun delete(@Path("id") id: Long)

    @PATCH("/api/products/{id}/toggle-enabled")
    suspend fun toggleEnabled(@Path("id") id: Long): Product
}
