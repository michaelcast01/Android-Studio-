package com.example.tiendasuplementacion.util

import android.content.Context
import android.util.Log
import java.io.IOException
import java.util.Properties

/**
 * Utilidad para leer variables de entorno desde el archivo .env
 * ubicado en la carpeta assets de la aplicación
 */
object EnvConfig {
    private const val TAG = "EnvConfig"
    private const val ENV_FILE = ".env"
    private var properties: Properties? = null
    
    /**
     * Inicializa la configuración de variables de entorno
     * @param context Contexto de la aplicación
     */
    fun initialize(context: Context) {
        try {
            properties = Properties()
            val inputStream = context.assets.open(ENV_FILE)
            properties?.load(inputStream)
            inputStream.close()
            Log.d(TAG, "Archivo .env cargado exitosamente")
        } catch (e: IOException) {
            Log.e(TAG, "Error al cargar archivo .env: ${e.message}")
            // Si no se puede cargar desde assets, intentar valores por defecto
            setDefaultValues()
        }
    }
    
    /**
     * Establece valores por defecto en caso de que no se pueda cargar el archivo .env
     */
    private fun setDefaultValues() {
        properties = Properties().apply {
            setProperty("EMAIL_API_KEY", "3a71KlUgY833X5vBkHOsM6YjgPfbqZ6d4HNiBn7q")
        }
        Log.d(TAG, "Usando valores por defecto para variables de entorno")
    }
    
    /**
     * Obtiene el valor de una variable de entorno
     * @param key Clave de la variable
     * @param defaultValue Valor por defecto si no existe la clave
     * @return Valor de la variable o el valor por defecto
     */
    fun get(key: String, defaultValue: String = ""): String {
        return properties?.getProperty(key, defaultValue) ?: defaultValue
    }
    
    /**
     * Obtiene un valor booleano de una variable de entorno
     * @param key Clave de la variable
     * @param defaultValue Valor por defecto si no existe la clave
     * @return Valor booleano de la variable
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return try {
            get(key).toBoolean()
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    /**
     * Obtiene un valor entero de una variable de entorno
     * @param key Clave de la variable
     * @param defaultValue Valor por defecto si no existe la clave
     * @return Valor entero de la variable
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return try {
            get(key).toInt()
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    /**
     * Obtiene todas las variables de entorno como un mapa
     * @return Mapa con todas las variables cargadas
     */
    fun getAllProperties(): Map<String, String> {
        return properties?.map { (key, value) -> 
            key.toString() to value.toString() 
        }?.toMap() ?: emptyMap()
    }
    
    /**
     * Verifica si una clave existe en las variables de entorno
     * @param key Clave a verificar
     * @return true si existe, false en caso contrario
     */
    fun hasKey(key: String): Boolean {
        return properties?.containsKey(key) ?: false
    }
    
    /**
     * Registra todas las variables cargadas en el log (solo para depuración)
     */
    fun logAllVariables() {
        if (getBoolean("DEBUG_MODE", false)) {
            Log.d(TAG, "Variables de entorno cargadas:")
            getAllProperties().forEach { (key, value) ->
                // No mostrar valores sensibles como passwords o tokens
                val maskedValue = if (key.contains("PASSWORD") || 
                                    key.contains("TOKEN") || 
                                    key.contains("KEY")) {
                    "*".repeat(value.length.coerceAtMost(8))
                } else {
                    value
                }
                Log.d(TAG, "$key = $maskedValue")
            }
        }
    }
}