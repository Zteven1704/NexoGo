package com.example.nexogo.modules.sales.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.modules.sales.data.SalesRepository
import com.example.nexogo.modules.sales.model.Sale
import com.example.nexogo.modules.sales.model.SaleItem
import com.example.nexogo.modules.sales.model.Service
import com.example.nexogo.modules.sales.model.ServiceCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesViewModel(
    private val salesRepository: SalesRepository
) : ViewModel() {
    companion object {
        private const val TAG = "NEXOGO_SALES_VM"
    }

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    init {
        loadSales()
        loadServices()
        loadServiceCategories()
    }

    fun loadSales() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando ventas")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                salesRepository.getAllSales().collect { sales ->
                    _uiState.value = _uiState.value.copy(
                        sales = sales,
                        isLoading = false
                    )
                    Log.d(TAG, "Ventas cargadas: ${sales.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando ventas: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error cargando ventas: ${e.message}"
                )
            }
        }
    }

    fun loadServices() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando servicios")
                salesRepository.getAllServices().collect { services ->
                    _uiState.value = _uiState.value.copy(services = services)
                    Log.d(TAG, "Servicios cargados: ${services.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando servicios: ${e.message}")
            }
        }
    }

    fun loadServiceCategories() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando categorías de servicios")
                salesRepository.getAllServiceCategories().collect { categories ->
                    _uiState.value = _uiState.value.copy(serviceCategories = categories)
                    Log.d(TAG, "Categorías cargadas: ${categories.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando categorías: ${e.message}")
            }
        }
    }

    fun loadSale(saleId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando venta: $saleId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val sale = salesRepository.getSaleById(saleId)
                _uiState.value = _uiState.value.copy(
                    currentSale = sale,
                    isLoading = false
                )
                Log.d(TAG, "Venta cargada: ${sale?.saleNumber}")
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando venta: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error cargando venta: ${e.message}"
                )
            }
        }
    }

    fun createSale(sale: Sale) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creando venta: ${sale.saleNumber}")
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                
                val result = salesRepository.createSale(sale)
                if (result.isSuccess) {
                    Log.d(TAG, "Venta creada exitosamente")
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadSales() // Recargar la lista
                } else {
                    Log.e(TAG, "Error creando venta: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Error creando venta: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creando venta: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Error creando venta: ${e.message}"
                )
            }
        }
    }

    fun updateSale(saleId: String, sale: Sale) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Actualizando venta: $saleId")
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                
                val result = salesRepository.updateSale(saleId, sale)
                if (result.isSuccess) {
                    Log.d(TAG, "Venta actualizada exitosamente")
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadSales() // Recargar la lista
                } else {
                    Log.e(TAG, "Error actualizando venta: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Error actualizando venta: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando venta: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Error actualizando venta: ${e.message}"
                )
            }
        }
    }

    fun deleteSale(saleId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Eliminando venta: $saleId")
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                
                val result = salesRepository.deleteSale(saleId)
                if (result.isSuccess) {
                    Log.d(TAG, "Venta eliminada exitosamente")
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadSales() // Recargar la lista
                } else {
                    Log.e(TAG, "Error eliminando venta: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Error eliminando venta: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando venta: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Error eliminando venta: ${e.message}"
                )
            }
        }
    }

    fun createService(service: Service) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creando servicio: ${service.name}")
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                
                val result = salesRepository.createService(service)
                if (result.isSuccess) {
                    Log.d(TAG, "Servicio creado exitosamente")
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadServices() // Recargar la lista
                } else {
                    Log.e(TAG, "Error creando servicio: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Error creando servicio: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creando servicio: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Error creando servicio: ${e.message}"
                )
            }
        }
    }

    fun updateService(serviceId: String, service: Service) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Actualizando servicio: $serviceId")
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                
                val result = salesRepository.updateService(serviceId, service)
                if (result.isSuccess) {
                    Log.d(TAG, "Servicio actualizado exitosamente")
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadServices() // Recargar la lista
                } else {
                    Log.e(TAG, "Error actualizando servicio: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Error actualizando servicio: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando servicio: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Error actualizando servicio: ${e.message}"
                )
            }
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Eliminando servicio: $serviceId")
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                
                val result = salesRepository.deleteService(serviceId)
                if (result.isSuccess) {
                    Log.d(TAG, "Servicio eliminado exitosamente")
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadServices() // Recargar la lista
                } else {
                    Log.e(TAG, "Error eliminando servicio: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Error eliminando servicio: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando servicio: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Error eliminando servicio: ${e.message}"
                )
            }
        }
    }

    fun createServiceCategory(category: ServiceCategory) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creando categoría: ${category.name}")
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                
                val result = salesRepository.createServiceCategory(category)
                if (result.isSuccess) {
                    Log.d(TAG, "Categoría creada exitosamente")
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    loadServiceCategories() // Recargar la lista
                } else {
                    Log.e(TAG, "Error creando categoría: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Error creando categoría: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creando categoría: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Error creando categoría: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class SalesUiState(
    val sales: List<Sale> = emptyList(),
    val services: List<Service> = emptyList(),
    val serviceCategories: List<ServiceCategory> = emptyList(),
    val currentSale: Sale? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

