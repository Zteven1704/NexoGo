package com.example.nexogo.modules.inventory

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.core.models.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.google.firebase.Timestamp

/**
 * ViewModel para gestión de inventario
 */
class InventoryViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts: StateFlow<List<Product>> = _filteredProducts.asStateFlow()
    
    private val _lowStockProducts = MutableStateFlow<List<Product>>(emptyList())
    val lowStockProducts: StateFlow<List<Product>> = _lowStockProducts.asStateFlow()
    
    init {
        loadProducts()
    }
    
    fun loadProducts() {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val result = repository.getCollection("inventory")
                if (result.isSuccess) {
                    val productsData = result.getOrNull() ?: emptyList()
                    val products = productsData.mapNotNull { data ->
                        try {
                            Product(
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
                            Log.e("NEXOGO_INVENTORY", "Error parseando producto: ${e.message}")
                            null
                        }
                    }
                    _products.value = products
                    _filteredProducts.value = products
                    updateLowStockProducts(products)
                    _message.value = "Productos cargados exitosamente"
                    Log.d("NEXOGO_INVENTORY", "Productos cargados: ${products.size}")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_INVENTORY", "Error cargando productos: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_INVENTORY", "Excepción cargando productos: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createProduct(
        name: String,
        description: String,
        category: String,
        quantity: Int,
        unitPrice: Double,
        lowStockThreshold: Int,
        imageUri: Uri? = null
    ) {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val productId = repository.generateId()
                var imageUrl = ""
                
                // Subir imagen si se proporciona
                if (imageUri != null) {
                    val imagePath = "products/$productId/uploads/product_${System.currentTimeMillis()}.jpg"
                    val uploadResult = repository.uploadFile(imagePath, imageUri)
                    if (uploadResult.isSuccess) {
                        imageUrl = uploadResult.getOrNull() ?: ""
                        Log.d("NEXOGO_INVENTORY", "Imagen de producto subida: $imageUrl")
                    } else {
                        _message.value = "Error subiendo imagen: ${uploadResult.exceptionOrNull()?.message}"
                        Log.e("NEXOGO_INVENTORY", "Error subiendo imagen: ${uploadResult.exceptionOrNull()?.message}")
                        return@launch
                    }
                }
                
                val totalPrice = quantity * unitPrice
                val product = Product(
                    id = productId,
                    name = name,
                    description = description,
                    category = category,
                    quantity = quantity,
                    unitPrice = unitPrice,
                    totalPrice = totalPrice,
                    imageUrl = imageUrl,
                    lowStockThreshold = lowStockThreshold,
                    isActive = true
                )
                
                val productData = mapOf(
                    "id" to product.id,
                    "name" to product.name,
                    "description" to product.description,
                    "category" to product.category,
                    "quantity" to product.quantity,
                    "unitPrice" to product.unitPrice,
                    "totalPrice" to product.totalPrice,
                    "imageUrl" to product.imageUrl,
                    "lowStockThreshold" to product.lowStockThreshold,
                    "isActive" to product.isActive,
                    "createdAt" to repository.getCurrentTimestamp(),
                    "updatedAt" to repository.getCurrentTimestamp()
                )
                
                val result = repository.createDocument("inventory", productId, productData)
                if (result.isSuccess) {
                    _message.value = "Producto creado exitosamente"
                    Log.d("NEXOGO_INVENTORY", "Producto creado: $productId")
                    // Recargar datos desde Firebase para asegurar sincronización
                    loadProducts()
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_INVENTORY", "Error creando producto: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_INVENTORY", "Excepción creando producto: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateProduct(
        productId: String,
        name: String,
        description: String,
        category: String,
        quantity: Int,
        unitPrice: Double,
        lowStockThreshold: Int,
        imageUri: Uri? = null
    ) {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                var imageUrl = _products.value.find { it.id == productId }?.imageUrl ?: ""
                
                // Subir nueva imagen si se proporciona
                if (imageUri != null) {
                    val imagePath = "products/$productId/uploads/product_${System.currentTimeMillis()}.jpg"
                    val uploadResult = repository.uploadFile(imagePath, imageUri)
                    if (uploadResult.isSuccess) {
                        imageUrl = uploadResult.getOrNull() ?: ""
                        Log.d("NEXOGO_INVENTORY", "Imagen de producto actualizada: $imageUrl")
                    } else {
                        _message.value = "Error subiendo imagen: ${uploadResult.exceptionOrNull()?.message}"
                        Log.e("NEXOGO_INVENTORY", "Error subiendo imagen: ${uploadResult.exceptionOrNull()?.message}")
                        return@launch
                    }
                }
                
                val totalPrice = quantity * unitPrice
                val updateData = mapOf(
                    "name" to name,
                    "description" to description,
                    "category" to category,
                    "quantity" to quantity,
                    "unitPrice" to unitPrice,
                    "totalPrice" to totalPrice,
                    "imageUrl" to imageUrl,
                    "lowStockThreshold" to lowStockThreshold,
                    "updatedAt" to repository.getCurrentTimestamp()
                )
                
                val result = repository.updateDocument("inventory", productId, updateData)
                if (result.isSuccess) {
                    _message.value = "Producto actualizado exitosamente"
                    Log.d("NEXOGO_INVENTORY", "Producto actualizado: $productId")
                    // Recargar datos desde Firebase para asegurar sincronización
                    loadProducts()
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_INVENTORY", "Error actualizando producto: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_INVENTORY", "Excepción actualizando producto: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteDocument("inventory", productId)
                if (result.isSuccess) {
                    _message.value = "Producto eliminado exitosamente"
                    Log.d("NEXOGO_INVENTORY", "Producto eliminado: $productId")
                    // Recargar datos desde Firebase para asegurar sincronización
                    loadProducts()
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_INVENTORY", "Error eliminando producto: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_INVENTORY", "Excepción eliminando producto: ${e.message}")
            }
        }
    }
    
    fun updateStock(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                val updateData = mapOf(
                    "quantity" to newQuantity,
                    "totalPrice" to (newQuantity * (_products.value.find { it.id == productId }?.unitPrice ?: 0.0)),
                    "updatedAt" to repository.getCurrentTimestamp()
                )
                
                val result = repository.updateDocument("inventory", productId, updateData)
                if (result.isSuccess) {
                    _products.value = _products.value.map { product ->
                        if (product.id == productId) {
                            product.copy(
                                quantity = newQuantity,
                                totalPrice = newQuantity * product.unitPrice
                            )
                        } else {
                            product
                        }
                    }
                    _filteredProducts.value = _filteredProducts.value.map { product ->
                        if (product.id == productId) {
                            product.copy(
                                quantity = newQuantity,
                                totalPrice = newQuantity * product.unitPrice
                            )
                        } else {
                            product
                        }
                    }
                    updateLowStockProducts(_products.value)
                    _message.value = "Stock actualizado exitosamente"
                    Log.d("NEXOGO_INVENTORY", "Stock actualizado: $productId -> $newQuantity")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_INVENTORY", "Error actualizando stock: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_INVENTORY", "Excepción actualizando stock: ${e.message}")
            }
        }
    }
    
    fun searchProducts(query: String) {
        _searchQuery.value = query
        
        if (query.isEmpty() && _selectedCategory.value.isEmpty()) {
            _filteredProducts.value = _products.value
        } else {
            val filtered = _products.value.filter { product ->
                val matchesQuery = query.isEmpty() || 
                    product.name.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true)
                
                val matchesCategory = _selectedCategory.value.isEmpty() || 
                    product.category == _selectedCategory.value
                
                matchesQuery && matchesCategory
            }
            _filteredProducts.value = filtered
        }
    }
    
    fun filterByCategory(category: String) {
        _selectedCategory.value = category
        searchProducts(_searchQuery.value)
    }
    
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = ""
        _filteredProducts.value = _products.value
    }
    
    private fun updateLowStockProducts(products: List<Product>) {
        val lowStock = products.filter { product ->
            product.isActive && product.quantity <= product.lowStockThreshold
        }
        _lowStockProducts.value = lowStock
        
        if (lowStock.isNotEmpty()) {
            Log.w("NEXOGO_INVENTORY", "⚠️ Productos con stock bajo: ${lowStock.size}")
            lowStock.forEach { product ->
                Log.w("NEXOGO_INVENTORY", "⚠️ ${product.name}: ${product.quantity} unidades (umbral: ${product.lowStockThreshold})")
            }
        }
    }
    
    fun getCategories(): List<String> {
        return _products.value.map { it.category }.distinct().sorted()
    }
    
    fun getTotalValue(): Double {
        return _products.value.sumOf { it.totalPrice }
    }
    
    fun getTotalProducts(): Int {
        return _products.value.size
    }
    
    fun getActiveProducts(): Int {
        return _products.value.count { it.isActive }
    }
    
    fun clearMessage() {
        _message.value = ""
    }
}
