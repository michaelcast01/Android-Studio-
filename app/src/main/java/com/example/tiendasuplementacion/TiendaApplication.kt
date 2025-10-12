package com.example.tiendasuplementacion

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.example.tiendasuplementacion.network.RetrofitClient
import com.example.tiendasuplementacion.util.EnvConfig
import com.example.tiendasuplementacion.util.MemoryManager

class TiendaApplication : Application(), ImageLoaderFactory {
    
    override fun onCreate() {
        super.onCreate()
        EnvConfig.initialize(this)
        EnvConfig.logAllVariables()
        
        RetrofitClient.init(this)
        
        // Inicializar optimizaciones de memoria
        MemoryManager.logMemoryStats(this)
    }
    
    override fun newImageLoader(): ImageLoader {
        val optimizedConfig = MemoryManager.getOptimizedConfigurations(this)
        
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(optimizedConfig.imageCachePercentage)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(optimizedConfig.imageCacheSize * 1024 * 1024) // Convertir MB a bytes
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .crossfade(true) // Animación suave entre imágenes
            .allowHardware(optimizedConfig.enableHardwareAcceleration) // Hardware acceleration optimizada
            .build()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Delegar manejo de memoria al MemoryManager
        MemoryManager.handleTrimMemory(level)
    }
}