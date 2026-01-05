package com.example.nexogo.modules.inventory.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.example.nexogo.modules.inventory.data.InventoryRepository
import com.example.nexogo.modules.inventory.model.Product
import com.example.nexogo.modules.inventory.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el módulo de inventario
 * Maneja el estado de la UI y las operaciones CRUD
 */
class InventoryViewModel(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {
    companion object {
        private const val TAG = "NEXOGO_INVENTORY_VM"
    }

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    /**
     * Carga todos los productos
     */
    fun loadProducts() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando productos")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                inventoryRepository.getAllProducts().collect { products ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        isLoading = false
                    )
                    Log.d(TAG, "Productos cargados: ${products.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando productos: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error cargando productos: ${e.message}"
                )
            }
        }
    }

    /**
     * Carga las categorías
     */
    fun loadCategories() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando categorías")
                inventoryRepository.getAllCategories().collect { categories ->
                    _uiState.value = _uiState.value.copy(categories = categories)
                    Log.d(TAG, "Categorías cargadas: ${categories.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando categorías: ${e.message}")
            }
        }
    }

    /**
     * Carga un producto específico
     */
    fun loadProduct(productId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando producto: $productId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val product = inventoryRepository.getProductById(productId)
                _uiState.value = _uiState.value.copy(
                    currentProduct = product,
                    isLoading = false
                )
                Log.d(TAG, "Producto cargado: ${product?.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando producto: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error cargando producto: ${e.message}"
                )
            }
        }
    }

    /**
     * Crea un nuevo producto
     */
    fun createProduct(product: Product) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creando producto: ${product.name}")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = inventoryRepository.createProduct(product)
                if (result.isSuccess) {
                    Log.d(TAG, "Producto creado exitosamente")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    // Recargar la lista de productos
                    loadProducts()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "Error creando producto: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error creando producto: $error"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creando producto: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error creando producto: ${e.message}"
                )
            }
        }
    }

    /**
     * Actualiza un producto existente
     */
    fun updateProduct(productId: String, product: Product) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Actualizando producto: $productId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = inventoryRepository.updateProduct(productId, product)
                if (result.isSuccess) {
                    Log.d(TAG, "Producto actualizado exitosamente")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    // Recargar la lista de productos
                    loadProducts()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "Error actualizando producto: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error actualizando producto: $error"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando producto: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error actualizando producto: ${e.message}"
                )
            }
        }
    }

    /**
     * Elimina un producto
     */
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Eliminando producto: $productId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = inventoryRepository.deleteProduct(productId)
                if (result.isSuccess) {
                    Log.d(TAG, "Producto eliminado exitosamente")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    // Recargar la lista de productos
                    loadProducts()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "Error eliminando producto: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error eliminando producto: $error"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando producto: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error eliminando producto: ${e.message}"
                )
            }
        }
    }

    /**
     * Sube una imagen de producto
     */
    fun uploadProductImage(productId: String, imageUri: Uri) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Subiendo imagen para producto: $productId")
                _uiState.value = _uiState.value.copy(isUploading = true, error = null)
                
                val result = inventoryRepository.uploadProductImage(productId, imageUri) { progress ->
                    _uiState.value = _uiState.value.copy(uploadProgress = progress)
                }
                
                if (result.isSuccess) {
                    val imageUrl = result.getOrNull() ?: ""
                    Log.d(TAG, "Imagen subida exitosamente: $imageUrl")
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        uploadProgress = 0f,
                        error = null
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "Error subiendo imagen: $error")
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        uploadProgress = 0f,
                        error = "Error subiendo imagen: $error"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error subiendo imagen: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    uploadProgress = 0f,
                    error = "Error subiendo imagen: ${e.message}"
                )
            }
        }
    }

    /**
     * Reduce el stock de un producto
     */
    fun reduceStock(productId: String, amount: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Reduciendo stock del producto $productId en $amount unidades")
                val result = inventoryRepository.reduceStock(productId, amount)
                if (result.isSuccess) {
                    Log.d(TAG, "Stock reducido exitosamente")
                    // Recargar la lista de productos
                    loadProducts()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "Error reduciendo stock: $error")
                    _uiState.value = _uiState.value.copy(
                        error = "Error reduciendo stock: $error"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reduciendo stock: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Error reduciendo stock: ${e.message}"
                )
            }
        }
    }

    /**
     * Filtra productos por categoría
     */
    fun filterByCategory(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    /**
     * Busca productos por nombre
     */
    fun searchProducts(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /**
     * Filtra productos con stock bajo
     */
    fun filterLowStock(showLowStock: Boolean) {
        _uiState.value = _uiState.value.copy(showLowStockOnly = showLowStock)
    }

    /**
     * Limpia el error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * Estado de la UI del inventario
 */
data class InventoryUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val currentProduct: Product? = null,
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val error: String? = null,
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val showLowStockOnly: Boolean = false
) {
    val filteredProducts: List<Product>
        get() {
            var filtered = products
            
            // Filtrar por categoría
            selectedCategoryId?.let { categoryId ->
                filtered = filtered.filter { it.categoryId == categoryId }
            }
            
            // Filtrar por búsqueda
            if (searchQuery.isNotEmpty()) {
                filtered = filtered.filter { 
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
                }
            }
            
            // Filtrar por stock bajo
            if (showLowStockOnly) {
                filtered = filtered.filter { it.isLowStock }
            }
            
            return filtered
        }
}
