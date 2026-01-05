package com.example.nexogo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.data.AppDataStore
import com.example.nexogo.model.SimplePatient
import com.example.nexogo.model.SimplePet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PatientViewModel(private val context: Context) : ViewModel() {
    
    companion object {
        @Volatile
        private var INSTANCE: PatientViewModel? = null
        
        fun getInstance(context: Context): PatientViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PatientViewModel(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val dataStore = AppDataStore(context)
    
    private val _patients = MutableStateFlow<List<SimplePatient>>(emptyList())
    val patients: StateFlow<List<SimplePatient>> = _patients.asStateFlow()
    
    init {
        viewModelScope.launch {
            loadPatients()
        }
    }
    
    private suspend fun loadPatients() {
        try {
            val storedPatients = dataStore.loadUsers()
            if (storedPatients.isNotEmpty()) {
                _patients.value = storedPatients
            } else {
                loadSamplePatients()
            }
        } catch (e: Exception) {
            loadSamplePatients()
        }
    }
    
    private fun loadSamplePatients() {
        val samplePatients = listOf(
            SimplePatient(
                id = "1",
                name = "María González",
                phone = "555-0101",
                email = "maria.gonzalez@email.com",
                address = "Calle Principal 123, Ciudad",
                pets = listOf(
                    SimplePet(id = "pet1", name = "Max", species = "Perro", breed = "Labrador", age = 3),
                    SimplePet(id = "pet2", name = "Luna", species = "Gato", breed = "Persa", age = 2)
                )
            ),
            SimplePatient(
                id = "2",
                name = "Juan Pérez",
                phone = "555-0102",
                email = "juan.perez@email.com",
                address = "Avenida Central 456, Ciudad",
                pets = listOf(
                    SimplePet(id = "pet3", name = "Bella", species = "Perro", breed = "Golden Retriever", age = 5)
                )
            ),
            SimplePatient(
                id = "3",
                name = "Ana López",
                phone = "555-0103",
                email = "ana.lopez@email.com",
                address = "Plaza Mayor 789, Ciudad",
                pets = listOf(
                    SimplePet(id = "pet4", name = "Milo", species = "Gato", breed = "Siamés", age = 1),
                    SimplePet(id = "pet5", name = "Rex", species = "Perro", breed = "Pastor Alemán", age = 4)
                )
            ),
            SimplePatient(
                id = "4",
                name = "Carlos Rodríguez",
                phone = "555-0104",
                email = "carlos.rodriguez@email.com",
                address = "Calle Secundaria 321, Ciudad",
                pets = listOf(
                    SimplePet(id = "pet6", name = "Nala", species = "Gato", breed = "Maine Coon", age = 2)
                )
            )
        )
        
        _patients.value = samplePatients
        viewModelScope.launch {
            savePatients()
        }
    }
    
    private suspend fun savePatients() {
        try {
            dataStore.saveUsers(_patients.value)
        } catch (e: Exception) {
            // Manejar error de guardado
        }
    }
    
    fun getAllPets(): List<SimplePet> {
        return _patients.value.flatMap { patient ->
            patient.pets.map { pet ->
                pet.copy(name = "${pet.name} (${patient.name})")
            }
        }
    }
    
    fun addPatient(patient: SimplePatient) {
        viewModelScope.launch {
            val currentPatients = _patients.value.toMutableList()
            currentPatients.add(patient)
            _patients.value = currentPatients
            savePatients()
        }
    }
    
    fun updatePatient(patient: SimplePatient) {
        viewModelScope.launch {
            val currentPatients = _patients.value.toMutableList()
            val index = currentPatients.indexOfFirst { it.id == patient.id }
            if (index != -1) {
                currentPatients[index] = patient
                _patients.value = currentPatients
                savePatients()
            }
        }
    }
    
    fun deletePatient(patientId: String) {
        viewModelScope.launch {
            val currentPatients = _patients.value.filter { it.id != patientId }
            _patients.value = currentPatients
            savePatients()
        }
    }
}
