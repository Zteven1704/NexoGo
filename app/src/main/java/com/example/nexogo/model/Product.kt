package com.example.nexogo.model

import com.google.firebase.Timestamp

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val sku: String = "", // Código de producto
    val barcode: String = "", // Código de barras
    val currentStock: Int = 0,
    val quantity: Int = 0, // Cantidad para compatibilidad
    val minStock: Int = 5, // Stock mínimo para alertas
    val maxStock: Int = 100, // Stock máximo recomendado
    val unitPrice: Double = 0.0, // Precio unitario
    val totalPrice: Double = 0.0, // Precio total para compatibilidad
    val salePrice: Double = 0.0, // Precio de venta
    val costPrice: Double = 0.0, // Precio de costo
    val imageUrl: String = "",
    val location: String = "", // Ubicación del producto
    val isActive: Boolean = true,
    val isPrescriptionRequired: Boolean = false, // Si requiere receta
    val supplier: String = "", // Proveedor
    val supplierContact: String = "",
    val expirationDate: Timestamp? = null, // Fecha de vencimiento
    val batchNumber: String = "", // Número de lote
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = "", // ID del usuario que creó el producto
    val lastRestockedAt: Timestamp? = null,
    val lastSoldAt: Timestamp? = null
)

data class ProductCategory(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = ""
)