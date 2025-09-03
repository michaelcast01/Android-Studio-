package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.Status
import com.example.tiendasuplementacion.network.RetrofitClient

class StatusRepository {
    private val service = RetrofitClient.statusService

    suspend fun getAll() = service.getAll()
    suspend fun getById(id: Long) = service.getById(id)
    suspend fun create(status: Status) = service.create(status)
    suspend fun update(id: Long, status: Status) = service.update(id, status)
    suspend fun delete(id: Long) = service.delete(id)
}
