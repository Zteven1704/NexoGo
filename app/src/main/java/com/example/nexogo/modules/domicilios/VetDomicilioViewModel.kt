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
 * ViewModel para Veterinarios/Auxiliares - Gestión de domicilios asignados
 */
class VetDomicilioViewModel(
    private val repository: DomicilioRepository
) : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    
    private val _domiciliosAsignados = MutableStateFlow<List<Domicilio>>(emptyList())
    val domiciliosAsignados: StateFlow<List<Domicilio>> = _domiciliosAsignados.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    init {
        loadDomiciliosAsignados()
    }
    
    /**
     * Carga los domicilios asignados al veterinario/auxiliar actual
     */
    fun loadDomiciliosAsignados() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val currentUser = auth.currentUser
                    ?: throw Exception("Usuario no autenticado")
                
                val result = repository.getDomiciliosAsignados(currentUser.uid)
                result.fold(
                    onSuccess = { domicilios ->
                        _domiciliosAsignados.value = domicilios
                        Log.d("VetDomicilioViewModel", "✅ Domicilios asignados cargados: ${domicilios.size}")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al cargar domicilios"
                        Log.e("VetDomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("VetDomicilioViewModel", "❌ Error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cambia el estado a "En Camino"
     */
    fun marcarEnCamino(domicilioId: String) {
        cambiarEstado(domicilioId, DomicilioEstado.EN_CAMINO)
    }
    
    /**
     * Cambia el estado a "Finalizado"
     */
    fun marcarFinalizado(domicilioId: String) {
        cambiarEstado(domicilioId, DomicilioEstado.FINALIZADO)
    }
    
    /**
     * Cambia el estado de un domicilio
     */
    private fun cambiarEstado(
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
                        val mensaje = when (nuevoEstado) {
                            DomicilioEstado.EN_CAMINO -> "Estado actualizado a 'En Camino'"
                            DomicilioEstado.FINALIZADO -> "Servicio marcado como finalizado"
                            else -> "Estado actualizado exitosamente"
                        }
                        _successMessage.value = mensaje
                        loadDomiciliosAsignados() // Recargar lista
                        Log.d("VetDomicilioViewModel", "✅ Estado actualizado: $domicilioId -> ${nuevoEstado.name}")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al actualizar estado"
                        Log.e("VetDomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("VetDomicilioViewModel", "❌ Error: ${e.message}", e)
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
                        loadDomiciliosAsignados()
                        Log.d("VetDomicilioViewModel", "✅ Domicilio producto creado: $it")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al crear solicitud"
                        Log.e("VetDomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("VetDomicilioViewModel", "❌ Error: ${e.message}", e)
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
                        loadDomiciliosAsignados()
                        Log.d("VetDomicilioViewModel", "✅ Domicilio servicio creado: $it")
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al crear solicitud"
                        Log.e("VetDomicilioViewModel", "❌ Error: ${exception.message}", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Error inesperado"
                Log.e("VetDomicilioViewModel", "❌ Error: ${e.message}", e)
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

