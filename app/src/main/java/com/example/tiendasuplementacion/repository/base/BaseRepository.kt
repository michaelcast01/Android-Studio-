package com.example.tiendasuplementacion.repository.base

import com.example.tiendasuplementacion.util.SimpleCache
import com.example.tiendasuplementacion.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio base con funcionalidades de cache y manejo de errores optimizado
 * Puede ser usado como template para otros repositorios
 */
abstract class BaseRepository<T> {
    
    protected val cache = SimpleCache<List<T>>(validityMs = 300_000) // 5 minutos
    
    /**
     * Método abstracto que implementa cada repositorio para obtener datos del servicio
     */
    protected abstract suspend fun fetchFromService(): List<T>
    
    /**
     * Método abstracto para obtener un elemento por ID
     */
    protected abstract suspend fun fetchByIdFromService(id: Long): T
    
    /**
     * Obtiene todos los elementos con cache inteligente
     */
    suspend fun getAll(forceRefresh: Boolean = false): List<T> = withContext(Dispatchers.IO) {
        try {
            // Si no es refresh forzado, intentar obtener del cache
            if (!forceRefresh) {
                cache.get()?.let { cachedData ->
                    return@withContext cachedData
                }
            }
            
            // Obtener datos del servicio
            val result = fetchFromService()
            
            // Guardar en cache
            cache.put(result)
            
            result
        } catch (e: HttpException) {
            // Si hay error de red y tenemos cache, usar cache
            cache.get()?.let { return@withContext it }
            throw mapHttpException(e)
        } catch (e: IOException) {
            // Si hay error de conexión y tenemos cache, usar cache
            cache.get()?.let { return@withContext it }
            throw Exception("Error de conexión: Verifique su conexión a internet")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }
    
    /**
     * Obtiene un elemento por ID con cache
     */
    suspend fun getById(id: Long): T = withContext(Dispatchers.IO) {
        try {
            // Primero verificar en cache
            cache.get()?.find { getIdFromItem(it) == id }?.let { return@withContext it }
            
            // Si no está en cache, obtener del servicio
            fetchByIdFromService(id)
        } catch (e: HttpException) {
            throw mapHttpException(e)
        } catch (e: IOException) {
            throw Exception("Error de conexión: Verifique su conexión a internet")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }
    
    /**
     * Invalida el cache
     */
    fun invalidateCache() {
        cache.invalidate()
    }
    
    /**
     * Método abstracto para obtener el ID de un elemento
     * Debe ser implementado por cada repositorio específico
     */
    protected abstract fun getIdFromItem(item: T): Long
    
    /**
     * Mapea errores HTTP a mensajes más descriptivos
     */
    private fun mapHttpException(e: HttpException): Exception {
        val message = when (e.code()) {
            400 -> "Solicitud inválida"
            401 -> "No autorizado"
            403 -> "Acceso denegado"
            404 -> "Recurso no encontrado"
            408 -> "Tiempo de espera agotado"
            500 -> "Error interno del servidor"
            502 -> "Servidor no disponible"
            503 -> "Servicio temporalmente no disponible"
            in 400..499 -> "Error del cliente: ${e.message()}"
            in 500..599 -> "Error del servidor: ${e.message()}"
            else -> "Error de red: ${e.message()}"
        }
        return Exception(message)
    }
}

/**
 * Ejemplo de implementación específica para productos
 * Muestra cómo extender BaseRepository
 */
/*
class OptimizedProductRepository : BaseRepository<Product>() {
    private val service = RetrofitClient.productService
    
    override suspend fun fetchFromService(): List<Product> {
        return service.getAll()
    }
    
    override suspend fun fetchByIdFromService(id: Long): Product {
        return service.getById(id)
    }
    
    override fun getIdFromItem(item: Product): Long {
        return item.id
    }
    
    // Métodos específicos adicionales
    suspend fun create(product: Product): Product = withContext(Dispatchers.IO) {
        try {
            val result = service.create(product)
            invalidateCache() // Invalidar cache después de crear
            result
        } catch (e: HttpException) {
            throw mapHttpException(e)
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }
    
    suspend fun update(id: Long, product: Product): Product = withContext(Dispatchers.IO) {
        try {
            val result = service.update(id, product)
            invalidateCache() // Invalidar cache después de actualizar
            result
        } catch (e: HttpException) {
            throw mapHttpException(e)
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }
    
    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        try {
            service.delete(id)
            invalidateCache() // Invalidar cache después de eliminar
        } catch (e: HttpException) {
            throw mapHttpException(e)
        } catch (e: IOException) {
            throw Exception("Error de conexión: ${e.message}")
        } catch (e: Exception) {
            throw Exception("Error inesperado: ${e.message}")
        }
    }
}
*/