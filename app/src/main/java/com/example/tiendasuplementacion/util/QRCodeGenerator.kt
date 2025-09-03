package com.example.tiendasuplementacion.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

object QRCodeGenerator {
    fun generateQRCode(content: String, width: Int = 512, height: Int = 512): Bitmap {
        try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)  // Highest error correction
                put(EncodeHintType.MARGIN, 1)  // Small margin for better scanning
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val bitmap = createBitmap(width, height)

            // Create QR Code with black modules on white background
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }

            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            // Return a simple error bitmap if QR generation fails
            return createBitmap(width, height).apply {
                eraseColor(Color.WHITE)
            }
        }
    }
} 