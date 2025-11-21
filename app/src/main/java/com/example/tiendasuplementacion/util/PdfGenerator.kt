package com.example.tiendasuplementacion.util

import android.content.Context
import android.util.Log
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocumentInfo
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.example.tiendasuplementacion.model.UserOrder
import com.example.tiendasuplementacion.model.OrderProduct
import com.example.tiendasuplementacion.model.OrderProductDetail
import com.example.tiendasuplementacion.model.Product
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Generador moderno y escalable de facturas en PDF
 * 
 * Características:
 * - Generación asíncrona optimizada
 * - Gestión automática de archivos
 * - Validación de datos robusta
 * - Logging estructurado
 * - Diseño profesional y consistente
 * - Arquitectura modular y extensible
 */
object PdfGenerator {
    private const val TAG = "PdfGenerator"
    private const val INVOICE_DIRECTORY = "facturas"
    private const val APP_VERSION = "1.0.0"
    private const val IVA_RATE = 0.19 // 19% IVA Colombia
    private const val MAX_PDF_AGE_DAYS = 30L
    
    // Configuración de colores corporativos
    private object BrandColors {
        val PRIMARY = DeviceRgb(63, 81, 181)
        val SECONDARY = DeviceRgb(96, 125, 139)
        val ACCENT = DeviceRgb(255, 152, 0)
        val BACKGROUND_LIGHT = DeviceRgb(250, 250, 250)
        val BACKGROUND_GRAY = DeviceRgb(240, 240, 240)
        val TEXT_DARK = DeviceRgb(33, 33, 33)
        val TEXT_GRAY = DeviceRgb(117, 117, 117)
        val SUCCESS = DeviceRgb(76, 175, 80)
        val ERROR = DeviceRgb(244, 67, 54)
    }
    
    // Configuración de tipografía
    private object Typography {
        const val TITLE_SIZE = 24f
        const val SUBTITLE_SIZE = 18f
        const val HEADING_SIZE = 14f
        const val BODY_SIZE = 12f
        const val SMALL_SIZE = 10f
        const val CAPTION_SIZE = 8f
    }
    /**
     * Genera una factura PDF con detalles completos de la orden
     * 
     * @param context Contexto de Android
     * @param order Orden del usuario
     * @param orderProductRepository Repositorio para obtener detalles de productos
     * @return Ruta absoluta del archivo PDF generado
     * @throws InvoiceGenerationException Si hay un error en la generación
     */
    suspend fun generateInvoicePdfWithDetails(
        context: Context,
        order: UserOrder,
        orderProductRepository: com.example.tiendasuplementacion.repository.OrderProductRepository
    ): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Iniciando generación de factura para orden #${order.order_id}")
        
        // Validar orden antes de procesar
        validateOrder(order)
        
        // Obtener los detalles de la orden desde el repositorio
        val orderDetails = try {
            orderProductRepository.getByOrderId(order.order_id).also {
                Log.d(TAG, "Detalles obtenidos: ${it.size} productos")
            }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudieron obtener detalles de productos, usando fallback", e)
            emptyList<OrderProductDetail>()
        }
        
        // Limpiar facturas antiguas en background
        cleanOldInvoices(context)
        
        generateInvoicePdf(context, order, orderDetails)
    }
    
    /**
     * Valida que la orden tenga todos los datos necesarios
     */
    private fun validateOrder(order: UserOrder) {
        require(order.order_id > 0) { "ID de orden inválido: ${order.order_id}" }
        require(order.products.isNotEmpty()) { "La orden no tiene productos" }
        require(order.total > 0) { "Total de orden inválido: ${order.total}" }
        
        if (order.additionalInfoPayment == null) {
            Log.w(TAG, "Advertencia: Orden #${order.order_id} sin información de facturación")
        }
    }
    
    /**
     * Limpia archivos PDF más antiguos de MAX_PDF_AGE_DAYS días
     */
    private fun cleanOldInvoices(context: Context) {
        try {
            val directory = File(context.getExternalFilesDir(null), INVOICE_DIRECTORY)
            if (!directory.exists()) return
            
            val cutoffTime = System.currentTimeMillis() - (MAX_PDF_AGE_DAYS * 24 * 60 * 60 * 1000)
            var deletedCount = 0
            
            directory.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
            
            if (deletedCount > 0) {
                Log.d(TAG, "Limpieza completada: $deletedCount archivos eliminados")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al limpiar facturas antiguas", e)
        }
    }

    /**
     * Genera el documento PDF de la factura
     * 
     * @param context Contexto de Android
     * @param order Orden del usuario
     * @param orderDetails Lista de detalles de productos
     * @return Ruta absoluta del archivo PDF generado
     */
    private fun generateInvoicePdf(
        context: Context, 
        order: UserOrder, 
        orderDetails: List<OrderProductDetail> = emptyList()
    ): String {
        val startTime = System.currentTimeMillis()
        
        try {
            // Preparar directorio y archivo
            val file = prepareInvoiceFile(context, order.order_id)
            Log.d(TAG, "Generando PDF en: ${file.absolutePath}")

            // Configurar documento PDF
            val writer = PdfWriter(file)
            val pdf = PdfDocument(writer)
            configurePdfMetadata(pdf, order)
            
            val document = Document(pdf, PageSize.A4)
            document.setMargins(40f, 40f, 40f, 40f)

            // Formateadores
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CO"))

            // Fuentes
            val boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD)
            val regularFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA)
            val italicFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_OBLIQUE)

            // Construir documento
            addHeader(document, order, boldFont, dateFormat)
            addCustomerInfo(document, order, boldFont, regularFont, italicFont)
            addOrderStatus(document, order, boldFont)
            
            val invoiceData = addProductsTable(document, order, orderDetails, boldFont, regularFont, currencyFormat)
            
            addAdditionalInfo(document, invoiceData, boldFont, regularFont, currencyFormat)
            addFooter(document, boldFont, italicFont)

            document.close()
            
            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Factura generada exitosamente en ${duration}ms: ${file.name}")
            
            return file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error generando factura para orden #${order.order_id}", e)
            throw InvoiceGenerationException("Error al generar la factura: ${e.message}", e)
        }
    }
    
    /**
     * Prepara el directorio y archivo para la factura
     */
    private fun prepareInvoiceFile(context: Context, orderId: Long): File {
        val directory = File(context.getExternalFilesDir(null), INVOICE_DIRECTORY)
        if (!directory.exists()) {
            directory.mkdirs()
            Log.d(TAG, "Directorio de facturas creado: ${directory.absolutePath}")
        }

        val timestamp = System.currentTimeMillis()
        val fileName = "factura_${orderId}_${timestamp}.pdf"
        return File(directory, fileName)
    }
    
    /**
     * Configura los metadatos del PDF
     */
    private fun configurePdfMetadata(pdf: PdfDocument, order: UserOrder) {
        pdf.documentInfo.apply {
            setTitle("Factura #${order.order_id}")
            setAuthor("Tienda Suplementación")
            setCreator("TiendaApp v$APP_VERSION")
            setSubject("Factura de compra")
            addCreationDate()
        }
    }
    
    /**
     * Agrega el encabezado del documento
     */
    private fun addHeader(document: Document, order: UserOrder, boldFont: com.itextpdf.kernel.font.PdfFont, dateFormat: SimpleDateFormat) {
        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(60f, 40f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)

        // Información de la empresa
        headerTable.addCell(
            Cell().add(
                Paragraph("Tienda Suplementación")
                    .setFontSize(Typography.TITLE_SIZE)
                    .setFont(boldFont)
                    .setFontColor(BrandColors.PRIMARY)
            ).add(
                Paragraph("Suplementos deportivos de calidad")
                    .setFontSize(Typography.SMALL_SIZE)
                    .setFontColor(BrandColors.TEXT_GRAY)
                    .setMarginTop(5f)
            ).setBorder(Border.NO_BORDER)
        )

        // Información de la factura
        val invoiceInfoCell = Cell().setBorder(Border.NO_BORDER)
        
        invoiceInfoCell.add(
            Paragraph("FACTURA")
                .setFontSize(Typography.TITLE_SIZE)
                .setFont(boldFont)
                .setFontColor(BrandColors.PRIMARY)
                .setTextAlignment(TextAlignment.RIGHT)
        ).add(
            Paragraph("No. ${order.order_id}")
                .setFontSize(Typography.HEADING_SIZE)
                .setFont(boldFont)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(5f)
        ).add(
            Paragraph("Fecha: ${order.date_order}")
                .setFontSize(Typography.BODY_SIZE)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(5f)
        )
        
        headerTable.addCell(invoiceInfoCell)
        document.add(headerTable)
        
        // Línea divisoria
        document.add(
            Paragraph("")
                .setBorderBottom(SolidBorder(BrandColors.PRIMARY, 2f))
                .setMarginBottom(20f)
        )
    }
    
    /**
     * Agrega la información del cliente
     */
    private fun addCustomerInfo(document: Document, order: UserOrder, boldFont: com.itextpdf.kernel.font.PdfFont, regularFont: com.itextpdf.kernel.font.PdfFont, italicFont: com.itextpdf.kernel.font.PdfFont) {

        val clienteTable = Table(UnitValue.createPercentArray(floatArrayOf(100f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setBackgroundColor(BrandColors.BACKGROUND_GRAY)
            .setPadding(15f)
            .setMarginBottom(20f)

        clienteTable.addCell(
            Cell().add(
                Paragraph("INFORMACIÓN DEL CLIENTE")
                    .setFont(boldFont)
                    .setFontSize(Typography.HEADING_SIZE)
                    .setFontColor(BrandColors.TEXT_DARK)
            ).setBorder(Border.NO_BORDER)
        )
        
        order.additionalInfoPayment?.let { paymentInfo ->
            val addressParts = listOfNotNull(
                paymentInfo.addressLine1,
                listOfNotNull(
                    paymentInfo.city,
                    paymentInfo.stateOrProvince,
                    paymentInfo.postalCode
                ).filter { it.isNotEmpty() }.joinToString(", "),
                paymentInfo.country
            ).filter { it.isNotEmpty() }
            
            if (addressParts.isNotEmpty()) {
                clienteTable.addCell(
                    Cell().add(
                        Paragraph(addressParts.joinToString("\n"))
                            .setFont(regularFont)
                            .setFontSize(Typography.BODY_SIZE)
                            .setFontColor(BrandColors.TEXT_DARK)
                    ).setBorder(Border.NO_BORDER)
                )
            }
        } ?: run {
            clienteTable.addCell(
                Cell().add(
                    Paragraph("Información de facturación no disponible")
                        .setFont(italicFont)
                        .setFontSize(Typography.BODY_SIZE)
                        .setFontColor(BrandColors.TEXT_GRAY)
                ).setBorder(Border.NO_BORDER)
            )
        }

        document.add(clienteTable)
    }
    
    /**
     * Agrega el estado de la orden
     */
    private fun addOrderStatus(document: Document, order: UserOrder, boldFont: com.itextpdf.kernel.font.PdfFont) {
        val statusColor = when (order.status.name.lowercase()) {
            "completado", "entregado" -> BrandColors.SUCCESS
            "cancelado", "rechazado" -> BrandColors.ERROR
            else -> BrandColors.ACCENT
        }
        
        document.add(
            Paragraph("Estado del pedido: ${order.status.name}")
                .setFont(boldFont)
                .setFontSize(Typography.BODY_SIZE)
                .setFontColor(statusColor)
                .setMarginBottom(15f)
        )
    }
    
    /**
     * Datos de la factura para cálculos
     */
    private data class InvoiceData(
        val subtotalSinIva: Double,
        val ivaTotal: Double,
        val totalConIva: Double,
        val totalProductos: Int
    )

    /**
     * Agrega la tabla de productos y retorna los datos de facturación
     */
    private fun addProductsTable(
        document: Document,
        order: UserOrder,
        orderDetails: List<OrderProductDetail>,
        boldFont: com.itextpdf.kernel.font.PdfFont,
        regularFont: com.itextpdf.kernel.font.PdfFont,
        currencyFormat: NumberFormat
    ): InvoiceData {
        // Título de la sección
        document.add(
            Paragraph("PRODUCTOS")
                .setFont(boldFont)
                .setFontSize(Typography.HEADING_SIZE)
                .setFontColor(BrandColors.TEXT_DARK)
                .setMarginBottom(10f)
        )
        
        // Crear tabla de productos
        val table = Table(UnitValue.createPercentArray(floatArrayOf(35f, 12f, 18f, 18f, 17f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)

        // Encabezados con estilo moderno
        listOf("Producto", "Cant.", "Precio Unit.", "Subtotal", "IVA (19%)").forEach { header ->
            table.addHeaderCell(
                Cell().add(
                    Paragraph(header)
                        .setFont(boldFont)
                        .setFontSize(Typography.SMALL_SIZE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(BrandColors.TEXT_DARK)
                ).setBackgroundColor(BrandColors.PRIMARY)
                    .setFontColor(DeviceRgb(255, 255, 255))
                    .setPadding(8f)
                    .setBorder(Border.NO_BORDER)
            )
        }

        // Acumuladores
        var subtotalConIva = 0.0
        var subtotalSinIva = 0.0
        var ivaTotal = 0.0

        // Obtener productos agrupados con cantidades correctas
        val productosAgrupados = getGroupedProducts(order, orderDetails)
        
        Log.d(TAG, "Procesando ${productosAgrupados.size} productos únicos")

        // Agregar filas de productos con diseño alternado
        productosAgrupados.forEachIndexed { index, productoAgrupado ->
            val precioConIva = productoAgrupado.producto.price
            val precioSinIva = precioConIva / (1 + IVA_RATE)
            
            val subtotalProductoConIva = precioConIva * productoAgrupado.cantidad
            val subtotalProductoSinIva = precioSinIva * productoAgrupado.cantidad
            val ivaProducto = subtotalProductoConIva - subtotalProductoSinIva
            
            subtotalConIva += subtotalProductoConIva
            subtotalSinIva += subtotalProductoSinIva
            ivaTotal += ivaProducto
            
            val rowColor = if (index % 2 == 0) BrandColors.BACKGROUND_LIGHT else DeviceRgb(255, 255, 255)

            // Nombre del producto
            table.addCell(
                Cell().add(
                    Paragraph(productoAgrupado.producto.name)
                        .setFont(regularFont)
                        .setFontSize(Typography.SMALL_SIZE)
                ).setBackgroundColor(rowColor)
                    .setPadding(8f)
                    .setBorder(Border.NO_BORDER)
            )
            
            // Cantidad
            table.addCell(
                Cell().add(
                    Paragraph(productoAgrupado.cantidad.toString())
                        .setFont(regularFont)
                        .setFontSize(Typography.SMALL_SIZE)
                        .setTextAlignment(TextAlignment.CENTER)
                ).setBackgroundColor(rowColor)
                    .setPadding(8f)
                    .setBorder(Border.NO_BORDER)
            )
            
            // Precio unitario (con IVA)
            table.addCell(
                Cell().add(
                    Paragraph(currencyFormat.format(precioConIva))
                        .setFont(regularFont)
                        .setFontSize(Typography.SMALL_SIZE)
                        .setTextAlignment(TextAlignment.RIGHT)
                ).setBackgroundColor(rowColor)
                    .setPadding(8f)
                    .setBorder(Border.NO_BORDER)
            )
            
            // Subtotal
            table.addCell(
                Cell().add(
                    Paragraph(currencyFormat.format(subtotalProductoConIva))
                        .setFont(boldFont)
                        .setFontSize(Typography.SMALL_SIZE)
                        .setTextAlignment(TextAlignment.RIGHT)
                ).setBackgroundColor(rowColor)
                    .setPadding(8f)
                    .setBorder(Border.NO_BORDER)
            )
            
            // IVA del producto
            table.addCell(
                Cell().add(
                    Paragraph(currencyFormat.format(ivaProducto))
                        .setFont(regularFont)
                        .setFontSize(Typography.CAPTION_SIZE)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontColor(BrandColors.TEXT_GRAY)
                ).setBackgroundColor(rowColor)
                    .setPadding(8f)
                    .setBorder(Border.NO_BORDER)
            )
        }

        // Fila de Subtotal (sin IVA)
        addTotalRow(table, "Subtotal (sin IVA):", currencyFormat.format(subtotalSinIva), 
            currencyFormat.format(ivaTotal), boldFont, BrandColors.BACKGROUND_GRAY)

        // Fila de Total Final
        addFinalTotalRow(table, "TOTAL", currencyFormat.format(subtotalConIva), boldFont)

        document.add(table)
        
        return InvoiceData(
            subtotalSinIva = subtotalSinIva,
            ivaTotal = ivaTotal,
            totalConIva = subtotalConIva,
            totalProductos = productosAgrupados.sumOf { it.cantidad }
        )
    }
    
    /**
     * Obtiene productos agrupados con cantidades correctas
     */
    private fun getGroupedProducts(order: UserOrder, orderDetails: List<OrderProductDetail>): List<ProductoAgrupado> {
        return if (orderDetails.isNotEmpty()) {
            // Usar detalles reales con cantidades correctas
            orderDetails.map { detail ->
                ProductoAgrupado(
                    producto = detail.product,
                    cantidad = detail.quantity
                )
            }
        } else {
            // Fallback: agrupar productos por ID
            Log.w(TAG, "Usando fallback para agrupar productos")
            order.products.groupBy { it.id }
                .map { (_, products) ->
                    val orderProduct = products.first()
                    ProductoAgrupado(
                        producto = Product(
                            id = orderProduct.id,
                            name = orderProduct.name,
                            description = orderProduct.description,
                            price = orderProduct.price,
                            stock = orderProduct.stock,
                            url_image = orderProduct.url_image
                        ),
                        cantidad = products.size
                    )
                }
        }
    }
    
    /**
     * Agrega una fila de total a la tabla
     */
    private fun addTotalRow(
        table: Table, 
        label: String, 
        value1: String, 
        value2: String,
        boldFont: com.itextpdf.kernel.font.PdfFont,
        backgroundColor: DeviceRgb
    ) {
        table.addCell(
            Cell(1, 3).add(
                Paragraph(label)
                    .setFont(boldFont)
                    .setFontSize(Typography.BODY_SIZE)
                    .setTextAlignment(TextAlignment.RIGHT)
            ).setBackgroundColor(backgroundColor)
                .setPadding(8f)
                .setBorder(Border.NO_BORDER)
        )
        
        table.addCell(
            Cell().add(
                Paragraph(value1)
                    .setFont(boldFont)
                    .setFontSize(Typography.BODY_SIZE)
                    .setTextAlignment(TextAlignment.RIGHT)
            ).setBackgroundColor(backgroundColor)
                .setPadding(8f)
                .setBorder(Border.NO_BORDER)
        )
        
        table.addCell(
            Cell().add(
                Paragraph(value2)
                    .setFont(boldFont)
                    .setFontSize(Typography.BODY_SIZE)
                    .setTextAlignment(TextAlignment.RIGHT)
            ).setBackgroundColor(backgroundColor)
                .setPadding(8f)
                .setBorder(Border.NO_BORDER)
        )
    }
    
    /**
     * Agrega la fila de total final
     */
    private fun addFinalTotalRow(
        table: Table,
        label: String,
        value: String,
        boldFont: com.itextpdf.kernel.font.PdfFont
    ) {
        table.addCell(
            Cell(1, 3).add(
                Paragraph(label)
                    .setFont(boldFont)
                    .setFontSize(Typography.HEADING_SIZE)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(DeviceRgb(255, 255, 255))
            ).setBackgroundColor(BrandColors.PRIMARY)
                .setPadding(10f)
                .setBorder(Border.NO_BORDER)
        )
        
        table.addCell(
            Cell(1, 2).add(
                Paragraph(value)
                    .setFont(boldFont)
                    .setFontSize(Typography.HEADING_SIZE)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(DeviceRgb(255, 255, 255))
            ).setBackgroundColor(BrandColors.PRIMARY)
                .setPadding(10f)
                .setBorder(Border.NO_BORDER)
        )
    }

    /**
     * Agrega información adicional y términos
     */
    private fun addAdditionalInfo(
        document: Document,
        invoiceData: InvoiceData,
        boldFont: com.itextpdf.kernel.font.PdfFont,
        regularFont: com.itextpdf.kernel.font.PdfFont,
        currencyFormat: NumberFormat
    ) {
        document.add(Paragraph("").setHeight(20f))
        
        // Resumen fiscal
        val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setBackgroundColor(BrandColors.BACKGROUND_LIGHT)
            .setPadding(15f)
            .setMarginBottom(20f)
        
        summaryTable.addCell(
            Cell(1, 2).add(
                Paragraph("RESUMEN FISCAL")
                    .setFont(boldFont)
                    .setFontSize(Typography.HEADING_SIZE)
                    .setFontColor(BrandColors.TEXT_DARK)
            ).setBorder(Border.NO_BORDER)
                .setPaddingBottom(10f)
        )
        
        // Detalles fiscales
        val fiscalDetails = listOf(
            "Base gravable (sin IVA)" to currencyFormat.format(invoiceData.subtotalSinIva),
            "IVA (19%)" to currencyFormat.format(invoiceData.ivaTotal),
            "Total productos" to invoiceData.totalProductos.toString(),
            "Total a pagar" to currencyFormat.format(invoiceData.totalConIva)
        )
        
        fiscalDetails.forEach { (label, value) ->
            summaryTable.addCell(
                Cell().add(
                    Paragraph(label)
                        .setFont(regularFont)
                        .setFontSize(Typography.SMALL_SIZE)
                        .setFontColor(BrandColors.TEXT_GRAY)
                ).setBorder(Border.NO_BORDER)
                    .setPadding(3f)
            )
            
            summaryTable.addCell(
                Cell().add(
                    Paragraph(value)
                        .setFont(boldFont)
                        .setFontSize(Typography.SMALL_SIZE)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontColor(BrandColors.TEXT_DARK)
                ).setBorder(Border.NO_BORDER)
                    .setPadding(3f)
            )
        }
        
        document.add(summaryTable)
        
        // Términos y condiciones
        document.add(
            Paragraph("TÉRMINOS Y CONDICIONES")
                .setFont(boldFont)
                .setFontSize(Typography.HEADING_SIZE)
                .setFontColor(BrandColors.TEXT_DARK)
                .setMarginBottom(10f)
        )
        
        val terms = listOf(
            "Esta factura electrónica tiene validez legal",
            "Los precios incluyen IVA del 19% según legislación colombiana",
            "La factura sirve como garantía del producto",
            "Para devoluciones conserve este documento",
            "Plazo para devoluciones: 30 días calendario",
            "Consultas: contacto@tiendasuplementacion.com"
        )
        
        terms.forEach { term ->
            document.add(
                Paragraph("• $term")
                    .setFont(regularFont)
                    .setFontSize(Typography.CAPTION_SIZE)
                    .setFontColor(BrandColors.TEXT_GRAY)
                    .setMarginLeft(10f)
                    .setMarginBottom(3f)
            )
        }
    }
    
    /**
     * Agrega el pie de página
     */
    private fun addFooter(
        document: Document,
        boldFont: com.itextpdf.kernel.font.PdfFont,
        italicFont: com.itextpdf.kernel.font.PdfFont
    ) {
        document.add(Paragraph("").setHeight(30f))
        
        // Mensaje de agradecimiento
        document.add(
            Paragraph("¡Gracias por tu compra!")
                .setFont(boldFont)
                .setFontSize(Typography.SUBTITLE_SIZE)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(BrandColors.PRIMARY)
                .setMarginBottom(5f)
        )
        
        // Información de contacto
        document.add(
            Paragraph("www.tiendasuplementacion.com")
                .setFont(italicFont)
                .setFontSize(Typography.SMALL_SIZE)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(BrandColors.TEXT_GRAY)
        )
        
        document.add(
            Paragraph("Suplementos deportivos de calidad garantizada")
                .setFont(italicFont)
                .setFontSize(Typography.CAPTION_SIZE)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(BrandColors.TEXT_GRAY)
                .setMarginTop(5f)
        )
    }
}

/**
 * Modelo interno para agrupar productos con sus cantidades
 */
private data class ProductoAgrupado(
    val producto: Product,
    val cantidad: Int
)

/**
 * Excepción personalizada para errores en la generación de facturas
 */
class InvoiceGenerationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) 