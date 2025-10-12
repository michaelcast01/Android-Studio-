package com.example.tiendasuplementacion.repository

import com.example.tiendasuplementacion.model.Product
import com.example.tiendasuplementacion.network.RetrofitClient
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository {
    private val service = RetrofitClient.productService
    
    // Cache simple en memoria (puede ser reemplazado por Room database más tarde)
    private var cachedProducts: List<Product>? = null
    private var lastCacheTime: Long = 0
    private val cacheValidityMs = 5 * 60 * 1000 // 5 minutos
    
    suspend fun getAll(): List<Product> = withContext(Dispatchers.IO) {
        try {
            // Verificar si el cache es válido
            val currentTime = System.currentTimeMillis()
            if (cachedProducts != null && (currentTime - lastCacheTime) < cacheValidityMs) {
                return@withContext cachedProducts!!
            }
            
            val result = service.getAll()
            // Actualizar cache
            cachedProducts = result
            lastCacheTime = currentTime
            result
        } catch (e: HttpException) {
            // Si hay error de red y tenemos cache, usar cache
            cachedProducts?.let { return@withContext it }
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            // Si hay error de conexión y tenemos cache, usar cache
            cachedProducts?.let { return@withContext it }
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }
    
    suspend fun getById(id: Long): Product = withContext(Dispatchers.IO) {
        try {
            // Primero verificar en cache
            cachedProducts?.find { it.id == id }?.let { return@withContext it }
            
            service.getById(id)
        } catch (e: HttpException) {
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }
    
    suspend fun create(product: Product): Product = withContext(Dispatchers.IO) {
        try {
            val result = service.create(product)
            // Invalidar cache después de crear
            invalidateCache()
            result
        } catch (e: HttpException) {
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }
    
    suspend fun update(id: Long, product: Product): Product = withContext(Dispatchers.IO) {
        try {
            val result = service.update(id, product)
            // Invalidar cache después de actualizar
            invalidateCache()
            result
        } catch (e: HttpException) {
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }
    
    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        try {
            service.delete(id)
            // Invalidar cache después de eliminar
            invalidateCache()
        } catch (e: HttpException) {
            throw Exception("Error del servidor: ${e.message()}")
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }
    
    private fun invalidateCache() {
        cachedProducts = null
        lastCacheTime = 0
    }
    
    fun clearCache() {
        invalidateCache()
    }
}
