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
 * ViewModel para gestión de citas
 */
class AppointmentViewModel : ViewModel() {
    
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
    
    init {
        Log.d("NEXOGO_APPT", "AppointmentViewModel - Inicializando...")
        loadAppointments()
        loadPatients()
        startRealtimeListener()
        Log.d("NEXOGO_APPT", "AppointmentViewModel - Inicialización completada")
    }
    
    private fun startRealtimeListener() {
        viewModelScope.launch {
            repository.listenToAppointments().collect { appointments ->
                Log.d("NEXOGO_APPT", "Listener actualizado: ${appointments.size} citas")
                _appointments.value = appointments
                Log.d("NEXOGO_APPT", "Citas actualizadas desde listener: ${appointments.size}")
            }
        }
    }
    
    fun loadAppointments() {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                Log.d("NEXOGO_APPT", "Iniciando carga de citas...")
                val result = repository.getAllAppointments()
                Log.d("NEXOGO_APPT", "Resultado obtenido: ${result.isSuccess}")
                
                if (result.isSuccess) {
                    val appointments = result.getOrNull() ?: emptyList()
                    _appointments.value = appointments
                    _message.value = "Citas cargadas exitosamente"
                    Log.d("NEXOGO_APPT", "Citas cargadas: ${appointments.size}")
                } else {
                    val error = result.exceptionOrNull()
                    _message.value = "Error: ${error?.message ?: "Error desconocido"}"
                    Log.e("NEXOGO_APPT", "Error cargando citas: ${error?.message}", error)
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_APPT", "Excepción cargando citas: ${e.message}", e)
            } finally {
                _isLoading.value = false
                Log.d("NEXOGO_APPT", "Carga de citas finalizada")
            }
        }
    }
    
    fun loadPatients() {
        viewModelScope.launch {
            try {
                val result = repository.getAllPatients()
                if (result.isSuccess) {
                    val patients = result.getOrNull() ?: emptyList()
                    _patients.value = patients
                    Log.d("NEXOGO_APPT", "Pacientes cargados: ${patients.size}")
                } else {
                    Log.e("NEXOGO_APPT", "Error cargando pacientes: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e("NEXOGO_APPT", "Excepción cargando pacientes: ${e.message}")
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
                
                val result = repository.addAppointment(appointment)
                if (result.isSuccess) {
                    _appointments.value = _appointments.value + appointment
                    _message.value = "Cita creada exitosamente"
                    Log.d("NEXOGO_APPT", "Cita creada: $appointmentId")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_APPT", "Error creando cita: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_APPT", "Excepción creando cita: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        viewModelScope.launch {
            try {
                // Buscar la cita actual y actualizar su estado
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
                        Log.d("NEXOGO_APPT", "Estado de cita actualizado: $appointmentId -> $status")
                    } else {
                        _message.value = "Error: ${result.exceptionOrNull()?.message}"
                        Log.e("NEXOGO_APPT", "Error actualizando estado: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    _message.value = "Cita no encontrada"
                    Log.e("NEXOGO_APPT", "Cita no encontrada: $appointmentId")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_APPT", "Excepción actualizando estado: ${e.message}")
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
                    Log.d("NEXOGO_APPT", "Cita eliminada: $appointmentId")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_APPT", "Error eliminando cita: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_APPT", "Excepción eliminando cita: ${e.message}")
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
        
        return _appointments.value.filter { appointment ->
            appointment.dateTime >= startOfDay && appointment.dateTime < endOfDay
        }
    }
    
    fun getAppointmentsForMonth(year: Int, month: Int): List<Appointment> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = Timestamp(calendar.time)
        
        calendar.add(Calendar.MONTH, 1)
        val endOfMonth = Timestamp(calendar.time)
        
        return _appointments.value.filter { appointment ->
            appointment.dateTime >= startOfMonth && appointment.dateTime < endOfMonth
        }
    }
    
    fun clearMessage() {
        _message.value = ""
    }
}
