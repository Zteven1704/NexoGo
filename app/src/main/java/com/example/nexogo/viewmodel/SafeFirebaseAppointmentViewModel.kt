package com.example.nexogo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.model.Appointment
import com.example.nexogo.model.AppointmentStatus
import com.example.nexogo.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel seguro para el módulo de Citas
 * Sin inicialización automática que pueda causar crashes
 */
class SafeFirebaseAppointmentViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    // NO hacer nada en init para evitar crashes
    
    /**
     * Carga todas las citas desde Firebase
     */
    fun loadAppointments() {
        println("DEBUG: SafeFirebaseAppointmentViewModel - Iniciando carga de citas")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getAllAppointments()
                if (result.isSuccess) {
                    _appointments.value = result.getOrNull() ?: emptyList()
                    _message.value = "Citas cargadas exitosamente"
                    println("DEBUG: SafeFirebaseAppointmentViewModel - Citas cargadas: ${_appointments.value.size}")
                } else {
                    _message.value = "Error al cargar citas: ${result.exceptionOrNull()?.message}"
                    println("DEBUG: SafeFirebaseAppointmentViewModel - Error: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar citas: ${e.message}"
                println("DEBUG: SafeFirebaseAppointmentViewModel - Excepción: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Guarda una cita (nueva o actualizada)
     */
    fun saveAppointment(appointment: Appointment) {
        println("DEBUG: SafeFirebaseAppointmentViewModel - Iniciando guardado de cita")
        println("DEBUG: SafeFirebaseAppointmentViewModel - ID: ${appointment.id}")
        println("DEBUG: SafeFirebaseAppointmentViewModel - Mascota: ${appointment.patientName}")
        
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addAppointment(appointment)
                if (result.isSuccess) {
                    println("DEBUG: SafeFirebaseAppointmentViewModel - Cita guardada exitosamente")
                    _message.value = "Cita guardada exitosamente"
                } else {
                    println("DEBUG: SafeFirebaseAppointmentViewModel - Error al guardar: ${result.exceptionOrNull()?.message}")
                    _message.value = "Error al guardar cita: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                println("DEBUG: SafeFirebaseAppointmentViewModel - Excepción: ${e.message}")
                _message.value = "Error al guardar cita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Elimina una cita
     */
    fun deleteAppointment(appointmentId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.deleteAppointment(appointmentId)
                if (result.isSuccess) {
                    _message.value = "Cita eliminada exitosamente"
                } else {
                    _message.value = "Error al eliminar cita: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al eliminar cita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Limpia el mensaje
     */
    fun clearMessage() {
        _message.value = ""
    }
}

