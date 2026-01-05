package com.example.nexogo.modules.sales.data

import android.util.Log
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.modules.sales.model.Sale
import com.example.nexogo.modules.sales.model.Service
import com.example.nexogo.modules.sales.model.ServiceCategory
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class SalesRepository(
    private val firebaseRepository: FirebaseRepository
) {
    companion object {
        private const val TAG = "NEXOGO_SALES_REPO"
        private const val SALES_COLLECTION = "sales"
        private const val SERVICES_COLLECTION = "services"
        private const val SERVICE_CATEGORIES_COLLECTION = "service_categories"
    }

    // --- Sales CRUD Operations ---

    fun getAllSales(): Flow<List<Sale>> = flow {
        try {
            Log.d(TAG, "Obteniendo todas las ventas")
            val result = firebaseRepository.getCollection(SALES_COLLECTION)
            if (result.isSuccess) {
                val sales = result.getOrNull()?.mapNotNull { data ->
                        try {
                            Sale(
                                id = data["id"] as? String ?: "",
                                saleNumber = data["saleNumber"] as? String ?: "",
                                clientId = data["clientId"] as? String ?: "",
                                clientName = data["clientName"] as? String ?: "",
                                clientPhone = data["clientPhone"] as? String ?: "",
                                clientEmail = data["clientEmail"] as? String ?: "",
                                items = parseSaleItems(data["items"] as? List<Map<String, Any>>),
                                subtotal = (data["subtotal"] as? Number)?.toDouble() ?: 0.0,
                                tax = (data["tax"] as? Number)?.toDouble() ?: 0.0,
                                total = (data["total"] as? Number)?.toDouble() ?: 0.0,
                                paymentMethod = parsePaymentMethod(data["paymentMethod"] as? String),
                                status = parseSaleStatus(data["status"] as? String),
                                notes = data["notes"] as? String ?: "",
                                createdBy = data["createdBy"] as? String ?: "",
                                createdByName = data["createdByName"] as? String ?: "",
                                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                                updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now(),
                                pdfUrl = data["pdfUrl"] as? String ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parseando venta: ${e.message}")
                            null
                        }
                    } ?: emptyList()
                    emit(sales)
            } else {
                Log.e(TAG, "Error obteniendo ventas: ${result.exceptionOrNull()?.message}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo ventas: ${e.message}")
            emit(emptyList())
        }
    }

    suspend fun getSaleById(saleId: String): Sale? {
        return try {
            Log.d(TAG, "Obteniendo venta: $saleId")
            val result = firebaseRepository.getDocument(SALES_COLLECTION, saleId)
            if (result.isSuccess) {
                val data = result.getOrNull()
                data?.let {
                    Sale(
                        id = it["id"] as? String ?: saleId,
                        saleNumber = it["saleNumber"] as? String ?: "",
                        clientId = it["clientId"] as? String ?: "",
                        clientName = it["clientName"] as? String ?: "",
                        clientPhone = it["clientPhone"] as? String ?: "",
                        clientEmail = it["clientEmail"] as? String ?: "",
                        items = parseSaleItems(it["items"] as? List<Map<String, Any>>),
                        subtotal = (it["subtotal"] as? Number)?.toDouble() ?: 0.0,
                        tax = (it["tax"] as? Number)?.toDouble() ?: 0.0,
                        total = (it["total"] as? Number)?.toDouble() ?: 0.0,
                        paymentMethod = parsePaymentMethod(it["paymentMethod"] as? String),
                        status = parseSaleStatus(it["status"] as? String),
                        notes = it["notes"] as? String ?: "",
                        createdBy = it["createdBy"] as? String ?: "",
                        createdByName = it["createdByName"] as? String ?: "",
                        createdAt = it["createdAt"] as? Timestamp ?: Timestamp.now(),
                        updatedAt = it["updatedAt"] as? Timestamp ?: Timestamp.now(),
                        pdfUrl = it["pdfUrl"] as? String ?: ""
                    )
                }
            } else {
                Log.e(TAG, "Error obteniendo venta: ${result.exceptionOrNull()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo venta: ${e.message}")
            null
        }
    }

    suspend fun createSale(sale: Sale): Result<String> {
        return try {
            Log.d(TAG, "Creando venta: ${sale.saleNumber}")
            val saleId = UUID.randomUUID().toString()
            val saleData = mapOf(
                "id" to saleId,
                "saleNumber" to sale.saleNumber,
                "clientId" to sale.clientId,
                "clientName" to sale.clientName,
                "clientPhone" to sale.clientPhone,
                "clientEmail" to sale.clientEmail,
                "items" to mapSaleItems(sale.items),
                "subtotal" to sale.subtotal,
                "tax" to sale.tax,
                "total" to sale.total,
                "paymentMethod" to sale.paymentMethod.name,
                "status" to sale.status.name,
                "notes" to sale.notes,
                "createdBy" to sale.createdBy,
                "createdByName" to sale.createdByName,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now(),
                "pdfUrl" to sale.pdfUrl
            )

            val result = firebaseRepository.createDocument(SALES_COLLECTION, saleId, saleData)
            if (result.isSuccess) {
                Log.d(TAG, "Venta creada exitosamente: $saleId")
                Result.success(saleId)
            } else {
                Log.e(TAG, "Error creando venta: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creando venta: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateSale(saleId: String, sale: Sale): Result<Unit> {
        return try {
            Log.d(TAG, "Actualizando venta: $saleId")
            val updateData = mapOf(
                "clientName" to sale.clientName,
                "clientPhone" to sale.clientPhone,
                "clientEmail" to sale.clientEmail,
                "items" to mapSaleItems(sale.items),
                "subtotal" to sale.subtotal,
                "tax" to sale.tax,
                "total" to sale.total,
                "paymentMethod" to sale.paymentMethod.name,
                "status" to sale.status.name,
                "notes" to sale.notes,
                "updatedAt" to Timestamp.now(),
                "pdfUrl" to sale.pdfUrl
            )

            val result = firebaseRepository.updateDocument(SALES_COLLECTION, saleId, updateData)
            if (result.isSuccess) {
                Log.d(TAG, "Venta actualizada exitosamente: $saleId")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Error actualizando venta: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando venta: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteSale(saleId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Eliminando venta: $saleId")
            val result = firebaseRepository.deleteDocument(SALES_COLLECTION, saleId)
            if (result.isSuccess) {
                Log.d(TAG, "Venta eliminada exitosamente: $saleId")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Error eliminando venta: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando venta: ${e.message}")
            Result.failure(e)
        }
    }

    // --- Services CRUD Operations ---

    fun getAllServices(): Flow<List<Service>> = flow {
        try {
            Log.d(TAG, "Obteniendo todos los servicios")
            val result = firebaseRepository.getCollection(SERVICES_COLLECTION)
            if (result.isSuccess) {
                val services = result.getOrNull()?.mapNotNull { data ->
                        try {
                            Service(
                                id = data["id"] as? String ?: "",
                                name = data["name"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                category = data["category"] as? String ?: "",
                                price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                                duration = (data["duration"] as? Number)?.toInt() ?: 0,
                                isActive = data["isActive"] as? Boolean ?: true,
                                createdBy = data["createdBy"] as? String ?: "",
                                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                                updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now()
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parseando servicio: ${e.message}")
                            null
                        }
                    } ?: emptyList()
                    emit(services)
            } else {
                Log.e(TAG, "Error obteniendo servicios: ${result.exceptionOrNull()?.message}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo servicios: ${e.message}")
            emit(emptyList())
        }
    }

    suspend fun createService(service: Service): Result<String> {
        return try {
            Log.d(TAG, "Creando servicio: ${service.name}")
            val serviceId = UUID.randomUUID().toString()
            val serviceData = mapOf(
                "id" to serviceId,
                "name" to service.name,
                "description" to service.description,
                "category" to service.category,
                "price" to service.price,
                "duration" to service.duration,
                "isActive" to service.isActive,
                "createdBy" to service.createdBy,
                "createdAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            val result = firebaseRepository.createDocument(SERVICES_COLLECTION, serviceId, serviceData)
            if (result.isSuccess) {
                Log.d(TAG, "Servicio creado exitosamente: $serviceId")
                Result.success(serviceId)
            } else {
                Log.e(TAG, "Error creando servicio: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creando servicio: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateService(serviceId: String, service: Service): Result<Unit> {
        return try {
            Log.d(TAG, "Actualizando servicio: $serviceId")
            val updateData = mapOf(
                "name" to service.name,
                "description" to service.description,
                "category" to service.category,
                "price" to service.price,
                "duration" to service.duration,
                "isActive" to service.isActive,
                "updatedAt" to Timestamp.now()
            )

            val result = firebaseRepository.updateDocument(SERVICES_COLLECTION, serviceId, updateData)
            if (result.isSuccess) {
                Log.d(TAG, "Servicio actualizado exitosamente: $serviceId")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Error actualizando servicio: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error actualizando servicio: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteService(serviceId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Eliminando servicio: $serviceId")
            val result = firebaseRepository.deleteDocument(SERVICES_COLLECTION, serviceId)
            if (result.isSuccess) {
                Log.d(TAG, "Servicio eliminado exitosamente: $serviceId")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Error eliminando servicio: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error eliminando servicio: ${e.message}")
            Result.failure(e)
        }
    }

    // --- Service Categories ---

    fun getAllServiceCategories(): Flow<List<ServiceCategory>> = flow {
        try {
            Log.d(TAG, "Obteniendo categorías de servicios")
            val result = firebaseRepository.getCollection(SERVICE_CATEGORIES_COLLECTION)
            if (result.isSuccess) {
                val categories = result.getOrNull()?.mapNotNull { data ->
                        try {
                            ServiceCategory(
                                id = data["id"] as? String ?: "",
                                name = data["name"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                isActive = data["isActive"] as? Boolean ?: true,
                                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now()
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parseando categoría: ${e.message}")
                            null
                        }
                    } ?: emptyList()
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

    suspend fun createServiceCategory(category: ServiceCategory): Result<String> {
        return try {
            Log.d(TAG, "Creando categoría: ${category.name}")
            val categoryId = UUID.randomUUID().toString()
            val categoryData = mapOf(
                "id" to categoryId,
                "name" to category.name,
                "description" to category.description,
                "isActive" to category.isActive,
                "createdAt" to Timestamp.now()
            )

            val result = firebaseRepository.createDocument(SERVICE_CATEGORIES_COLLECTION, categoryId, categoryData)
            if (result.isSuccess) {
                Log.d(TAG, "Categoría creada exitosamente: $categoryId")
                Result.success(categoryId)
            } else {
                Log.e(TAG, "Error creando categoría: ${result.exceptionOrNull()?.message}")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creando categoría: ${e.message}")
            Result.failure(e)
        }
    }

    // --- Helper Methods ---

    private fun parseSaleItems(data: List<Map<String, Any>>?): List<com.example.nexogo.modules.sales.model.SaleItem> {
        if (data == null) return emptyList()
        return data.mapNotNull { item ->
            try {
                com.example.nexogo.modules.sales.model.SaleItem(
                    id = item["id"] as? String ?: "",
                    type = parseSaleItemType(item["type"] as? String),
                    itemId = item["itemId"] as? String ?: "",
                    itemName = item["itemName"] as? String ?: "",
                    description = item["description"] as? String ?: "",
                    quantity = (item["quantity"] as? Number)?.toInt() ?: 1,
                    unitPrice = (item["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                    totalPrice = (item["totalPrice"] as? Number)?.toDouble() ?: 0.0
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error parseando item de venta: ${e.message}")
                null
            }
        }
    }

    private fun mapSaleItems(items: List<com.example.nexogo.modules.sales.model.SaleItem>): List<Map<String, Any>> {
        return items.map { item ->
            mapOf(
                "id" to item.id,
                "type" to item.type.name,
                "itemId" to item.itemId,
                "itemName" to item.itemName,
                "description" to item.description,
                "quantity" to item.quantity,
                "unitPrice" to item.unitPrice,
                "totalPrice" to item.totalPrice
            )
        }
    }

    private fun parseSaleItemType(type: String?): com.example.nexogo.modules.sales.model.SaleItemType {
        return try {
            com.example.nexogo.modules.sales.model.SaleItemType.valueOf(type ?: "PRODUCT")
        } catch (e: Exception) {
            com.example.nexogo.modules.sales.model.SaleItemType.PRODUCT
        }
    }

    private fun parsePaymentMethod(method: String?): com.example.nexogo.modules.sales.model.PaymentMethod {
        return try {
            com.example.nexogo.modules.sales.model.PaymentMethod.valueOf(method ?: "CASH")
        } catch (e: Exception) {
            com.example.nexogo.modules.sales.model.PaymentMethod.CASH
        }
    }

    private fun parseSaleStatus(status: String?): com.example.nexogo.modules.sales.model.SaleStatus {
        return try {
            com.example.nexogo.modules.sales.model.SaleStatus.valueOf(status ?: "COMPLETED")
        } catch (e: Exception) {
            com.example.nexogo.modules.sales.model.SaleStatus.COMPLETED
        }
    }
}
