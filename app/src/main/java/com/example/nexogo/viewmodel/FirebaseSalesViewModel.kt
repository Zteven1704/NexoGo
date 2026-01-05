package com.example.nexogo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.model.Sale
import com.example.nexogo.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirebaseSalesViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    init {
        // Escuchar cambios en tiempo real
        viewModelScope.launch {
            repository.listenToSales().collect { salesList ->
                _sales.value = salesList
            }
        }
    }

    /**
     * Carga todas las ventas
     */
    fun loadSales() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getAllSales()
                if (result.isSuccess) {
                    _sales.value = result.getOrNull() ?: emptyList()
                    _message.value = "Ventas cargadas exitosamente"
                } else {
                    _message.value = "Error al cargar ventas: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar ventas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Guarda una nueva venta
     */
    fun saveSale(sale: Sale) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addSale(sale)
                if (result.isSuccess) {
                    _message.value = "Venta guardada exitosamente"
                } else {
                    _message.value = "Error al guardar venta: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al guardar venta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza una venta existente
     */
    fun updateSale(sale: Sale) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addSale(sale)
                if (result.isSuccess) {
                    _message.value = "Venta actualizada exitosamente"
                } else {
                    _message.value = "Error al actualizar venta: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al actualizar venta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina una venta
     */
    fun deleteSale(saleId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.deleteSale(saleId)
                if (result.isSuccess) {
                    _message.value = "Venta eliminada exitosamente"
                } else {
                    _message.value = "Error al eliminar venta: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al eliminar venta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtiene resumen de ventas
     */
    fun getSalesSummary(): SalesSummary {
        val sales = _sales.value
        val totalSales = sales.sumOf { it.total }
        val totalTransactions = sales.size
        val averageSale = if (totalTransactions > 0) totalSales / totalTransactions else 0.0
        val paidSales = sales.count { it.paymentStatus == com.example.nexogo.model.PaymentStatus.PAID }
        
        return SalesSummary(
            totalSales = totalSales,
            totalTransactions = totalTransactions,
            averageSale = averageSale,
            paidSales = paidSales
        )
    }

    /**
     * Obtiene los productos más vendidos
     */
    fun getTopSellingProducts(limit: Int = 5): List<ProductSales> {
        val sales = _sales.value
        val productSales = mutableMapOf<String, ProductSales>()
        
        sales.forEach { sale ->
            sale.items.forEach { item ->
                val existing = productSales[item.productId]
                if (existing != null) {
                    productSales[item.productId] = existing.copy(
                        quantity = existing.quantity + item.quantity,
                        total = existing.total + item.totalPrice
                    )
                } else {
                    productSales[item.productId] = ProductSales(
                        productId = item.productId,
                        productName = item.productName,
                        quantity = item.quantity,
                        total = item.totalPrice
                    )
                }
            }
        }
        
        return productSales.values.sortedByDescending { it.total }.take(limit)
    }

    /**
     * Obtiene ventas por método de pago
     */
    fun getSalesByPaymentMethod(): Map<com.example.nexogo.model.PaymentMethod, Double> {
        val sales = _sales.value
        return sales.groupBy { it.paymentMethod }
            .mapValues { (_, salesList) -> salesList.sumOf { it.total } }
    }

    /**
     * Busca ventas por cliente
     */
    fun searchSalesByClient(clientName: String): List<Sale> {
        return _sales.value.filter { sale ->
            sale.clientName.contains(clientName, ignoreCase = true)
        }
    }

    /**
     * Busca ventas por profesional
     */
    fun searchSalesByProfessional(professionalName: String): List<Sale> {
        return _sales.value.filter { sale ->
            sale.professionalName.contains(professionalName, ignoreCase = true)
        }
    }

    /**
     * Limpia el mensaje actual
     */
    fun clearMessage() {
        _message.value = ""
    }
}

data class SalesSummary(
    val totalSales: Double,
    val totalTransactions: Int,
    val averageSale: Double,
    val paidSales: Int
)

data class ProductSales(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val total: Double
)