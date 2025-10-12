package com.example.tiendasuplementacion.util

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import java.lang.Runtime

/**
 * Gestor de memoria para monitorear y optimizar el uso de memoria de la aplicación
 */
object MemoryManager {
    private const val TAG = "MemoryManager"
    
    /**
     * Obtiene información detallada sobre el uso de memoria
     */
    fun getMemoryInfo(context: Context): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val availableMemory = maxMemory - usedMemory
        
        return MemoryInfo(
            totalMemoryMB = memInfo.totalMem / (1024 * 1024),
            availableMemoryMB = memInfo.availMem / (1024 * 1024),
            usedMemoryMB = usedMemory / (1024 * 1024),
            maxHeapMB = maxMemory / (1024 * 1024),
            isLowMemory = memInfo.lowMemory,
            threshold = memInfo.threshold / (1024 * 1024)
        )
    }
    
    /**
     * Verifica si la memoria está bajo presión
     */
    fun isMemoryUnderPressure(context: Context): Boolean {
        val memInfo = getMemoryInfo(context)
        val memoryUsagePercentage = (memInfo.usedMemoryMB.toFloat() / memInfo.maxHeapMB.toFloat()) * 100
        
        return memInfo.isLowMemory || memoryUsagePercentage > 80f
    }
    
    /**
     * Ejecuta limpieza de memoria cuando es necesario
     */
    fun performMemoryCleanup(context: Context) {
        if (isMemoryUnderPressure(context)) {
            Log.d(TAG, "Performing memory cleanup due to memory pressure")
            
            // Sugerir al sistema que ejecute GC
            System.gc()
            
            // Notificar a componentes que pueden liberar memoria
            // (esto podría expandirse para notificar a ViewModels, caches, etc.)
            Runtime.getRuntime().runFinalization()
        }
    }
    
    /**
     * Obtiene configuraciones optimizadas basadas en la memoria disponible
     */
    fun getOptimizedConfigurations(context: Context): OptimizedConfig {
        val memInfo = getMemoryInfo(context)
        
        return OptimizedConfig(
            imageCacheSize = MemoryConfig.getOptimalCacheSize(memInfo.totalMemoryMB),
            imageCachePercentage = MemoryConfig.getOptimalImageCachePercentage(memInfo.totalMemoryMB),
            enableHardwareAcceleration = MemoryConfig.shouldEnableHardwareAcceleration(memInfo.totalMemoryMB),
            maxConcurrentImageLoads = when {
                memInfo.totalMemoryMB < 512 -> 2
                memInfo.totalMemoryMB < 1024 -> 4
                memInfo.totalMemoryMB < 2048 -> 6
                else -> 8
            },
            enableMemoryTrimming = memInfo.totalMemoryMB < 1024
        )
    }
    
    /**
     * Log de información de memoria para debugging
     */
    fun logMemoryStats(context: Context) {
        if (AppConfig.Debug.ENABLE_PERFORMANCE_MONITORING) {
            val memInfo = getMemoryInfo(context)
            Log.d(TAG, """
                Memory Stats:
                - Total System Memory: ${memInfo.totalMemoryMB} MB
                - Available System Memory: ${memInfo.availableMemoryMB} MB
                - Used Heap Memory: ${memInfo.usedMemoryMB} MB
                - Max Heap Memory: ${memInfo.maxHeapMB} MB
                - Is Low Memory: ${memInfo.isLowMemory}
                - Low Memory Threshold: ${memInfo.threshold} MB
                - Memory Usage: ${(memInfo.usedMemoryMB.toFloat() / memInfo.maxHeapMB.toFloat() * 100).toInt()}%
            """.trimIndent())
        }
    }
    
    /**
     * Configuración para trim memory basada en el nivel
     */
    fun handleTrimMemory(level: Int) {
        when (level) {
            TRIM_MEMORY_UI_HIDDEN -> {
                Log.d(TAG, "UI hidden - light memory cleanup")
                // Limpieza ligera
            }
            TRIM_MEMORY_BACKGROUND -> {
                Log.d(TAG, "App in background - moderate memory cleanup")
                // Limpieza moderada
                System.gc()
            }
            TRIM_MEMORY_MODERATE -> {
                Log.d(TAG, "Memory pressure moderate - aggressive cleanup")
                // Limpieza agresiva
                System.gc()
                Runtime.getRuntime().runFinalization()
            }
            TRIM_MEMORY_COMPLETE -> {
                Log.d(TAG, "Memory pressure critical - maximum cleanup")
                // Limpieza máxima
                System.gc()
                Runtime.getRuntime().runFinalization()
                // Aquí se podría notificar a caches para que se limpien
            }
        }
    }
    
    // Constants for simulated trim memory levels (kept inside the object - no companion needed)
    const val TRIM_MEMORY_UI_HIDDEN = 20
    const val TRIM_MEMORY_BACKGROUND = 40
    const val TRIM_MEMORY_MODERATE = 60
    const val TRIM_MEMORY_COMPLETE = 80
}

data class MemoryInfo(
    val totalMemoryMB: Long,
    val availableMemoryMB: Long,
    val usedMemoryMB: Long,
    val maxHeapMB: Long,
    val isLowMemory: Boolean,
    val threshold: Long
)

data class OptimizedConfig(
    val imageCacheSize: Long,
    val imageCachePercentage: Double,
    val enableHardwareAcceleration: Boolean,
    val maxConcurrentImageLoads: Int,
    val enableMemoryTrimming: Boolean
)