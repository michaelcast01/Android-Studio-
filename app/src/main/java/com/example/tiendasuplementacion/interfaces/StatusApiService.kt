package com.example.tiendasuplementacion.interfaces

import com.example.tiendasuplementacion.model.Status
import retrofit2.http.*

interface StatusApiService {
    @GET("/api/statuses")
    suspend fun getAll(): List<Status>

    @GET("/api/statuses/{id}")
    suspend fun getById(@Path("id") id: Long): Status

    @POST("/api/statuses")
    suspend fun create(@Body status: Status): Status

    @PUT("/api/statuses/{id}")
    suspend fun update(@Path("id") id: Long, @Body status: Status): Status

    @DELETE("/api/statuses/{id}")
    suspend fun delete(@Path("id") id: Long)
}
