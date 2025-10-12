package com.example.tiendasuplementacion.util

import androidx.compose.runtime.Stable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Configuraciones centralizadas de optimización de performance
 */
@Stable
object AppConfig {
    
    // Configuraciones de Cache
    object Cache {
        val DEFAULT_MEMORY_CACHE_VALIDITY: Duration = 5.minutes
        val IMAGE_CACHE_SIZE_MB = 100L
        val HTTP_CACHE_SIZE_MB = 20L
        val MEMORY_CACHE_PERCENTAGE = 0.30
    }
    
    // Configuraciones de Red
    object Network {
        val CONNECTION_TIMEOUT: Duration = 15.seconds
        val READ_TIMEOUT: Duration = 15.seconds
        val WRITE_TIMEOUT: Duration = 15.seconds
        const val MAX_RETRIES = 3
        const val RETRY_DELAY_MS = 1000L
    }
    
    // Configuraciones de UI
    object UI {
        const val SHIMMER_DURATION_MS = 800
        const val DEBOUNCE_DELAY_MS = 300L
        const val ANIMATION_DURATION_MS = 300
        const val LIST_PREFETCH_DISTANCE = 3
        const val PAGINATION_PAGE_SIZE = 20
    }
    
    // Configuraciones de Performance
    object Performance {
        const val ENABLE_R8_FULL_MODE = true
        const val ENABLE_AGGRESSIVE_OPTIMIZATIONS = true
        const val USE_HARDWARE_ACCELERATION = true
        const val ENABLE_MEMORY_TRIM = true
    }
    
    // Configuraciones de Debugging
    object Debug {
        const val ENABLE_PERFORMANCE_MONITORING = true
        const val LOG_NETWORK_CALLS = true
        const val LOG_CACHE_OPERATIONS = false
    }
}

/**
 * Configuraciones específicas por build type
 */
object BuildConfig {
    
    fun getImageQuality(): ImageQuality {
        return when {
            com.example.tiendasuplementacion.BuildConfig.DEBUG -> ImageQuality.HIGH
            else -> ImageQuality.OPTIMIZED
        }
    }
    
    fun shouldUseCompression(): Boolean {
        return !com.example.tiendasuplementacion.BuildConfig.DEBUG
    }
    
    fun getLogLevel(): LogLevel {
        return when {
            com.example.tiendasuplementacion.BuildConfig.DEBUG -> LogLevel.VERBOSE
            else -> LogLevel.ERROR
        }
    }
}

enum class ImageQuality {
    LOW, MEDIUM, HIGH, OPTIMIZED
}

enum class LogLevel {
    VERBOSE, DEBUG, INFO, WARN, ERROR, NONE
}

/**
 * Configuraciones de memoria adaptativas basadas en el dispositivo
 */
object MemoryConfig {
    
    fun getOptimalCacheSize(availableMemoryMB: Long): Long {
        return when {
            availableMemoryMB < 512 -> 25L // 25MB para dispositivos con poca memoria
            availableMemoryMB < 1024 -> 50L // 50MB para dispositivos medios
            availableMemoryMB < 2048 -> 100L // 100MB para dispositivos buenos
            else -> 150L // 150MB para dispositivos de alta gama
        }
    }
    
    fun getOptimalImageCachePercentage(availableMemoryMB: Long): Double {
        return when {
            availableMemoryMB < 512 -> 0.15 // 15% para dispositivos con poca memoria
            availableMemoryMB < 1024 -> 0.25 // 25% para dispositivos medios
            availableMemoryMB < 2048 -> 0.30 // 30% para dispositivos buenos
            else -> 0.35 // 35% para dispositivos de alta gama
        }
    }
    
    fun shouldEnableHardwareAcceleration(availableMemoryMB: Long): Boolean {
        return availableMemoryMB >= 512 // Solo en dispositivos con al menos 512MB
    }
}

/**
 * Configuraciones de optimización para diferentes tipos de conexión
 */
object NetworkOptimizationConfig {
    
    fun getImageQualityForConnection(connectionType: ConnectionType): ImageQuality {
        return when (connectionType) {
            ConnectionType.WIFI, ConnectionType.ETHERNET -> ImageQuality.HIGH
            ConnectionType.MOBILE -> ImageQuality.OPTIMIZED
            ConnectionType.OTHER -> ImageQuality.MEDIUM
            ConnectionType.NONE -> ImageQuality.LOW
        }
    }
    
    fun getCacheStrategyForConnection(connectionType: ConnectionType): CacheStrategy {
        return when (connectionType) {
            ConnectionType.WIFI, ConnectionType.ETHERNET -> CacheStrategy.NETWORK_FIRST
            ConnectionType.MOBILE -> CacheStrategy.CACHE_FIRST
            ConnectionType.OTHER -> CacheStrategy.CACHE_FIRST
            ConnectionType.NONE -> CacheStrategy.CACHE_ONLY
        }
    }
    
    fun getRetryCountForConnection(connectionType: ConnectionType): Int {
        return when (connectionType) {
            ConnectionType.WIFI, ConnectionType.ETHERNET -> 3
            ConnectionType.MOBILE -> 2
            ConnectionType.OTHER -> 2
            ConnectionType.NONE -> 0
        }
    }
}

enum class CacheStrategy {
    NETWORK_FIRST, CACHE_FIRST, CACHE_ONLY, NETWORK_ONLY
}