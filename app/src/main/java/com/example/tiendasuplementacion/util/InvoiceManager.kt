package com.example.tiendasuplementacion.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

/**
 * Gestor centralizado de facturas PDF
 * 
 * Proporciona funcionalidades para:
 * - Abrir facturas en visores externos
 * - Compartir facturas por diferentes medios
 * - Listar facturas generadas
 * - Eliminar facturas específicas
 */
object InvoiceManager {
    private const val TAG = "InvoiceManager"
    private const val INVOICE_DIRECTORY = "facturas"
    
    /**
     * Abre una factura PDF en un visor externo
     * 
     * @param context Contexto de Android
     * @param pdfPath Ruta absoluta del archivo PDF
     * @return true si se abrió correctamente, false en caso contrario
     */
    fun openInvoicePdf(context: Context, pdfPath: String): Boolean {
        return try {
            val file = File(pdfPath)
            if (!file.exists()) {
                Log.e(TAG, "El archivo PDF no existe: $pdfPath")
                return false
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            context.startActivity(intent)
            Log.d(TAG, "Factura abierta: ${file.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al abrir factura", e)
            false
        }
    }
    
    /**
     * Comparte una factura PDF usando el selector de compartir del sistema
     * 
     * @param context Contexto de Android
     * @param pdfPath Ruta absoluta del archivo PDF
     * @param orderId ID de la orden (para el texto del mensaje)
     * @return true si se inició el proceso de compartir, false en caso contrario
     */
    fun shareInvoicePdf(context: Context, pdfPath: String, orderId: Long): Boolean {
        return try {
            val file = File(pdfPath)
            if (!file.exists()) {
                Log.e(TAG, "El archivo PDF no existe: $pdfPath")
                return false
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Factura #$orderId - Tienda Suplementación")
                putExtra(Intent.EXTRA_TEXT, "Adjunto la factura de su compra #$orderId")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            context.startActivity(Intent.createChooser(intent, "Compartir factura"))
            Log.d(TAG, "Factura compartida: ${file.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al compartir factura", e)
            false
        }
    }
    
    /**
     * Lista todas las facturas generadas
     * 
     * @param context Contexto de Android
     * @return Lista de archivos PDF ordenados por fecha (más recientes primero)
     */
    fun listInvoices(context: Context): List<InvoiceInfo> {
        return try {
            val directory = File(context.getExternalFilesDir(null), INVOICE_DIRECTORY)
            if (!directory.exists()) {
                return emptyList()
            }
            
            directory.listFiles()
                ?.filter { it.isFile && it.extension == "pdf" }
                ?.map { file ->
                    InvoiceInfo(
                        fileName = file.name,
                        filePath = file.absolutePath,
                        fileSize = file.length(),
                        lastModified = file.lastModified(),
                        orderId = extractOrderIdFromFileName(file.name)
                    )
                }
                ?.sortedByDescending { it.lastModified }
                ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error al listar facturas", e)
            emptyList()
        }
    }
    
    /**
     * Elimina una factura específica
     * 
     * @param context Contexto de Android
     * @param pdfPath Ruta absoluta del archivo PDF
     * @return true si se eliminó correctamente, false en caso contrario
     */
    fun deleteInvoice(context: Context, pdfPath: String): Boolean {
        return try {
            val file = File(pdfPath)
            if (!file.exists()) {
                Log.w(TAG, "El archivo no existe: $pdfPath")
                return false
            }
            
            val deleted = file.delete()
            if (deleted) {
                Log.d(TAG, "Factura eliminada: ${file.name}")
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar factura", e)
            false
        }
    }
    
    /**
     * Obtiene información de una factura específica
     * 
     * @param pdfPath Ruta absoluta del archivo PDF
     * @return Información de la factura o null si no existe
     */
    fun getInvoiceInfo(pdfPath: String): InvoiceInfo? {
        return try {
            val file = File(pdfPath)
            if (!file.exists()) return null
            
            InvoiceInfo(
                fileName = file.name,
                filePath = file.absolutePath,
                fileSize = file.length(),
                lastModified = file.lastModified(),
                orderId = extractOrderIdFromFileName(file.name)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener información de factura", e)
            null
        }
    }
    
    /**
     * Verifica si existe una factura para una orden específica
     * 
     * @param context Contexto de Android
     * @param orderId ID de la orden
     * @return Ruta del archivo si existe, null en caso contrario
     */
    fun findInvoiceForOrder(context: Context, orderId: Long): String? {
        return try {
            val directory = File(context.getExternalFilesDir(null), INVOICE_DIRECTORY)
            if (!directory.exists()) return null
            
            directory.listFiles()
                ?.filter { it.isFile && it.extension == "pdf" }
                ?.firstOrNull { extractOrderIdFromFileName(it.name) == orderId }
                ?.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar factura para orden $orderId", e)
            null
        }
    }
    
    /**
     * Obtiene el tamaño total ocupado por las facturas
     * 
     * @param context Contexto de Android
     * @return Tamaño total en bytes
     */
    fun getTotalInvoicesSize(context: Context): Long {
        return try {
            val directory = File(context.getExternalFilesDir(null), INVOICE_DIRECTORY)
            if (!directory.exists()) return 0L
            
            directory.listFiles()
                ?.filter { it.isFile && it.extension == "pdf" }
                ?.sumOf { it.length() }
                ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular tamaño de facturas", e)
            0L
        }
    }
    
    /**
     * Extrae el ID de orden del nombre del archivo
     * Formato esperado: factura_{orderId}_{timestamp}.pdf
     */
    private fun extractOrderIdFromFileName(fileName: String): Long? {
        return try {
            val parts = fileName.replace(".pdf", "").split("_")
            if (parts.size >= 2 && parts[0] == "factura") {
                parts[1].toLongOrNull()
            } else null
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Información de una factura
 */
data class InvoiceInfo(
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val lastModified: Long,
    val orderId: Long?
) {
    /**
     * Obtiene el tamaño del archivo en formato legible
     */
    fun getReadableSize(): String {
        val kb = fileSize / 1024.0
        val mb = kb / 1024.0
        
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$fileSize bytes"
        }
    }
    
    /**
     * Obtiene la fecha de modificación en formato legible
     */
    fun getReadableDate(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("es", "CO"))
        return sdf.format(java.util.Date(lastModified))
    }
}
