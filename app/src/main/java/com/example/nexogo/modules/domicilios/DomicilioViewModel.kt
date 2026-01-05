package com.example.nexogo.modules.domicilios

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.model.*
import com.example.nexogo.repository.DomicilioRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para usuarios normales - Gestión de domicilios
 */
class DomicilioViewModel(
    private val repository: DomicilioRepository
) : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    
    private val _domicilios = MutableStateFlow<List<Domicilio>>(emptyList())
    val domicilios: StateFlow<List<Domicilio>> = _domicilios.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    init {
        loadDomicilios()
    }
    
    /**
     * Carga los domicilios del usuario actual
     */
    fun loadDomicilios() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val currentUser = auth.currentUser
                    ?: throw Exception("Usuario no autenticado")
                
                val result = repository.getDomiciliosByUsuario(currentUser.uid)
                result.fold(
                    onSuccess = { domicilios ->
                        _domicilios.value = domicilios
                        Log.d("DomicilioViewModel", "✅ Domicilios cargados: ${domicilios.size}")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al cargar domicilios"
                        Log.e("DomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("DomicilioViewModel", "❌ Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Crea una nueva solicitud de domicilio de productos
     */
    fun createDomicilioProducto(
        nombreUsuario: String,
        telefono: String,
        direccion: String,
        datosProducto: DatosProducto,
        fotoUri: Uri?,
        usuarioId: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _successMessage.value = null
                
                val currentUser = auth.currentUser
                    ?: throw Exception("Usuario no autenticado")
                
                val domicilio = Domicilio(
                    domicilioId = "", // Se generará en Firestore
                    usuarioId = usuarioId ?: currentUser.uid,
                    nombreUsuario = nombreUsuario,
                    telefono = telefono,
                    direccion = direccion,
                    tipoSolicitud = TipoSolicitudDomicilio.PRODUCTO,
                    datosProducto = datosProducto,
                    estado = DomicilioEstado.PENDIENTE
                )
                
                val result = repository.createDomicilio(domicilio, fotoUri)
                result.fold(
                    onSuccess = {
                        _successMessage.value = "Solicitud de productos creada exitosamente"
                        loadDomicilios() // Recargar lista
                        Log.d("DomicilioViewModel", "✅ Domicilio producto creado: $it")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al crear solicitud"
                        Log.e("DomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("DomicilioViewModel", "❌ Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Crea una nueva solicitud de domicilio de servicios veterinarios
     */
    fun createDomicilioServicio(
        nombreUsuario: String,
        telefono: String,
        direccion: String,
        datosServicio: DatosServicio,
        fotoUri: Uri?,
        usuarioId: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _successMessage.value = null
                
                val currentUser = auth.currentUser
                    ?: throw Exception("Usuario no autenticado")
                
                val domicilio = Domicilio(
                    domicilioId = "", // Se generará en Firestore
                    usuarioId = usuarioId ?: currentUser.uid,
                    nombreUsuario = nombreUsuario,
                    telefono = telefono,
                    direccion = direccion,
                    tipoSolicitud = TipoSolicitudDomicilio.SERVICIO,
                    datosServicio = datosServicio,
                    estado = DomicilioEstado.PENDIENTE
                )
                
                val result = repository.createDomicilio(domicilio, fotoUri)
                result.fold(
                    onSuccess = {
                        _successMessage.value = "Solicitud de servicio creada exitosamente"
                        loadDomicilios() // Recargar lista
                        Log.d("DomicilioViewModel", "✅ Domicilio servicio creado: $it")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al crear solicitud"
                        Log.e("DomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("DomicilioViewModel", "❌ Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Crea una nueva solicitud de domicilio (método legacy - mantener para compatibilidad)
     */
    @Deprecated("Usar createDomicilioProducto o createDomicilioServicio")
    fun createDomicilio(
        nombreUsuario: String,
        telefono: String,
        direccion: String,
        tipoServicio: String,
        fechaSolicitada: Timestamp,
        fotoUri: Uri?
    ) {
        // Convertir a formato nuevo
        val datosServicio = DatosServicio(
            tipoServicio = TipoServicioVeterinario.CONSULTA_GENERAL,
            descripcion = tipoServicio,
            fechaSolicitada = fechaSolicitada
        )
        createDomicilioServicio(nombreUsuario, telefono, direccion, datosServicio, fotoUri)
    }
    
    /**
     * Cancela un domicilio (solo si está pendiente)
     */
    fun cancelarDomicilio(domicilioId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _successMessage.value = null
                
                val currentUser = auth.currentUser
                    ?: throw Exception("Usuario no autenticado")
                
                val result = repository.cancelarDomicilio(domicilioId, currentUser.uid)
                result.fold(
                    onSuccess = {
                        _successMessage.value = "Solicitud cancelada exitosamente"
                        loadDomicilios() // Recargar lista
                        Log.d("DomicilioViewModel", "✅ Domicilio cancelado: $domicilioId")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al cancelar solicitud"
                        Log.e("DomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("DomicilioViewModel", "❌ Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Limpia mensajes de error y éxito
     */
    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}

