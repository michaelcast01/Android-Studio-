package com.example.tiendasuplementacion.util

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Utilidades para optimización de performance en Jetpack Compose
 */

/**
 * Debounce function para búsquedas y operaciones costosas
 */
@Composable
fun <T> debounce(
    value: T,
    delay: Duration = 300.milliseconds,
    onValueChanged: (T) -> Unit
) {
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(value) {
        job?.cancel()
        job = scope.launch {
            delay(delay)
            onValueChanged(value)
        }
    }
}

/**
 * Cache simple con expiración para ViewModels
 */
class SimpleCache<T>(private val validityMs: Long = 300_000) { // 5 minutos por defecto
    private var cachedData: T? = null
    private var lastCacheTime: Long = 0

    fun get(): T? {
        val currentTime = System.currentTimeMillis()
        return if (cachedData != null && (currentTime - lastCacheTime) < validityMs) {
            cachedData
        } else {
            null
        }
    }

    fun put(data: T) {
        cachedData = data
        lastCacheTime = System.currentTimeMillis()
    }

    fun invalidate() {
        cachedData = null
        lastCacheTime = 0
    }
}

/**
 * Optimizaciones para LazyColumn/LazyRow
 */
object LazyListOptimizations {
    // Configuración optimizada para listas grandes
    val contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
    val verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    
    // Tamaños de prefetch optimizados
    const val PREFETCH_DISTANCE = 3
    const val BEYOND_BOUNDS_LAYOUT_COUNT = 0
}

/**
 * Estado para manejar paginación simple
 */
@Stable
class PaginationState(
    initialPage: Int = 0,
    val pageSize: Int = 20
) {
    var currentPage by mutableStateOf(initialPage)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var hasMorePages by mutableStateOf(true)
        private set
    
    fun loadNextPage() {
        if (!isLoading && hasMorePages) {
            currentPage++
        }
    }
    
    // Use update* methods to avoid JVM signature clash with the Kotlin property setter
    fun updateLoading(loading: Boolean) {
        isLoading = loading
    }

    fun updateHasMorePages(hasMore: Boolean) {
        hasMorePages = hasMore
    }
    
    fun reset() {
        currentPage = 0
        isLoading = false
        hasMorePages = true
    }
}

/**
 * Estado para manejar pull-to-refresh
 */
@Stable
class RefreshState {
    var isRefreshing by mutableStateOf(false)
        private set
    
    // Use updateRefreshing to avoid colliding with property setter for 'isRefreshing'
    fun updateRefreshing(refreshing: Boolean) {
        isRefreshing = refreshing
    }
}

/**
 * Optimizaciones de memoria para imágenes
 */
object ImageOptimizations {
    // Configuraciones de memoria para Coil
    const val MEMORY_CACHE_PERCENTAGE = 0.25
    const val DISK_CACHE_SIZE = 100L * 1024L * 1024L // 100MB
    
    // Configuraciones de placeholder para diferentes tamaños
    val SMALL_PLACEHOLDER_SIZE = 50.dp to 50.dp
    val MEDIUM_PLACEHOLDER_SIZE = 100.dp to 100.dp
    val LARGE_PLACEHOLDER_SIZE = 200.dp to 200.dp
}