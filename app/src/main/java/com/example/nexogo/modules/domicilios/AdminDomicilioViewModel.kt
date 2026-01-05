package com.example.nexogo.modules.domicilios

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.model.*
import android.net.Uri
import com.example.nexogo.repository.DomicilioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para Administradores - Gestión de domicilios pendientes
 */
class AdminDomicilioViewModel(
    private val repository: DomicilioRepository
) : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    
    private val _domiciliosPendientes = MutableStateFlow<List<Domicilio>>(emptyList())
    val domiciliosPendientes: StateFlow<List<Domicilio>> = _domiciliosPendientes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    init {
        loadDomiciliosPendientes()
    }
    
    /**
     * Carga los domicilios pendientes
     */
    fun loadDomiciliosPendientes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val result = repository.getDomiciliosPendientes()
                result.fold(
                    onSuccess = { domicilios ->
                        _domiciliosPendientes.value = domicilios
                        Log.d("AdminDomicilioViewModel", "✅ Domicilios pendientes cargados: ${domicilios.size}")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al cargar domicilios"
                        Log.e("AdminDomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("AdminDomicilioViewModel", "❌ Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Aprueba un domicilio
     */
    fun aprobarDomicilio(
        domicilioId: String,
        veterinarioAsignadoId: String? = null,
        veterinarioAsignadoNombre: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _successMessage.value = null
                
                val currentUser = auth.currentUser
                    ?: throw Exception("Usuario no autenticado")
                
                val result = repository.updateEstadoDomicilio(
                    domicilioId = domicilioId,
                    nuevoEstado = DomicilioEstado.APROBADO,
                    usuarioAccion = currentUser.uid,
                    veterinarioAsignadoId = veterinarioAsignadoId,
                    veterinarioAsignadoNombre = veterinarioAsignadoNombre
                )
                
                result.fold(
                    onSuccess = {
                        _successMessage.value = "Solicitud aprobada exitosamente"
                        loadDomiciliosPendientes() // Recargar lista
                        Log.d("AdminDomicilioViewModel", "✅ Domicilio aprobado: $domicilioId")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al aprobar solicitud"
                        Log.e("AdminDomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("AdminDomicilioViewModel", "❌ Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Rechaza un domicilio
     */
    fun rechazarDomicilio(
        domicilioId: String,
        motivoRechazo: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _successMessage.value = null
                
                val currentUser = auth.currentUser
                    ?: throw Exception("Usuario no autenticado")
                
                val result = repository.updateEstadoDomicilio(
                    domicilioId = domicilioId,
                    nuevoEstado = DomicilioEstado.RECHAZADO,
                    usuarioAccion = currentUser.uid,
                    motivoRechazo = motivoRechazo
                )
                
                result.fold(
                    onSuccess = {
                        _successMessage.value = "Solicitud rechazada exitosamente"
                        loadDomiciliosPendientes() // Recargar lista
                        Log.d("AdminDomicilioViewModel", "✅ Domicilio rechazado: $domicilioId")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al rechazar solicitud"
                        Log.e("AdminDomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("AdminDomicilioViewModel", "❌ Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cambia el estado de un domicilio
     */
    fun cambiarEstado(
        domicilioId: String,
        nuevoEstado: DomicilioEstado
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _successMessage.value = null
                
                val currentUser = auth.currentUser
                    ?: throw Exception("Usuario no autenticado")
                
                val result = repository.updateEstadoDomicilio(
                    domicilioId = domicilioId,
                    nuevoEstado = nuevoEstado,
                    usuarioAccion = currentUser.uid
                )
                
                result.fold(
                    onSuccess = {
                        _successMessage.value = "Estado actualizado exitosamente"
                        loadDomiciliosPendientes() // Recargar lista
                        Log.d("AdminDomicilioViewModel", "✅ Estado actualizado: $domicilioId -> ${nuevoEstado.name}")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al actualizar estado"
                        Log.e("AdminDomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("AdminDomicilioViewModel", "❌ Error: ${e.message}", e)
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
                
                val targetUserId = usuarioId ?: currentUser.uid
                
                val domicilio = Domicilio(
                    domicilioId = "",
                    usuarioId = targetUserId,
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
                        loadDomiciliosPendientes()
                        Log.d("AdminDomicilioViewModel", "✅ Domicilio producto creado: $it")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al crear solicitud"
                        Log.e("AdminDomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("AdminDomicilioViewModel", "❌ Error: ${e.message}", e)
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
                
                val targetUserId = usuarioId ?: currentUser.uid
                
                val domicilio = Domicilio(
                    domicilioId = "",
                    usuarioId = targetUserId,
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
                        loadDomiciliosPendientes()
                        Log.d("AdminDomicilioViewModel", "✅ Domicilio servicio creado: $it")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al crear solicitud"
                        Log.e("AdminDomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("AdminDomicilioViewModel", "❌ Error: ${e.message}", e)
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
        fechaSolicitada: com.google.firebase.Timestamp,
        fotoUri: Uri?,
        usuarioId: String? = null
    ) {
        val datosServicio = DatosServicio(
            tipoServicio = TipoServicioVeterinario.CONSULTA_GENERAL,
            descripcion = tipoServicio,
            fechaSolicitada = fechaSolicitada
        )
        createDomicilioServicio(nombreUsuario, telefono, direccion, datosServicio, fotoUri, usuarioId)
    }
    
    /**
     * Limpia mensajes de error y éxito
     */
    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}

