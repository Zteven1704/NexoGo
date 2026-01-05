package com.example.nexogo.modules.inventory.data

import android.net.Uri
import android.util.Log
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.modules.inventory.model.Product
import com.example.nexogo.modules.inventory.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Repositorio para operaciones del inventario
 * Utiliza el FirebaseRepository central para todas las operaciones
 */
class InventoryRepository(
    private val firebaseRepository: FirebaseRepository
) {
    companion object {
        private const val TAG = "NEXOGO_INVENTORY"
        private const val PRODUCTS_COLLECTION = "inventory"
        private const val CATEGORIES_COLLECTION = "categories"
        private const val STORAGE_PATH = "inventory"
    }

    /**
     * Obtiene todos los productos del inventario
     */
    suspend fun getAllProducts(): Flow<List<Product>> = flow {
        try {
            Log.d(TAG, "Obteniendo todos los productos")
            val result = firebaseRepository.getCollection(PRODUCTS_COLLECTION)
            
            if (result.isSuccess) {
                val dataList = result.getOrNull() ?: emptyList()
                val products = dataList.mapNotNull { data ->
                    try {
                        Product(
                            id = data["id"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            categoryId = data["categoryId"] as? String ?: "",
                            categoryName = data["categoryName"] as? String ?: "",
                            unitPrice = (data["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                            quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                            imageUrl = data["imageUrl"] as? String ?: "",
                            createdAt = data["createdAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                            updatedAt = data["updatedAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parseando producto: ${e.message}")
                        null
                    }
                }
                
                Log.d(TAG, "Productos obtenidos: ${products.size}")
                emit(products)
            } else {
                Log.e(TAG, "Error obteniendo productos: ${result.exceptionOrNull()?.message}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo productos: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * Obtiene un producto específico
     */
    suspend fun getProductById(productId: String): Product? {
        return try {
            Log.d(TAG, "Obteniendo producto: $productId")
            val result = firebaseRepository.getDocument(PRODUCTS_COLLECTION, productId)
            if (result.isSuccess) {
                val data = result.getOrNull()
                data?.let {
                    Product(
                        id = it["id"] as? String ?: productId,
                        name = it["name"] as? String ?: "",
                        description = it["description"] as? String ?: "",
                        categoryId = it["categoryId"] as? String ?: "",
                        categoryName = it["categoryName"] as? String ?: "",
                        unitPrice = (it["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                        quantity = (it["quantity"] as? Number)?.toInt() ?: 0,
                        imageUrl = it["imageUrl"] as? String ?: "",
                        createdAt = it["createdAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                        updatedAt = it["updatedAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now()
                    )
                }
            } else {
                Log.e(TAG, "Error obteniendo producto: ${result.exceptionOrNull()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo producto: ${e.message}")
            null
        }
    }

    /**
     * Crea un nuevo producto
     */
    suspend fun createProduct(product: Product): Result<String> {
        return try {
            Log.d(TAG, "Creando producto: ${product.name}")
            val productId = UUID.randomUUID().toString()
            val productData = mapOf(
                "id" to productId,
                "name" to product.name,
                "description" to product.description,
                "categoryId" to product.categoryId,
                "categoryName" to product.categoryName,
                "unitPrice" to product.unitPrice,
                "quantity" to product.quantity,
                "imageUrl" to product.imageUrl,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            
            val result = firebaseRepository.createDocument(PRODUCTS_COLLECTION, productId, productData)
            if (result.isSuccess) {
                Log.d(TAG, "Producto creado exitosamente: $productId")
                Result.success(productId)
            } else {
                Log.e(TAG, "Error creando producto: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creando producto: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Actualiza un producto existente
     */
    suspend fun updateProduct(productId: String, product: Product): Result<Unit> {
        return try {
            Log.d(TAG, "Actualizando producto: $productId")
            val updateData = mapOf(
                "name" to product.name,
                "description" to product.description,
                "categoryId" to product.categoryId,
                "categoryName" to product.categoryName,
                "unitPrice" to product.unitPrice,
                "quantity" to product.quantity,
                "imageUrl" to product.imageUrl,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            
            val result = firebaseRepository.updateDocument(PRODUCTS_COLLECTION, productId, updateData)
            if (result.isSuccess) {
                Log.d(TAG, "Producto actualizado exitosamente: $productId")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Error actualizando producto: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando producto: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Elimina un producto
     */
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Eliminando producto: $productId")
            val result = firebaseRepository.deleteDocument(PRODUCTS_COLLECTION, productId)
            if (result.isSuccess) {
                Log.d(TAG, "Producto eliminado exitosamente: $productId")
                // TODO: Eliminar imagen del Storage si existe
                Result.success(Unit)
            } else {
                Log.e(TAG, "Error eliminando producto: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando producto: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sube una imagen de producto al Storage
     */
    suspend fun uploadProductImage(
        productId: String,
        imageUri: Uri,
        onProgress: (Float) -> Unit = {}
    ): Result<String> {
        return try {
            Log.d(TAG, "Subiendo imagen para producto: $productId")
            val storagePath = "$STORAGE_PATH/$productId/main.jpg"
            val result = firebaseRepository.uploadFile(storagePath, imageUri)
            if (result.isSuccess) {
                val downloadUrl = result.getOrNull() ?: ""
                Log.d(TAG, "Imagen subida exitosamente: $downloadUrl")
                Result.success(downloadUrl)
            } else {
                Log.e(TAG, "Error subiendo imagen: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error subiendo imagen: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Reduce el stock de un producto (usado por el módulo de Ventas)
     */
    suspend fun reduceStock(productId: String, amount: Int): Result<Unit> {
        return try {
            Log.d(TAG, "Reduciendo stock del producto $productId en $amount unidades")
            val product = getProductById(productId)
            if (product != null) {
                val newQuantity = maxOf(0, product.quantity - amount)
                val updatedProduct = product.copy(quantity = newQuantity)
                updateProduct(productId, updatedProduct)
            } else {
                Log.e(TAG, "Producto no encontrado: $productId")
                Result.failure(Exception("Producto no encontrado"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reduciendo stock: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Obtiene productos con stock bajo
     */
    suspend fun getLowStockProducts(threshold: Int = 10): Flow<List<Product>> = flow {
        try {
            Log.d(TAG, "Obteniendo productos con stock bajo (threshold: $threshold)")
            getAllProducts().collect { products ->
                val lowStockProducts = products.filter { it.quantity <= threshold }
                Log.d(TAG, "Productos con stock bajo encontrados: ${lowStockProducts.size}")
                emit(lowStockProducts)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo productos con stock bajo: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * Obtiene productos por categoría
     */
    suspend fun getProductsByCategory(categoryId: String): Flow<List<Product>> = flow {
        try {
            Log.d(TAG, "Obteniendo productos de la categoría: $categoryId")
            getAllProducts().collect { products ->
                val categoryProducts = products.filter { it.categoryId == categoryId }
                Log.d(TAG, "Productos de la categoría encontrados: ${categoryProducts.size}")
                emit(categoryProducts)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo productos por categoría: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * Obtiene todas las categorías
     */
    suspend fun getAllCategories(): Flow<List<Category>> = flow {
        try {
            Log.d(TAG, "Obteniendo todas las categorías")
            val result = firebaseRepository.getCollection(CATEGORIES_COLLECTION)
            
            if (result.isSuccess) {
                val dataList = result.getOrNull() ?: emptyList()
                val categories = dataList.mapNotNull { data ->
                    try {
                        Category(
                            id = data["id"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            isActive = data["isActive"] as? Boolean ?: true,
                            productCount = (data["productCount"] as? Number)?.toInt() ?: 0
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parseando categoría: ${e.message}")
                        null
                    }
                }
                
                Log.d(TAG, "Categorías obtenidas: ${categories.size}")
                emit(categories)
            } else {
                Log.e(TAG, "Error obteniendo categorías: ${result.exceptionOrNull()?.message}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo categorías: ${e.message}")
            emit(emptyList())
        }
    }
}

