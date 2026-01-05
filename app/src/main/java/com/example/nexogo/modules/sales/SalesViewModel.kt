package com.example.nexogo.modules.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.core.models.Sale
import com.example.nexogo.core.models.SaleItem
import com.example.nexogo.core.models.VeterinaryService
import com.example.nexogo.core.models.PaymentMethod
import com.example.nexogo.core.models.PaymentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.google.firebase.Timestamp
import java.util.*

/**
 * ViewModel para gestión de ventas y servicios
 */
class SalesViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()
    
    private val _products = MutableStateFlow<List<com.example.nexogo.core.models.Product>>(emptyList())
    val products: StateFlow<List<com.example.nexogo.core.models.Product>> = _products.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredSales = MutableStateFlow<List<Sale>>(emptyList())
    val filteredSales: StateFlow<List<Sale>> = _filteredSales.asStateFlow()
    
    init {
        loadSales()
        loadProducts()
    }
    
    fun loadSales() {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val result = repository.getCollection("sales")
                if (result.isSuccess) {
                    val salesData = result.getOrNull() ?: emptyList()
                    val sales = salesData.mapNotNull { data ->
                        try {
                            Sale(
                                id = data["id"] as? String ?: "",
                                patientId = data["patientId"] as? String ?: "",
                                patientName = data["patientName"] as? String ?: "",
                                ownerId = data["ownerId"] as? String ?: "",
                                ownerName = data["ownerName"] as? String ?: "",
                                items = (data["items"] as? List<Map<String, Any>>)?.mapNotNull { itemData ->
                                    try {
                                        SaleItem(
                                            productId = itemData["productId"] as? String ?: "",
                                            productName = itemData["productName"] as? String ?: "",
                                            quantity = (itemData["quantity"] as? Number)?.toInt() ?: 0,
                                            unitPrice = (itemData["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                                            totalPrice = (itemData["totalPrice"] as? Number)?.toDouble() ?: 0.0
                                        )
                                    } catch (e: Exception) {
                                        Log.e("NEXOGO_SALES", "Error parseando item: ${e.message}")
                                        null
                                    }
                                } ?: emptyList(),
                                services = (data["services"] as? List<Map<String, Any>>)?.mapNotNull { serviceData ->
                                    try {
                                        VeterinaryService(
                                            serviceId = serviceData["serviceId"] as? String ?: "",
                                            name = serviceData["name"] as? String ?: "",
                                            description = serviceData["description"] as? String ?: "",
                                            price = (serviceData["price"] as? Number)?.toDouble() ?: 0.0
                                        )
                                    } catch (e: Exception) {
                                        Log.e("NEXOGO_SALES", "Error parseando servicio: ${e.message}")
                                        null
                                    }
                                } ?: emptyList(),
                                totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                                paymentMethod = PaymentMethod.valueOf(data["paymentMethod"] as? String ?: "CASH"),
                                paymentStatus = PaymentStatus.valueOf(data["paymentStatus"] as? String ?: "PENDING"),
                                invoiceUrl = data["invoiceUrl"] as? String ?: "",
                                notes = data["notes"] as? String ?: "",
                                createdBy = data["createdBy"] as? String ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e("NEXOGO_SALES", "Error parseando venta: ${e.message}")
                            null
                        }
                    }
                    _sales.value = sales
                    _filteredSales.value = sales
                    _message.value = "Ventas cargadas exitosamente"
                    Log.d("NEXOGO_SALES", "Ventas cargadas: ${sales.size}")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_SALES", "Error cargando ventas: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_SALES", "Excepción cargando ventas: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            try {
                val result = repository.getCollection("inventory")
                if (result.isSuccess) {
                    val productsData = result.getOrNull() ?: emptyList()
                    val products = productsData.mapNotNull { data ->
                        try {
                            com.example.nexogo.core.models.Product(
                                id = data["id"] as? String ?: "",
                                name = data["name"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                category = data["category"] as? String ?: "",
                                quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                                unitPrice = (data["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                                totalPrice = (data["totalPrice"] as? Number)?.toDouble() ?: 0.0,
                                imageUrl = data["imageUrl"] as? String ?: "",
                                lowStockThreshold = (data["lowStockThreshold"] as? Number)?.toInt() ?: 5,
                                isActive = data["isActive"] as? Boolean ?: true
                            )
                        } catch (e: Exception) {
                            Log.e("NEXOGO_SALES", "Error parseando producto: ${e.message}")
                            null
                        }
                    }
                    _products.value = products
                    Log.d("NEXOGO_SALES", "Productos cargados: ${products.size}")
                } else {
                    Log.e("NEXOGO_SALES", "Error cargando productos: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("NEXOGO_SALES", "Excepción cargando productos: ${e.message}")
            }
        }
    }
    
    fun createSale(
        patientId: String,
        patientName: String,
        ownerId: String,
        ownerName: String,
        items: List<SaleItem>,
        services: List<VeterinaryService>,
        paymentMethod: PaymentMethod,
        notes: String,
        createdBy: String
    ) {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val saleId = repository.generateId()
                val totalAmount = items.sumOf { it.totalPrice } + services.sumOf { it.price }
                
                val sale = Sale(
                    id = saleId,
                    patientId = patientId,
                    patientName = patientName,
                    ownerId = ownerId,
                    ownerName = ownerName,
                    items = items,
                    services = services,
                    totalAmount = totalAmount,
                    paymentMethod = paymentMethod,
                    paymentStatus = PaymentStatus.PENDING,
                    notes = notes,
                    createdBy = createdBy
                )
                
                val saleData = mapOf(
                    "id" to sale.id,
                    "patientId" to sale.patientId,
                    "patientName" to sale.patientName,
                    "ownerId" to sale.ownerId,
                    "ownerName" to sale.ownerName,
                    "items" to sale.items.map { item ->
                        mapOf(
                            "productId" to item.productId,
                            "productName" to item.productName,
                            "quantity" to item.quantity,
                            "unitPrice" to item.unitPrice,
                            "totalPrice" to item.totalPrice
                        )
                    },
                    "services" to sale.services.map { service ->
                        mapOf(
                            "serviceId" to service.serviceId,
                            "name" to service.name,
                            "description" to service.description,
                            "price" to service.price
                        )
                    },
                    "totalAmount" to sale.totalAmount,
                    "paymentMethod" to sale.paymentMethod.name,
                    "paymentStatus" to sale.paymentStatus.name,
                    "invoiceUrl" to sale.invoiceUrl,
                    "notes" to sale.notes,
                    "createdBy" to sale.createdBy,
                    "createdAt" to repository.getCurrentTimestamp(),
                    "updatedAt" to repository.getCurrentTimestamp()
                )
                
                val result = repository.createDocument("sales", saleId, saleData)
                if (result.isSuccess) {
                    // Actualizar stock de productos
                    updateProductStock(items)
                    
                    // Generar factura PDF
                    val invoiceUrl = generateInvoice(sale)
                    
                    // Actualizar venta con URL de factura
                    val updateData = mapOf(
                        "invoiceUrl" to invoiceUrl,
                        "updatedAt" to repository.getCurrentTimestamp()
                    )
                    repository.updateDocument("sales", saleId, updateData)
                    
                    _sales.value = _sales.value + sale.copy(invoiceUrl = invoiceUrl)
                    _filteredSales.value = _filteredSales.value + sale.copy(invoiceUrl = invoiceUrl)
                    _message.value = "Venta creada exitosamente"
                    Log.d("NEXOGO_SALES", "Venta creada: $saleId")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_SALES", "Error creando venta: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_SALES", "Excepción creando venta: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updatePaymentStatus(saleId: String, status: PaymentStatus) {
        viewModelScope.launch {
            try {
                val updateData = mapOf(
                    "paymentStatus" to status.name,
                    "updatedAt" to repository.getCurrentTimestamp()
                )
                
                val result = repository.updateDocument("sales", saleId, updateData)
                if (result.isSuccess) {
                    _sales.value = _sales.value.map { sale ->
                        if (sale.id == saleId) {
                            sale.copy(paymentStatus = status)
                        } else {
                            sale
                        }
                    }
                    _filteredSales.value = _filteredSales.value.map { sale ->
                        if (sale.id == saleId) {
                            sale.copy(paymentStatus = status)
                        } else {
                            sale
                        }
                    }
                    _message.value = "Estado de pago actualizado"
                    Log.d("NEXOGO_SALES", "Estado de pago actualizado: $saleId -> $status")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_SALES", "Error actualizando estado: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_SALES", "Excepción actualizando estado: ${e.message}")
            }
        }
    }
    
    fun deleteSale(saleId: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteDocument("sales", saleId)
                if (result.isSuccess) {
                    _sales.value = _sales.value.filter { it.id != saleId }
                    _filteredSales.value = _filteredSales.value.filter { it.id != saleId }
                    _message.value = "Venta eliminada exitosamente"
                    Log.d("NEXOGO_SALES", "Venta eliminada: $saleId")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_SALES", "Error eliminando venta: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_SALES", "Excepción eliminando venta: ${e.message}")
            }
        }
    }
    
    fun searchSales(query: String) {
        _searchQuery.value = query
        
        if (query.isEmpty()) {
            _filteredSales.value = _sales.value
        } else {
            val filtered = _sales.value.filter { sale ->
                sale.patientName.contains(query, ignoreCase = true) ||
                sale.ownerName.contains(query, ignoreCase = true) ||
                sale.id.contains(query, ignoreCase = true)
            }
            _filteredSales.value = filtered
        }
    }
    
    fun getSalesByPatient(patientId: String): List<Sale> {
        return _sales.value.filter { it.patientId == patientId }
    }
    
    fun getSalesByDateRange(startDate: Date, endDate: Date): List<Sale> {
        return _sales.value.filter { sale ->
            val saleDate = sale.createdAt.toDate()
            saleDate >= startDate && saleDate <= endDate
        }
    }
    
    fun getTotalSales(): Double {
        return _sales.value.sumOf { it.totalAmount }
    }
    
    fun getTotalSalesByPaymentMethod(paymentMethod: PaymentMethod): Double {
        return _sales.value.filter { it.paymentMethod == paymentMethod }.sumOf { it.totalAmount }
    }
    
    fun getTopSellingProducts(): List<Pair<String, Int>> {
        val productSales = mutableMapOf<String, Int>()
        _sales.value.forEach { sale ->
            sale.items.forEach { item ->
                productSales[item.productName] = (productSales[item.productName] ?: 0) + item.quantity
            }
        }
        return productSales.toList().sortedByDescending { it.second }.take(5)
    }
    
    private suspend fun updateProductStock(items: List<SaleItem>) {
        items.forEach { item ->
            val product = _products.value.find { it.id == item.productId }
            if (product != null) {
                val newQuantity = product.quantity - item.quantity
                if (newQuantity >= 0) {
                    val updateData = mapOf(
                        "quantity" to newQuantity,
                        "totalPrice" to (newQuantity * product.unitPrice),
                        "updatedAt" to repository.getCurrentTimestamp()
                    )
                    repository.updateDocument("inventory", item.productId, updateData)
                    Log.d("NEXOGO_SALES", "Stock actualizado: ${product.name} -> $newQuantity")
                } else {
                    Log.w("NEXOGO_SALES", "⚠️ Stock insuficiente: ${product.name}")
                }
            }
        }
    }
    
    private suspend fun generateInvoice(sale: Sale): String {
        // TODO: Implementar generación de PDF
        val invoicePath = "invoices/${sale.id}/invoice.pdf"
        val invoiceContent = "Factura para ${sale.patientName} - Total: $${sale.totalAmount}"
        
        val result = repository.uploadBytes(invoicePath, invoiceContent.toByteArray())
        return if (result.isSuccess) {
            result.getOrNull() ?: ""
        } else {
            Log.e("NEXOGO_SALES", "Error generando factura: ${result.exceptionOrNull()?.message}")
            ""
        }
    }
    
    fun clearMessage() {
        _message.value = ""
    }
}

