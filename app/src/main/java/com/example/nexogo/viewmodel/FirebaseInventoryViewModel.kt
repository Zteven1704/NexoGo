package com.example.nexogo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.model.Product
import com.example.nexogo.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirebaseInventoryViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    init {
        // Escuchar cambios en tiempo real
        viewModelScope.launch {
            repository.listenToProducts().collect { productsList ->
                _products.value = productsList
            }
        }
    }

    /**
     * Carga todos los productos
     */
    fun loadProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getAllProducts()
                if (result.isSuccess) {
                    _products.value = result.getOrNull() ?: emptyList()
                    _message.value = "Productos cargados exitosamente"
                } else {
                    _message.value = "Error al cargar productos: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar productos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Agrega un nuevo producto
     */
    fun addProduct(product: Product) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addProduct(product)
                if (result.isSuccess) {
                    _message.value = "Producto agregado exitosamente"
                } else {
                    _message.value = "Error al agregar producto: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al agregar producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza un producto existente
     */
    fun updateProduct(product: Product) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addProduct(product)
                if (result.isSuccess) {
                    _message.value = "Producto actualizado exitosamente"
                } else {
                    _message.value = "Error al actualizar producto: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al actualizar producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina un producto
     */
    fun deleteProduct(productId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.deleteProduct(productId)
                _message.value = "Producto eliminado exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al eliminar producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Obtiene un producto por ID
     */
    fun getProductById(productId: String): Product? {
        return _products.value.find { it.id == productId }
    }

    /**
     * Busca productos
     */
    fun searchProducts(query: String): List<Product> {
        return _products.value.filter { product ->
            product.name.contains(query, ignoreCase = true) ||
            product.description.contains(query, ignoreCase = true) ||
            product.category.contains(query, ignoreCase = true)
        }
    }

    /**
     * Obtiene productos con stock bajo
     */
    fun getLowStockProducts(): List<Product> {
        return _products.value.filter { product ->
            product.currentStock <= product.minStock
        }
    }

    /**
     * Limpia el mensaje actual
     */
    fun clearMessage() {
        _message.value = ""
    }
}