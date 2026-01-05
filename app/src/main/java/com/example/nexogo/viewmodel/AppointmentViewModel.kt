package com.example.nexogo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.model.Appointment
import com.example.nexogo.model.AppointmentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class AppointmentViewModel : ViewModel() {
    
    companion object {
        @Volatile
        private var INSTANCE: AppointmentViewModel? = null
        
        fun getInstance(): AppointmentViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppointmentViewModel().also { INSTANCE = it }
            }
        }
    }
    
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    
    init {
        // Initialize with empty list to avoid any potential issues
        _appointments.value = emptyList()
    }
    
    fun selectDate(date: Date) {
        _selectedDate.value = date
    }
    
    fun getAppointmentsForDate(date: Date): List<Appointment> {
        // Simplified - return empty list for now
        return emptyList()
    }
    
    fun addAppointment(appointment: Appointment) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate network delay
                kotlinx.coroutines.delay(500)
                
                val newAppointment = appointment.copy(
                    id = "appointment_${System.currentTimeMillis()}",
                    createdAt = com.google.firebase.Timestamp.now(),
                    updatedAt = com.google.firebase.Timestamp.now()
                )
                _appointments.value = _appointments.value + newAppointment
                _message.value = "Cita agregada exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al agregar la cita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate network delay
                kotlinx.coroutines.delay(500)
                
                _appointments.value = _appointments.value.map { 
                    if (it.id == appointment.id) appointment.copy(updatedAt = com.google.firebase.Timestamp.now()) else it 
                }
                _message.value = "Cita actualizada exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al actualizar la cita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteAppointment(appointmentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simulate network delay
                kotlinx.coroutines.delay(500)
                
                _appointments.value = _appointments.value.filter { it.id != appointmentId }
                _message.value = "Cita eliminada exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al eliminar la cita: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessage() {
        _message.value = null
    }
}
