package com.example.nexogo.modules.inventory.utils

import com.example.nexogo.modules.inventory.model.Product
import com.example.nexogo.modules.inventory.model.StockStatus
import java.text.NumberFormat
import java.util.Locale

object InventoryUtils {
    
    /**
     * Formatea un precio como moneda
     */
    fun formatPrice(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        return formatter.format(price)
    }
    
    /**
     * Formatea un precio como moneda colombiana
     */
    fun formatPriceCOP(price: Double): String {
        return "$${String.format("%,.0f", price)}"
    }
    
    /**
     * Obtiene el color del estado de stock
     */
    fun getStockStatusColor(stockStatus: StockStatus): Long {
        return when (stockStatus) {
            StockStatus.OUT_OF_STOCK -> 0xFFE57373.toLong() // Rojo
            StockStatus.LOW_STOCK -> 0xFFFFB74D.toLong() // Naranja
            StockStatus.MEDIUM_STOCK -> 0xFFFFF176.toLong() // Amarillo
            StockStatus.HIGH_STOCK -> 0xFF81C784.toLong() // Verde
        }
    }
    
    /**
     * Obtiene el texto del estado de stock
     */
    fun getStockStatusText(stockStatus: StockStatus): String {
        return when (stockStatus) {
            StockStatus.OUT_OF_STOCK -> "Sin Stock"
            StockStatus.LOW_STOCK -> "Stock Bajo"
            StockStatus.MEDIUM_STOCK -> "Stock Medio"
            StockStatus.HIGH_STOCK -> "Stock Alto"
        }
    }
    
    /**
     * Calcula el valor total del inventario
     */
    fun calculateTotalInventoryValue(products: List<Product>): Double {
        return products.sumOf { it.totalValue }
    }
    
    /**
     * Obtiene el número de productos con stock bajo
     */
    fun getLowStockCount(products: List<Product>, threshold: Int = 10): Int {
        return products.count { it.quantity <= threshold }
    }
    
    /**
     * Obtiene el número de productos sin stock
     */
    fun getOutOfStockCount(products: List<Product>): Int {
        return products.count { it.quantity <= 0 }
    }
    
    /**
     * Valida si un producto tiene datos válidos
     */
    fun isValidProduct(product: Product): Boolean {
        return product.name.isNotBlank() &&
               product.description.isNotBlank() &&
               product.categoryId.isNotBlank() &&
               product.unitPrice > 0 &&
               product.quantity >= 0
    }
    
    /**
     * Obtiene el mensaje de validación para un producto
     */
    fun getValidationMessage(product: Product): String? {
        return when {
            product.name.isBlank() -> "El nombre del producto es obligatorio"
            product.description.isBlank() -> "La descripción del producto es obligatoria"
            product.categoryId.isBlank() -> "Debe seleccionar una categoría"
            product.unitPrice <= 0 -> "El precio unitario debe ser mayor a 0"
            product.quantity < 0 -> "La cantidad no puede ser negativa"
            else -> null
        }
    }
    
    /**
     * Genera un código de producto único
     */
    fun generateProductCode(categoryName: String, productCount: Int): String {
        val categoryPrefix = categoryName.take(3).uppercase()
        val paddedCount = String.format("%04d", productCount + 1)
        return "$categoryPrefix-$paddedCount"
    }
    
    /**
     * Obtiene estadísticas del inventario
     */
    fun getInventoryStats(products: List<Product>): InventoryStats {
        val totalProducts = products.size
        val totalValue = calculateTotalInventoryValue(products)
        val lowStockCount = getLowStockCount(products)
        val outOfStockCount = getOutOfStockCount(products)
        val averagePrice = if (totalProducts > 0) totalValue / totalProducts else 0.0
        
        return InventoryStats(
            totalProducts = totalProducts,
            totalValue = totalValue,
            lowStockCount = lowStockCount,
            outOfStockCount = outOfStockCount,
            averagePrice = averagePrice
        )
    }
}

/**
 * Estadísticas del inventario
 */
data class InventoryStats(
    val totalProducts: Int,
    val totalValue: Double,
    val lowStockCount: Int,
    val outOfStockCount: Int,
    val averagePrice: Double
)

