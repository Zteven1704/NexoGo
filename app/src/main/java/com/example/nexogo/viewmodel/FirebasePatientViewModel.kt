package com.example.nexogo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.model.Patient
import com.example.nexogo.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirebasePatientViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    init {
        // Escuchar cambios en tiempo real
        viewModelScope.launch {
            repository.listenToPatients().collect { patientsList ->
                _patients.value = patientsList
            }
        }
    }

    /**
     * Carga todos los pacientes
     */
    fun loadPatients() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getAllPatients()
                if (result.isSuccess) {
                    _patients.value = result.getOrNull() ?: emptyList()
                    _message.value = "Pacientes cargados exitosamente"
                } else {
                    _message.value = "Error al cargar pacientes: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar pacientes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Guarda un nuevo paciente
     */
    fun savePatient(patient: Patient) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addPatient(patient)
                if (result.isSuccess) {
                    _message.value = "Paciente guardado exitosamente"
                } else {
                    _message.value = "Error al guardar paciente: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al guardar paciente: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza un paciente existente
     */
    fun updatePatient(patient: Patient) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addPatient(patient)
                if (result.isSuccess) {
                    _message.value = "Paciente actualizado exitosamente"
                } else {
                    _message.value = "Error al actualizar paciente: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al actualizar paciente: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina un paciente
     */
    fun deletePatient(patientId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.deletePatient(patientId)
                if (result.isSuccess) {
                    _message.value = "Paciente eliminado exitosamente"
                } else {
                    _message.value = "Error al eliminar paciente: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al eliminar paciente: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Busca pacientes por nombre del propietario
     */
    fun searchPatientsByOwner(ownerName: String): List<Patient> {
        return _patients.value.filter { patient ->
            patient.ownerName.contains(ownerName, ignoreCase = true)
        }
    }

    /**
     * Busca pacientes por teléfono
     */
    fun searchPatientsByPhone(phone: String): List<Patient> {
        return _patients.value.filter { patient ->
            patient.ownerPhone.contains(phone, ignoreCase = true)
        }
    }

    /**
     * Obtiene un paciente por ID
     */
    fun getPatientById(patientId: String): Patient? {
        return _patients.value.find { it.id == patientId }
    }

    /**
     * Obtiene estadísticas de pacientes
     */
    fun getPatientStats(): PatientStats {
        val patients = _patients.value
        val total = patients.size
        val totalPets = patients.sumOf { it.pets.size }
        val averagePetsPerOwner = if (total > 0) totalPets.toDouble() / total else 0.0
        
        return PatientStats(
            totalPatients = total,
            totalPets = totalPets,
            averagePetsPerOwner = averagePetsPerOwner
        )
    }

    /**
     * Limpia el mensaje actual
     */
    fun clearMessage() {
        _message.value = ""
    }
}

data class PatientStats(
    val totalPatients: Int,
    val totalPets: Int,
    val averagePetsPerOwner: Double
)