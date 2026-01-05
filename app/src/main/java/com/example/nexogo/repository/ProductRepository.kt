package com.example.nexogo.repository

import com.example.nexogo.model.Product
import com.example.nexogo.model.ProductCategory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ProductRepository {
    suspend fun createProduct(product: Product): Result<String>
    suspend fun getProductById(productId: String): Result<Product>
    suspend fun getAllProducts(): Result<List<Product>>
    suspend fun getProductsByCategory(category: ProductCategory): Result<List<Product>>
    suspend fun getLowStockProducts(): Result<List<Product>>
    suspend fun updateProduct(product: Product): Result<Unit>
    suspend fun deleteProduct(productId: String): Result<Unit>
    suspend fun searchProducts(query: String): Result<List<Product>>
    suspend fun updateStock(productId: String, newQuantity: Int): Result<Unit>
}

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProductRepository {

    private val collection = firestore.collection("products")

    override suspend fun createProduct(product: Product): Result<String> {
        return try {
            val docRef = collection.document()
            val productWithId = product.copy(id = docRef.id)
            docRef.set(productWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductById(productId: String): Result<Product> {
        return try {
            val document = collection.document(productId).get().await()
            val product = document.toObject(Product::class.java)
            product?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Product not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllProducts(): Result<List<Product>> {
        return try {
            val snapshot = collection
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .get()
                .await()
            
            val products = snapshot.documents.mapNotNull { 
                it.toObject(Product::class.java) 
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProductsByCategory(category: ProductCategory): Result<List<Product>> {
        return try {
            val snapshot = collection
                .whereEqualTo("category", category.name)
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .get()
                .await()
            
            val products = snapshot.documents.mapNotNull { 
                it.toObject(Product::class.java) 
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLowStockProducts(): Result<List<Product>> {
        return try {
            val snapshot = collection
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val products = snapshot.documents.mapNotNull { 
                it.toObject(Product::class.java) 
            }.filter { product ->
                product.currentStock <= product.minStock
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(product: Product): Result<Unit> {
        return try {
            collection.document(product.id).set(product).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            collection.document(productId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchProducts(query: String): Result<List<Product>> {
        return try {
            val snapshot = collection
                .whereEqualTo("isActive", true)
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .limit(20)
                .get()
                .await()
            
            val products = snapshot.documents.mapNotNull { 
                it.toObject(Product::class.java) 
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStock(productId: String, newQuantity: Int): Result<Unit> {
        return try {
            collection.document(productId)
                .update("quantity", newQuantity)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
