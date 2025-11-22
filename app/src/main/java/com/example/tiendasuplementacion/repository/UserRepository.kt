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
        } catch (e: retrofit2.HttpException) {
            android.util.Log.e("UserRepository", "Error HTTP en login: ${e.code()}", e)
            when (e.code()) {
                404 -> throw Exception("404:El correo electrónico no está registrado")
                423 -> throw Exception("423:Tu cuenta está deshabilitada. Contacta al administrador")
                401 -> throw Exception("401:Contraseña incorrecta")
                else -> throw Exception("Error al iniciar sesión: ${e.message()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error en login: ${e.message}", e)
            throw e
        }
    }

    suspend fun toggleEnabled(id: Long): User = service.toggleUserEnabled(id)
}
