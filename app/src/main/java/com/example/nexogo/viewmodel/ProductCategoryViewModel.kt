package com.example.nexogo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.data.AppDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductCategoryViewModel(context: Context) : ViewModel() {

    private val appDataStore = AppDataStore(context)

    private val _categories = MutableStateFlow<List<ProductCategory>>(emptyList())
    val categories: StateFlow<List<ProductCategory>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            loadCategories()
        }
    }

    fun addCategory(name: String, description: String = "") {
        if (name.isBlank()) {
            _message.value = "El nombre de la categoría no puede estar vacío"
            return
        }

        if (_categories.value.any { it.name.equals(name, ignoreCase = true) }) {
            _message.value = "Ya existe una categoría con ese nombre"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val newCategory = ProductCategory(
                id = "cat_${System.currentTimeMillis()}",
                name = name.trim(),
                description = description.trim(),
                isActive = true
            )
            
            val currentCategories = _categories.value.toMutableList()
            currentCategories.add(newCategory)
            _categories.value = currentCategories
            appDataStore.saveProductCategories(currentCategories)
            _message.value = "Categoría '${newCategory.name}' agregada exitosamente"
            _isLoading.value = false
        }
    }

    fun updateCategory(categoryId: String, name: String, description: String = "") {
        if (name.isBlank()) {
            _message.value = "El nombre de la categoría no puede estar vacío"
            return
        }

        val existingCategory = _categories.value.find { it.id == categoryId }
        if (existingCategory == null) {
            _message.value = "Categoría no encontrada"
            return
        }

        if (_categories.value.any { it.id != categoryId && it.name.equals(name, ignoreCase = true) }) {
            _message.value = "Ya existe otra categoría con ese nombre"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val updatedCategory = existingCategory.copy(
                name = name.trim(),
                description = description.trim()
            )
            
            val currentCategories = _categories.value.toMutableList()
            val index = currentCategories.indexOfFirst { it.id == categoryId }
            if (index != -1) {
                currentCategories[index] = updatedCategory
                _categories.value = currentCategories
                appDataStore.saveProductCategories(currentCategories)
                _message.value = "Categoría '${updatedCategory.name}' actualizada exitosamente"
            } else {
                _message.value = "Error al actualizar la categoría"
            }
            _isLoading.value = false
        }
    }

    fun deleteCategory(categoryId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val categoryToDelete = _categories.value.find { it.id == categoryId }
            if (categoryToDelete == null) {
                _message.value = "Categoría no encontrada"
                _isLoading.value = false
                return@launch
            }

            val currentCategories = _categories.value.toMutableList()
            val removed = currentCategories.removeIf { it.id == categoryId }
            if (removed) {
                _categories.value = currentCategories
                appDataStore.saveProductCategories(currentCategories)
                _message.value = "Categoría '${categoryToDelete.name}' eliminada exitosamente"
            } else {
                _message.value = "Error al eliminar la categoría"
            }
            _isLoading.value = false
        }
    }

    fun toggleCategoryStatus(categoryId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val currentCategories = _categories.value.toMutableList()
            val index = currentCategories.indexOfFirst { it.id == categoryId }
            if (index != -1) {
                val category = currentCategories[index]
                currentCategories[index] = category.copy(isActive = !category.isActive)
                _categories.value = currentCategories
                appDataStore.saveProductCategories(currentCategories)
                val status = if (currentCategories[index].isActive) "activada" else "desactivada"
                _message.value = "Categoría '${category.name}' $status"
            } else {
                _message.value = "Categoría no encontrada"
            }
            _isLoading.value = false
        }
    }

    private suspend fun loadCategories() {
        _isLoading.value = true
        val loadedCategories = appDataStore.loadProductCategories()
        if (loadedCategories.isEmpty()) {
            // Add default categories if none exist
            addDefaultCategories()
        } else {
            _categories.value = loadedCategories
        }
        _isLoading.value = false
    }

    private suspend fun addDefaultCategories() {
        val defaultCategories = listOf(
            ProductCategory(
                id = "cat_medicine",
                name = "Medicamentos",
                description = "Medicamentos y fármacos veterinarios",
                isActive = true
            ),
            ProductCategory(
                id = "cat_vaccine",
                name = "Vacunas",
                description = "Vacunas para prevención de enfermedades",
                isActive = true
            ),
            ProductCategory(
                id = "cat_food",
                name = "Alimentos",
                description = "Alimentos y suplementos nutricionales",
                isActive = true
            ),
            ProductCategory(
                id = "cat_accessory",
                name = "Accesorios",
                description = "Accesorios y juguetes para mascotas",
                isActive = true
            ),
            ProductCategory(
                id = "cat_equipment",
                name = "Equipos",
                description = "Equipos médicos y herramientas",
                isActive = true
            ),
            ProductCategory(
                id = "cat_hygiene",
                name = "Higiene",
                description = "Productos de higiene y limpieza",
                isActive = true
            ),
            ProductCategory(
                id = "cat_medical_material",
                name = "Material Médico",
                description = "Materiales médicos desechables",
                isActive = true
            ),
            ProductCategory(
                id = "cat_supplement",
                name = "Suplementos",
                description = "Suplementos vitamínicos y nutricionales",
                isActive = true
            ),
            ProductCategory(
                id = "cat_tool",
                name = "Herramientas",
                description = "Herramientas y utensilios veterinarios",
                isActive = true
            ),
            ProductCategory(
                id = "cat_other",
                name = "Otros",
                description = "Otras categorías de productos",
                isActive = true
            )
        )
        _categories.value = defaultCategories
        appDataStore.saveProductCategories(defaultCategories)
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        @Volatile
        private var INSTANCE: ProductCategoryViewModel? = null

        fun getInstance(context: Context): ProductCategoryViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProductCategoryViewModel(context).also { INSTANCE = it }
            }
        }
    }
}

data class ProductCategory(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

