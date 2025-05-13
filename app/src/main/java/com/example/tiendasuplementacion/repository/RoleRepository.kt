package com.example.tiendasuplementacion.repository


import com.example.tiendasuplementacion.model.Role
import com.example.tiendasuplementacion.network.RetrofitClient

class RoleRepository {
    private val service = RetrofitClient.roleService

    suspend fun getAll() = service.getAll()
    suspend fun getById(id: Long) = service.getById(id)
    suspend fun create(role: Role) = service.create(role)
    suspend fun update(id: Long, role: Role) = service.update(id, role)
    suspend fun delete(id: Long) = service.delete(id)
}
