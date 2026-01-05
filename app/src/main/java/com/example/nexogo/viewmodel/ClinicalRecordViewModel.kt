package com.example.nexogo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.data.AppDataStore
import com.example.nexogo.model.ClinicalRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import java.util.Date

class ClinicalRecordViewModel(private val context: Context) : ViewModel() {
    
    companion object {
        @Volatile
        private var INSTANCE: ClinicalRecordViewModel? = null
        
        fun getInstance(context: Context): ClinicalRecordViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ClinicalRecordViewModel(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val dataStore = AppDataStore(context)
    
    private val _clinicalRecords = MutableStateFlow<List<ClinicalRecord>>(emptyList())
    val clinicalRecords: StateFlow<List<ClinicalRecord>> = _clinicalRecords.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    
    init {
        viewModelScope.launch {
            loadClinicalRecords()
        }
    }
    
    private suspend fun loadClinicalRecords() {
        _isLoading.value = true
        try {
            val storedRecords = dataStore.loadClinicalRecords()
            if (storedRecords.isNotEmpty()) {
                _clinicalRecords.value = storedRecords
            } else {
                loadSampleRecords()
            }
        } catch (e: Exception) {
            loadSampleRecords()
        } finally {
            _isLoading.value = false
        }
    }
    
    private fun loadSampleRecords() {
        val sampleRecords = listOf(
            ClinicalRecord(
                id = "cr1",
                patientId = "1",
                petId = "pet1",
                ownerData = com.example.nexogo.model.OwnerData(
                    name = "María González",
                    address = "Calle Principal 123, Ciudad",
                    phone = "555-0101",
                    email = "maria.gonzalez@email.com",
                    documentId = "12345678"
                ),
                petData = com.example.nexogo.model.PetData(
                    name = "Max",
                    species = "Perro",
                    breed = "Labrador",
                    color = "Dorado",
                    gender = "Macho",
                    age = 3,
                    weight = 25.5,
                    reproductiveStatus = com.example.nexogo.model.ReproductiveStatus.STERILIZED,
                    origin = com.example.nexogo.model.Origin.URBAN
                ),
                consultationReason = "Revisión anual y vacunación",
                anamnesis = com.example.nexogo.model.Anamnesis(
                    feeding = com.example.nexogo.model.FeedingInfo(
                        type = "Alimento balanceado premium",
                        appetite = "Normal"
                    ),
                    vaccinations = listOf(
                        com.example.nexogo.model.ClinicalVaccination(
                            id = "v1",
                            name = "Antirrábica",
                            date = Date(),
                            nextDue = Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)
                        )
                    ),
                    habitat = "Casa con jardín",
                    cohabitation = "Vive con 2 personas adultas"
                ),
                physicalExam = com.example.nexogo.model.ClinicalPhysicalExam(
                    temperature = 38.5,
                    heartRate = 80,
                    respiratoryRate = 20,
                    bodyCondition = com.example.nexogo.model.BodyCondition.NORMAL,
                    hydrationStatus = com.example.nexogo.model.HydrationStatus.NORMAL,
                    generalAppearance = "Animal en buen estado general"
                ),
                diagnoses = com.example.nexogo.model.Diagnoses(
                    presumptiveDiagnosis = "Animal sano",
                    definitiveDiagnosis = "Animal sano - apto para vacunación"
                ),
                therapeuticPlan = com.example.nexogo.model.TherapeuticPlan(
                    treatments = listOf(
                        com.example.nexogo.model.ClinicalTreatment(
                            id = "t1",
                            activeIngredient = "Vacuna antirrábica",
                            commercialName = "Rabvac",
                            dose = "1 ml",
                            route = "Subcutánea",
                            frequency = "Anual",
                            duration = "1 dosis"
                        )
                    ),
                    recommendations = "Continuar con alimentación actual, ejercicio regular"
                ),
                prognosis = "Excelente",
                createdBy = "vet1",
                createdByName = "Dr. Juan Pérez"
            )
        )
        
        _clinicalRecords.value = sampleRecords
        viewModelScope.launch {
            saveClinicalRecords()
        }
    }
    
    private suspend fun saveClinicalRecords() {
        try {
            dataStore.saveClinicalRecords(_clinicalRecords.value)
        } catch (e: Exception) {
            _message.value = "Error al guardar historiales clínicos: ${e.message}"
        }
    }
    
    fun getClinicalRecordsForPet(petId: String): List<ClinicalRecord> {
        return _clinicalRecords.value.filter { it.petId == petId }
    }
    
    fun getClinicalRecordsForPatient(patientId: String): List<ClinicalRecord> {
        return _clinicalRecords.value.filter { it.patientId == patientId }
    }
    
    fun addClinicalRecord(record: ClinicalRecord) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newRecord = record.copy(
                    id = "cr_${System.currentTimeMillis()}",
                    createdAt = Timestamp.now(),
                    lastModifiedAt = Timestamp.now()
                )
                val updatedRecords = _clinicalRecords.value + newRecord
                _clinicalRecords.value = updatedRecords
                saveClinicalRecords()
                _message.value = "Historial clínico agregado exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al agregar historial clínico: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateClinicalRecord(record: ClinicalRecord) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedRecord = record.copy(
                    lastModifiedAt = Timestamp.now()
                )
                val currentRecords = _clinicalRecords.value.toMutableList()
                val index = currentRecords.indexOfFirst { it.id == record.id }
                if (index != -1) {
                    currentRecords[index] = updatedRecord
                    _clinicalRecords.value = currentRecords
                    saveClinicalRecords()
                    _message.value = "Historial clínico actualizado exitosamente"
                }
            } catch (e: Exception) {
                _message.value = "Error al actualizar historial clínico: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteClinicalRecord(recordId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedRecords = _clinicalRecords.value.filter { it.id != recordId }
                _clinicalRecords.value = updatedRecords
                saveClinicalRecords()
                _message.value = "Historial clínico eliminado exitosamente"
            } catch (e: Exception) {
                _message.value = "Error al eliminar historial clínico: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessage() {
        _message.value = null
    }
}
