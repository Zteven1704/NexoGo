package com.example.nexogo.modules.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.repository.FirebaseRepository
import com.example.nexogo.model.Appointment
import com.example.nexogo.model.AppointmentStatus
import com.example.nexogo.model.Patient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.google.firebase.Timestamp
import java.util.*

/**
 * AppointmentViewModel mejorado para gestión de citas
 * Con mejor manejo de errores y logging detallado
 */
class AppointmentViewModelImproved : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()
    
    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()
    
    private var isListenerActive = false
    
    init {
        Log.d("NEXOGO_APPT_IMPROVED", "🚀 AppointmentViewModelImproved - Inicializando...")
        loadAppointments()
        loadPatients()
        startRealtimeListener()
        Log.d("NEXOGO_APPT_IMPROVED", "✅ AppointmentViewModelImproved - Inicialización completada")
    }
    
    private fun startRealtimeListener() {
        if (isListenerActive) {
            Log.d("NEXOGO_APPT_IMPROVED", "⚠️ Listener ya está activo, saltando...")
            return
        }
        
        isListenerActive = true
        viewModelScope.launch {
            try {
                Log.d("NEXOGO_APPT_IMPROVED", "🔄 Iniciando listener en tiempo real...")
                repository.listenToAppointments().collect { appointments ->
                    Log.d("NEXOGO_APPT_IMPROVED", "📡 Listener recibió: ${appointments.size} citas")
                    _appointments.value = appointments
                    Log.d("NEXOGO_APPT_IMPROVED", "✅ Citas actualizadas en UI: ${appointments.size}")
                    
                    // Log detallado de cada cita
                    appointments.forEachIndexed { index, appointment ->
                        Log.d("NEXOGO_APPT_IMPROVED", "📋 Cita $index: ${appointment.id} - ${appointment.patientName} - ${appointment.dateTime}")
                    }
                }
            } catch (e: Exception) {
                Log.e("NEXOGO_APPT_IMPROVED", "❌ Error en listener: ${e.message}", e)
                _message.value = "Error en listener: ${e.message}"
                isListenerActive = false
            }
        }
    }
    
    fun loadAppointments() {
        Log.d("NEXOGO_APPT_IMPROVED", "🔄 Cargando citas manualmente...")
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val result = repository.getAllAppointments()
                Log.d("NEXOGO_APPT_IMPROVED", "📊 Resultado de getAllAppointments: ${result.isSuccess}")
                
                if (result.isSuccess) {
                    val appointments = result.getOrNull() ?: emptyList()
                    Log.d("NEXOGO_APPT_IMPROVED", "📋 Citas cargadas: ${appointments.size}")
                    _appointments.value = appointments
                    _message.value = "Citas cargadas: ${appointments.size}"
                    
                    // Log detallado de cada cita
                    appointments.forEachIndexed { index, appointment ->
                        Log.d("NEXOGO_APPT_IMPROVED", "📋 Cita $index: ${appointment.id} - ${appointment.patientName} - ${appointment.dateTime}")
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("NEXOGO_APPT_IMPROVED", "❌ Error cargando citas: ${error?.message}")
                    _message.value = "Error: ${error?.message ?: "Error desconocido"}"
                }
            } catch (e: Exception) {
                Log.e("NEXOGO_APPT_IMPROVED", "❌ Excepción cargando citas: ${e.message}", e)
                _message.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d("NEXOGO_APPT_IMPROVED", "✅ Carga de citas finalizada")
            }
        }
    }
    
    fun loadPatients() {
        viewModelScope.launch {
            try {
                Log.d("NEXOGO_APPT_IMPROVED", "🔄 Cargando pacientes...")
                val result = repository.getAllPatients()
                if (result.isSuccess) {
                    val patients = result.getOrNull() ?: emptyList()
                    _patients.value = patients
                    Log.d("NEXOGO_APPT_IMPROVED", "👥 Pacientes cargados: ${patients.size}")
                } else {
                    Log.e("NEXOGO_APPT_IMPROVED", "❌ Error cargando pacientes: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("NEXOGO_APPT_IMPROVED", "❌ Excepción cargando pacientes: ${e.message}")
            }
        }
    }
    
    fun createAppointment(
        patientId: String,
        patientName: String,
        ownerId: String,
        ownerName: String,
        vetId: String,
        vetName: String,
        dateTime: Date,
        duration: Int,
        reason: String,
        notes: String,
        createdBy: String
    ) {
        Log.d("NEXOGO_APPT_IMPROVED", "🆕 Creando nueva cita...")
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val appointmentId = UUID.randomUUID().toString()
                val appointment = Appointment(
                    id = appointmentId,
                    patientId = patientId,
                    patientName = patientName,
                    ownerId = ownerId,
                    ownerName = ownerName,
                    vetId = vetId,
                    vetName = vetName,
                    dateTime = Timestamp(dateTime),
                    duration = duration,
                    status = AppointmentStatus.SCHEDULED,
                    reason = reason,
                    notes = notes,
                    createdBy = createdBy
                )
                
                Log.d("NEXOGO_APPT_IMPROVED", "📋 Cita creada: ${appointment.id} - ${appointment.patientName}")
                
                val result = repository.addAppointment(appointment)
                if (result.isSuccess) {
                    _message.value = "Cita creada exitosamente"
                    Log.d("NEXOGO_APPT_IMPROVED", "✅ Cita guardada en Firestore")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_APPT_IMPROVED", "❌ Error guardando cita: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_APPT_IMPROVED", "❌ Excepción creando cita: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        viewModelScope.launch {
            try {
                val currentAppointment = _appointments.value.find { it.id == appointmentId }
                if (currentAppointment != null) {
                    val updatedAppointment = currentAppointment.copy(status = status)
                    val result = repository.updateAppointment(updatedAppointment)
                    
                    if (result.isSuccess) {
                        _appointments.value = _appointments.value.map { appointment ->
                            if (appointment.id == appointmentId) {
                                appointment.copy(status = status)
                            } else {
                                appointment
                            }
                        }
                        _message.value = "Estado de cita actualizado"
                        Log.d("NEXOGO_APPT_IMPROVED", "✅ Estado actualizado: $appointmentId -> $status")
                    } else {
                        _message.value = "Error: ${result.exceptionOrNull()?.message}"
                        Log.e("NEXOGO_APPT_IMPROVED", "❌ Error actualizando estado: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    _message.value = "Cita no encontrada"
                    Log.e("NEXOGO_APPT_IMPROVED", "❌ Cita no encontrada: $appointmentId")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_APPT_IMPROVED", "❌ Excepción actualizando estado: ${e.message}")
            }
        }
    }
    
    fun deleteAppointment(appointmentId: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteAppointment(appointmentId)
                if (result.isSuccess) {
                    _appointments.value = _appointments.value.filter { it.id != appointmentId }
                    _message.value = "Cita eliminada exitosamente"
                    Log.d("NEXOGO_APPT_IMPROVED", "✅ Cita eliminada: $appointmentId")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_APPT_IMPROVED", "❌ Error eliminando cita: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_APPT_IMPROVED", "❌ Excepción eliminando cita: ${e.message}")
            }
        }
    }
    
    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
    }
    
    fun getAppointmentsForDate(date: Date): List<Appointment> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = Timestamp(calendar.time)
        
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = Timestamp(calendar.time)
        
        val filteredAppointments = _appointments.value.filter { appointment ->
            appointment.dateTime >= startOfDay && appointment.dateTime < endOfDay
        }
        
        Log.d("NEXOGO_APPT_IMPROVED", "📅 Citas para ${date}: ${filteredAppointments.size}")
        return filteredAppointments
    }
    
    fun getAppointmentsForMonth(date: Date): List<Appointment> {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = Timestamp(calendar.time)
        
        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = Timestamp(calendar.time)
        
        val filteredAppointments = _appointments.value.filter { appointment ->
            appointment.dateTime >= startOfMonth && appointment.dateTime < endOfMonth
        }
        
        Log.d("NEXOGO_APPT_IMPROVED", "📅 Citas para mes ${date}: ${filteredAppointments.size}")
        return filteredAppointments
    }
    
    /**
     * Actualiza una cita completa en Firestore
     */
    fun updateAppointment(updatedAppointment: Appointment) {
        viewModelScope.launch {
            try {
                Log.d("NEXOGO_APPT_IMPROVED", "🔄 Actualizando cita: ${updatedAppointment.id}")
                Log.d("NEXOGO_APPT_IMPROVED", "🔄 Mascota: ${updatedAppointment.patientName}")
                Log.d("NEXOGO_APPT_IMPROVED", "🔄 Fecha: ${updatedAppointment.dateTime}")
                
                val result = repository.updateAppointment(updatedAppointment)
                
                if (result.isSuccess) {
                    Log.d("NEXOGO_APPT_IMPROVED", "✅ Cita actualizada exitosamente")
                    // Actualizar la lista local
                    val currentAppointments = _appointments.value.toMutableList()
                    val index = currentAppointments.indexOfFirst { it.id == updatedAppointment.id }
                    if (index != -1) {
                        currentAppointments[index] = updatedAppointment
                        _appointments.value = currentAppointments
                    }
                } else {
                    Log.e("NEXOGO_APPT_IMPROVED", "❌ Error al actualizar cita: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("NEXOGO_APPT_IMPROVED", "❌ Excepción al actualizar cita: ${e.message}", e)
            }
        }
    }
}
