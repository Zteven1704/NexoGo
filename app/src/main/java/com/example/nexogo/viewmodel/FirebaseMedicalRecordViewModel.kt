package com.example.nexogo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.model.MedicalRecord
import com.example.nexogo.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirebaseMedicalRecordViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    private val _medicalRecords = MutableStateFlow<List<MedicalRecord>>(emptyList())
    val medicalRecords: StateFlow<List<MedicalRecord>> = _medicalRecords.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    init {
        // Escuchar cambios en tiempo real
        viewModelScope.launch {
            repository.listenToMedicalRecords().collect { recordsList ->
                _medicalRecords.value = recordsList
            }
        }
    }

    /**
     * Carga todos los historiales clínicos
     */
    fun loadMedicalRecords() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getAllMedicalRecords()
                if (result.isSuccess) {
                    _medicalRecords.value = result.getOrNull() ?: emptyList()
                    _message.value = "Historiales clínicos cargados exitosamente"
                } else {
                    _message.value = "Error al cargar historiales: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al cargar historiales: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Guarda un nuevo historial clínico
     */
    fun saveMedicalRecord(medicalRecord: MedicalRecord) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addMedicalRecord(medicalRecord)
                if (result.isSuccess) {
                    _message.value = "Historial clínico guardado exitosamente"
                } else {
                    _message.value = "Error al guardar historial: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al guardar historial: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza un historial clínico existente
     */
    fun updateMedicalRecord(medicalRecord: MedicalRecord) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.addMedicalRecord(medicalRecord)
                if (result.isSuccess) {
                    _message.value = "Historial clínico actualizado exitosamente"
                } else {
                    _message.value = "Error al actualizar historial: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al actualizar historial: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Elimina un historial clínico
     */
    fun deleteMedicalRecord(recordId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.deleteMedicalRecord(recordId)
                if (result.isSuccess) {
                    _message.value = "Historial clínico eliminado exitosamente"
                } else {
                    _message.value = "Error al eliminar historial: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Error al eliminar historial: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Obtiene un historial clínico por ID
     */
    fun getMedicalRecordById(recordId: String): MedicalRecord? {
        return _medicalRecords.value.find { it.id == recordId }
    }
    
    /**
     * Obtiene historiales por paciente
     */
    fun getMedicalRecordsByPatient(patientId: String): List<MedicalRecord> {
        return _medicalRecords.value.filter { it.patientId == patientId }
    }
    
    /**
     * Obtiene historiales por veterinario
     */
    fun getMedicalRecordsByVeterinarian(veterinarianId: String): List<MedicalRecord> {
        return _medicalRecords.value.filter { it.veterinarianId == veterinarianId }
    }
    
    /**
     * Busca historiales por motivo de consulta
     */
    fun searchMedicalRecordsByReason(reason: String): List<MedicalRecord> {
        return _medicalRecords.value.filter { record ->
            record.reason.contains(reason, ignoreCase = true) ||
            record.consultationReason.contains(reason, ignoreCase = true)
        }
    }
    
    /**
     * Obtiene estadísticas de historiales clínicos
     */
    fun getMedicalRecordStats(): MedicalRecordStats {
        val records = _medicalRecords.value
        val total = records.size
        val completed = records.count { it.isCompleted }
        val emergency = records.count { it.isEmergency }
        
        return MedicalRecordStats(
            total = total,
            completed = completed,
            emergency = emergency
        )
    }

    /**
     * Limpia el mensaje actual
     */
    fun clearMessage() {
        _message.value = ""
    }
}

data class MedicalRecordStats(
    val total: Int,
    val completed: Int,
    val emergency: Int
)
