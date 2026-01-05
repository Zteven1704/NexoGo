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
 * ViewModel para el módulo de Citas conectado a Firebase
 * Mantiene la sincronización en tiempo real sin cambiar la UI
 */
class FirebaseAppointmentViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    init {
        // Escuchar cambios en tiempo real
        viewModelScope.launch {
            repository.listenToAppointments().collect { appointmentsList ->
                _appointments.value = appointmentsList
            }
        }
    }
    
    /**
     * Carga todas las citas desde Firebase
     */
    fun loadAppointments() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getAllAppointments()
                if (result.isSuccess) {
                    _appointments.value = result.getOrNull() ?: emptyList()
                    _message.value = "Citas cargadas exitosamente"
                } else {
                    _message.value = "Error al cargar citas: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar citas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Crea una nueva cita
     */
    fun createAppointment(appointment: Appointment) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addAppointment(appointment)
                if (result.isSuccess) {
                    _message.value = "Cita creada exitosamente"
                } else {
                    _message.value = "Error al crear cita: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al crear cita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Guarda una cita (nueva o actualizada)
     */
    fun saveAppointment(appointment: Appointment) {
        println("DEBUG: FirebaseAppointmentViewModel - Iniciando guardado de cita")
        println("DEBUG: FirebaseAppointmentViewModel - ID: ${appointment.id}")
        println("DEBUG: FirebaseAppointmentViewModel - Mascota: ${appointment.patientName}")
        
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addAppointment(appointment)
                if (result.isSuccess) {
                    println("DEBUG: FirebaseAppointmentViewModel - Cita guardada exitosamente")
                    _message.value = "Cita guardada exitosamente"
                } else {
                    println("DEBUG: FirebaseAppointmentViewModel - Error al guardar: ${result.exceptionOrNull()?.message}")
                    _message.value = "Error al guardar cita: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                println("DEBUG: FirebaseAppointmentViewModel - Excepción: ${e.message}")
                _message.value = "Error al guardar cita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza una cita existente
     */
    fun updateAppointment(appointment: Appointment) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addAppointment(appointment)
                if (result.isSuccess) {
                    _message.value = "Cita actualizada exitosamente"
                } else {
                    _message.value = "Error al actualizar cita: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al actualizar cita: ${e.message}"
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
     * Obtiene una cita por ID
     */
    fun getAppointmentById(appointmentId: String): Appointment? {
        return _appointments.value.find { it.id == appointmentId }
    }
    
    /**
     * Obtiene citas por veterinario
     */
    fun getAppointmentsByVeterinarian(veterinarianId: String): List<Appointment> {
        return _appointments.value.filter { it.vetId == veterinarianId }
    }
    
    /**
     * Obtiene citas por paciente
     */
    fun getAppointmentsByPatient(patientId: String): List<Appointment> {
        return _appointments.value.filter { it.patientId == patientId }
    }
    
    /**
     * Obtiene citas por estado
     */
    fun getAppointmentsByStatus(status: AppointmentStatus): List<Appointment> {
        return _appointments.value.filter { it.status == status }
    }
    
    /**
     * Obtiene citas por rango de fechas
     */
    fun getAppointmentsByDateRange(startDate: com.google.firebase.Timestamp, endDate: com.google.firebase.Timestamp): List<Appointment> {
        return _appointments.value.filter { appointment ->
            appointment.dateTime >= startDate && appointment.dateTime <= endDate
        }
    }
    
    /**
     * Obtiene estadísticas de citas
     */
    fun getAppointmentStats(): AppointmentStats {
        val appointments = _appointments.value
        val total = appointments.size
        val scheduled = appointments.count { it.status == AppointmentStatus.SCHEDULED }
        val completed = appointments.count { it.status == AppointmentStatus.COMPLETED }
        val cancelled = appointments.count { it.status == AppointmentStatus.CANCELLED }
        
        return AppointmentStats(
            total = total,
            scheduled = scheduled,
            completed = completed,
            cancelled = cancelled
        )
    }
    
    /**
     * Cambia el estado de una cita
     */
    fun changeAppointmentStatus(appointmentId: String, newStatus: AppointmentStatus) {
        val appointment = _appointments.value.find { it.id == appointmentId }
        if (appointment != null) {
            val updatedAppointment = appointment.copy(status = newStatus)
            updateAppointment(updatedAppointment)
        }
    }
    
    /**
     * Obtiene citas por fecha
     */
    fun getAppointmentsByDate(date: String): List<Appointment> {
        return _appointments.value.filter { appointment ->
            // Aquí implementarías la lógica de filtrado por fecha
            true // Por ahora retorna todas
        }
    }
    
    /**
     * Limpia el mensaje
     */
    fun clearMessage() {
        _message.value = ""
    }
}

data class AppointmentStats(
    val total: Int,
    val scheduled: Int,
    val completed: Int,
    val cancelled: Int
)
