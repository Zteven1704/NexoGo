package com.example.nexogo.modules.patients

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.core.models.Patient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

/**
 * ViewModel para gestión de pacientes
 */
class PatientViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredPatients = MutableStateFlow<List<Patient>>(emptyList())
    val filteredPatients: StateFlow<List<Patient>> = _filteredPatients.asStateFlow()
    
    init {
        loadPatients()
    }
    
    fun loadPatients() {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val result = repository.getCollection("patients")
                if (result.isSuccess) {
                    val patientsData = result.getOrNull() ?: emptyList()
                    val patients = patientsData.mapNotNull { data ->
                        try {
                            Patient(
                                id = data["id"] as? String ?: "",
                                ownerId = data["ownerId"] as? String ?: "",
                                ownerName = data["ownerName"] as? String ?: "",
                                name = data["name"] as? String ?: "",
                                species = data["species"] as? String ?: "",
                                breed = data["breed"] as? String ?: "",
                                age = (data["age"] as? Number)?.toInt() ?: 0,
                                weight = (data["weight"] as? Number)?.toDouble() ?: 0.0,
                                color = data["color"] as? String ?: "",
                                gender = data["gender"] as? String ?: "",
                                imageUrl = data["imageUrl"] as? String ?: "",
                                medicalNotes = data["medicalNotes"] as? String ?: ""
                            )
                        } catch (e: Exception) {
                            Log.e("NEXOGO_PATIENTS", "Error parseando paciente: ${e.message}")
                            null
                        }
                    }
                    _patients.value = patients
                    _filteredPatients.value = patients
                    _message.value = "Pacientes cargados exitosamente"
                    Log.d("NEXOGO_PATIENTS", "Pacientes cargados: ${patients.size}")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_PATIENTS", "Error cargando pacientes: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_PATIENTS", "Excepción cargando pacientes: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createPatient(
        ownerId: String,
        ownerName: String,
        name: String,
        species: String,
        breed: String,
        age: Int,
        weight: Double,
        color: String,
        gender: String,
        medicalNotes: String,
        imageUri: Uri? = null
    ) {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val patientId = repository.generateId()
                var imageUrl = ""
                
                // Subir imagen si se proporciona
                if (imageUri != null) {
                    val imagePath = "pets/$patientId/images/pet_${System.currentTimeMillis()}.jpg"
                    val uploadResult = repository.uploadFile(imagePath, imageUri)
                    if (uploadResult.isSuccess) {
                        imageUrl = uploadResult.getOrNull() ?: ""
                        Log.d("NEXOGO_PATIENTS", "Imagen de paciente subida: $imageUrl")
                    } else {
                        _message.value = "Error subiendo imagen: ${uploadResult.exceptionOrNull()?.message}"
                        Log.e("NEXOGO_PATIENTS", "Error subiendo imagen: ${uploadResult.exceptionOrNull()?.message}")
                        return@launch
                    }
                }
                
                val patient = Patient(
                    id = patientId,
                    ownerId = ownerId,
                    ownerName = ownerName,
                    name = name,
                    species = species,
                    breed = breed,
                    age = age,
                    weight = weight,
                    color = color,
                    gender = gender,
                    imageUrl = imageUrl,
                    medicalNotes = medicalNotes
                )
                
                val patientData = mapOf(
                    "id" to patient.id,
                    "ownerId" to patient.ownerId,
                    "ownerName" to patient.ownerName,
                    "name" to patient.name,
                    "species" to patient.species,
                    "breed" to patient.breed,
                    "age" to patient.age,
                    "weight" to patient.weight,
                    "color" to patient.color,
                    "gender" to patient.gender,
                    "imageUrl" to patient.imageUrl,
                    "medicalNotes" to patient.medicalNotes,
                    "createdAt" to repository.getCurrentTimestamp(),
                    "updatedAt" to repository.getCurrentTimestamp()
                )
                
                val result = repository.createDocument("patients", patientId, patientData)
                if (result.isSuccess) {
                    _patients.value = _patients.value + patient
                    _filteredPatients.value = _filteredPatients.value + patient
                    _message.value = "Paciente creado exitosamente"
                    Log.d("NEXOGO_PATIENTS", "Paciente creado: $patientId")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_PATIENTS", "Error creando paciente: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_PATIENTS", "Excepción creando paciente: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updatePatient(
        patientId: String,
        name: String,
        species: String,
        breed: String,
        age: Int,
        weight: Double,
        color: String,
        gender: String,
        medicalNotes: String,
        imageUri: Uri? = null
    ) {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                var imageUrl = _patients.value.find { it.id == patientId }?.imageUrl ?: ""
                
                // Subir nueva imagen si se proporciona
                if (imageUri != null) {
                    val imagePath = "pets/$patientId/images/pet_${System.currentTimeMillis()}.jpg"
                    val uploadResult = repository.uploadFile(imagePath, imageUri)
                    if (uploadResult.isSuccess) {
                        imageUrl = uploadResult.getOrNull() ?: ""
                        Log.d("NEXOGO_PATIENTS", "Imagen de paciente actualizada: $imageUrl")
                    } else {
                        _message.value = "Error subiendo imagen: ${uploadResult.exceptionOrNull()?.message}"
                        Log.e("NEXOGO_PATIENTS", "Error subiendo imagen: ${uploadResult.exceptionOrNull()?.message}")
                        return@launch
                    }
                }
                
                val updateData = mapOf(
                    "name" to name,
                    "species" to species,
                    "breed" to breed,
                    "age" to age,
                    "weight" to weight,
                    "color" to color,
                    "gender" to gender,
                    "imageUrl" to imageUrl,
                    "medicalNotes" to medicalNotes,
                    "updatedAt" to repository.getCurrentTimestamp()
                )
                
                val result = repository.updateDocument("patients", patientId, updateData)
                if (result.isSuccess) {
                    _patients.value = _patients.value.map { patient ->
                        if (patient.id == patientId) {
                            patient.copy(
                                name = name,
                                species = species,
                                breed = breed,
                                age = age,
                                weight = weight,
                                color = color,
                                gender = gender,
                                imageUrl = imageUrl,
                                medicalNotes = medicalNotes
                            )
                        } else {
                            patient
                        }
                    }
                    _filteredPatients.value = _filteredPatients.value.map { patient ->
                        if (patient.id == patientId) {
                            patient.copy(
                                name = name,
                                species = species,
                                breed = breed,
                                age = age,
                                weight = weight,
                                color = color,
                                gender = gender,
                                imageUrl = imageUrl,
                                medicalNotes = medicalNotes
                            )
                        } else {
                            patient
                        }
                    }
                    _message.value = "Paciente actualizado exitosamente"
                    Log.d("NEXOGO_PATIENTS", "Paciente actualizado: $patientId")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_PATIENTS", "Error actualizando paciente: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_PATIENTS", "Excepción actualizando paciente: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deletePatient(patientId: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteDocument("patients", patientId)
                if (result.isSuccess) {
                    _patients.value = _patients.value.filter { it.id != patientId }
                    _filteredPatients.value = _filteredPatients.value.filter { it.id != patientId }
                    _message.value = "Paciente eliminado exitosamente"
                    Log.d("NEXOGO_PATIENTS", "Paciente eliminado: $patientId")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_PATIENTS", "Error eliminando paciente: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_PATIENTS", "Excepción eliminando paciente: ${e.message}")
            }
        }
    }
    
    fun searchPatients(query: String) {
        _searchQuery.value = query
        
        if (query.isEmpty()) {
            _filteredPatients.value = _patients.value
        } else {
            val filtered = _patients.value.filter { patient ->
                patient.name.contains(query, ignoreCase = true) ||
                patient.ownerName.contains(query, ignoreCase = true) ||
                patient.species.contains(query, ignoreCase = true) ||
                patient.breed.contains(query, ignoreCase = true)
            }
            _filteredPatients.value = filtered
        }
    }
    
    fun getPatientsByOwner(ownerId: String): List<Patient> {
        return _patients.value.filter { it.ownerId == ownerId }
    }
    
    fun clearMessage() {
        _message.value = ""
    }
}

