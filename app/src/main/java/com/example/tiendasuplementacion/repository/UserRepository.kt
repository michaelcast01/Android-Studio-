package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.User
import com.example.tiendasuplementacion.model.LoginResponse
import com.example.tiendasuplementacion.network.RetrofitClient

class UserRepository {

    private val service = RetrofitClient.userService

    suspend fun getAll(): List<User> = service.getUsers()

    suspend fun create(user: User): User = service.createUser(user)

    suspend fun update(id: Long, user: User): User = service.updateUser(id, user)

    suspend fun delete(id: Long) = service.deleteUser(id)

    suspend fun login(email: String, password: String): User {
        val credentials = mapOf(
            "email" to email,
            "password" to password
        )
        return service.login(credentials).user
    }
}
