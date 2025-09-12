package com.example.tiendasuplementacion.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.tiendasuplementacion.model.Order
import java.io.File
import java.io.FileWriter

object ExportUtils {
    fun ordersToCsv(orders: List<Order>): String {
        val sb = StringBuilder()
        sb.append("order_id,user_id,status_id,total,date_order,total_products\n")
        orders.forEach { o ->
            sb.append("${o.order_id},${o.user_id},${o.status_id},${o.total},${o.date_order},${o.total_products}\n")
        }
        return sb.toString()
    }
    
    suspend fun exportOrdersToFile(context: Context, orders: List<Order>): File {
        val csv = ordersToCsv(orders)
        val fileName = "orders_export_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        FileWriter(file).use { writer ->
            writer.write(csv)
        }
        
        return file
    }
    
    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Compartir archivo"))
    }
    
    suspend fun generateInvoicePdf(context: Context, order: Order): File {
        // Implementar generación PDF con iText
        val fileName = "invoice_${order.order_id}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        
        // TODO: Implementar con iText
        // Por ahora crear archivo placeholder
        file.writeText("PDF placeholder for order ${order.order_id}")
        
        return file
    }
}
