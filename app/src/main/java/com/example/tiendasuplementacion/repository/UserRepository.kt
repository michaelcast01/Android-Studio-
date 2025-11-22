package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.User
import com.example.tiendasuplementacion.network.RetrofitClient

class UserRepository {

    private val service = RetrofitClient.userService

    suspend fun getAll(): List<User> = service.getUsers()

    suspend fun create(user: User): User = service.createUser(user)

    suspend fun update(id: Long, user: User): User = service.updateUser(id, user)

    suspend fun delete(id: Long) = service.deleteUser(id)

    suspend fun login(email: String, password: String): User {
        return try {
            val credentials = mapOf(
                "email" to email,
                "password" to password
            )
            service.login(credentials).user
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error en login: ${e.message}", e)
            throw Exception("Error al iniciar sesión: ${e.localizedMessage ?: e.message}")
        }
    }

    suspend fun toggleEnabled(id: Long): User = service.toggleUserEnabled(id)
}
