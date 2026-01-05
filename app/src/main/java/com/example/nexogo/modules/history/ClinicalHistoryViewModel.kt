package com.example.nexogo.modules.history

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.core.models.ClinicalRecord
import com.example.nexogo.core.models.PhysicalExam
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.google.firebase.Timestamp
import java.util.*

/**
 * ViewModel para gestión de historial clínico
 */
class ClinicalHistoryViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    
    private val _records = MutableStateFlow<List<ClinicalRecord>>(emptyList())
    val records: StateFlow<List<ClinicalRecord>> = _records.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredRecords = MutableStateFlow<List<ClinicalRecord>>(emptyList())
    val filteredRecords: StateFlow<List<ClinicalRecord>> = _filteredRecords.asStateFlow()
    
    init {
        loadRecords()
    }
    
    fun loadRecords() {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val result = repository.getCollection("clinical_records")
                if (result.isSuccess) {
                    val recordsData = result.getOrNull() ?: emptyList()
                    val records = recordsData.mapNotNull { data ->
                        try {
                            ClinicalRecord(
                                id = data["id"] as? String ?: "",
                                patientId = data["patientId"] as? String ?: "",
                                patientName = data["patientName"] as? String ?: "",
                                ownerId = data["ownerId"] as? String ?: "",
                                ownerName = data["ownerName"] as? String ?: "",
                                vetId = data["vetId"] as? String ?: "",
                                vetName = data["vetName"] as? String ?: "",
                                date = data["date"] as? Timestamp ?: Timestamp.now(),
                                reason = data["reason"] as? String ?: "",
                                anamnesis = data["anamnesis"] as? String ?: "",
                                physicalExam = PhysicalExam(
                                    temperature = (data["physicalExam"] as? Map<String, Any>)?.get("temperature") as? Double ?: 0.0,
                                    heartRate = ((data["physicalExam"] as? Map<String, Any>)?.get("heartRate") as? Number)?.toInt() ?: 0,
                                    respiratoryRate = ((data["physicalExam"] as? Map<String, Any>)?.get("respiratoryRate") as? Number)?.toInt() ?: 0,
                                    weight = (data["physicalExam"] as? Map<String, Any>)?.get("weight") as? Double ?: 0.0,
                                    generalAppearance = (data["physicalExam"] as? Map<String, Any>)?.get("generalAppearance") as? String ?: "",
                                    cardiovascular = (data["physicalExam"] as? Map<String, Any>)?.get("cardiovascular") as? String ?: "",
                                    respiratory = (data["physicalExam"] as? Map<String, Any>)?.get("respiratory") as? String ?: "",
                                    digestive = (data["physicalExam"] as? Map<String, Any>)?.get("digestive") as? String ?: "",
                                    neurological = (data["physicalExam"] as? Map<String, Any>)?.get("neurological") as? String ?: "",
                                    musculoskeletal = (data["physicalExam"] as? Map<String, Any>)?.get("musculoskeletal") as? String ?: "",
                                    skin = (data["physicalExam"] as? Map<String, Any>)?.get("skin") as? String ?: "",
                                    eyes = (data["physicalExam"] as? Map<String, Any>)?.get("eyes") as? String ?: "",
                                    ears = (data["physicalExam"] as? Map<String, Any>)?.get("ears") as? String ?: "",
                                    mouth = (data["physicalExam"] as? Map<String, Any>)?.get("mouth") as? String ?: ""
                                ),
                                problems = (data["problems"] as? List<String>) ?: emptyList(),
                                diagnoses = (data["diagnoses"] as? List<String>) ?: emptyList(),
                                treatmentPlan = data["treatmentPlan"] as? String ?: "",
                                prognosis = data["prognosis"] as? String ?: "",
                                evolution = data["evolution"] as? String ?: "",
                                attachments = (data["attachments"] as? List<String>) ?: emptyList()
                            )
                        } catch (e: Exception) {
                            Log.e("NEXOGO_HISTORY", "Error parseando historial: ${e.message}")
                            null
                        }
                    }
                    _records.value = records
                    _filteredRecords.value = records
                    _message.value = "Historiales cargados exitosamente"
                    Log.d("NEXOGO_HISTORY", "Historiales cargados: ${records.size}")
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_HISTORY", "Error cargando historiales: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_HISTORY", "Excepción cargando historiales: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createRecord(
        patientId: String,
        patientName: String,
        ownerId: String,
        ownerName: String,
        vetId: String,
        vetName: String,
        reason: String,
        anamnesis: String,
        physicalExam: PhysicalExam,
        problems: List<String>,
        diagnoses: List<String>,
        treatmentPlan: String,
        prognosis: String,
        evolution: String,
        attachments: List<Uri>
    ) {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val recordId = repository.generateId()
                var attachmentUrls = emptyList<String>()
                
                // Subir archivos adjuntos
                if (attachments.isNotEmpty()) {
                    attachmentUrls = attachments.mapNotNull { uri ->
                        val filePath = "medical_records/$recordId/files/attachment_${System.currentTimeMillis()}.jpg"
                        val uploadResult = repository.uploadFile(filePath, uri)
                        if (uploadResult.isSuccess) {
                            uploadResult.getOrNull()
                        } else {
                            Log.e("NEXOGO_HISTORY", "Error subiendo archivo: ${uploadResult.exceptionOrNull()?.message}")
                            null
                        }
                    }
                }
                
                val record = ClinicalRecord(
                    id = recordId,
                    patientId = patientId,
                    patientName = patientName,
                    ownerId = ownerId,
                    ownerName = ownerName,
                    vetId = vetId,
                    vetName = vetName,
                    reason = reason,
                    anamnesis = anamnesis,
                    physicalExam = physicalExam,
                    problems = problems,
                    diagnoses = diagnoses,
                    treatmentPlan = treatmentPlan,
                    prognosis = prognosis,
                    evolution = evolution,
                    attachments = attachmentUrls
                )
                
                val recordData = mapOf(
                    "id" to record.id,
                    "patientId" to record.patientId,
                    "patientName" to record.patientName,
                    "ownerId" to record.ownerId,
                    "ownerName" to record.ownerName,
                    "vetId" to record.vetId,
                    "vetName" to record.vetName,
                    "date" to record.date,
                    "reason" to record.reason,
                    "anamnesis" to record.anamnesis,
                    "physicalExam" to mapOf(
                        "temperature" to record.physicalExam.temperature,
                        "heartRate" to record.physicalExam.heartRate,
                        "respiratoryRate" to record.physicalExam.respiratoryRate,
                        "weight" to record.physicalExam.weight,
                        "generalAppearance" to record.physicalExam.generalAppearance,
                        "cardiovascular" to record.physicalExam.cardiovascular,
                        "respiratory" to record.physicalExam.respiratory,
                        "digestive" to record.physicalExam.digestive,
                        "neurological" to record.physicalExam.neurological,
                        "musculoskeletal" to record.physicalExam.musculoskeletal,
                        "skin" to record.physicalExam.skin,
                        "eyes" to record.physicalExam.eyes,
                        "ears" to record.physicalExam.ears,
                        "mouth" to record.physicalExam.mouth
                    ),
                    "problems" to record.problems,
                    "diagnoses" to record.diagnoses,
                    "treatmentPlan" to record.treatmentPlan,
                    "prognosis" to record.prognosis,
                    "evolution" to record.evolution,
                    "attachments" to record.attachments,
                    "createdAt" to repository.getCurrentTimestamp(),
                    "updatedAt" to repository.getCurrentTimestamp()
                )
                
                val result = repository.createDocument("clinical_records", recordId, recordData)
                if (result.isSuccess) {
                    _message.value = "Historial clínico creado exitosamente"
                    Log.d("NEXOGO_HISTORY", "Historial creado: $recordId")
                    // Recargar datos desde Firebase para asegurar sincronización
                    loadRecords()
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_HISTORY", "Error creando historial: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_HISTORY", "Excepción creando historial: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateRecord(
        recordId: String,
        reason: String,
        anamnesis: String,
        physicalExam: PhysicalExam,
        problems: List<String>,
        diagnoses: List<String>,
        treatmentPlan: String,
        prognosis: String,
        evolution: String,
        newAttachments: List<Uri>
    ) {
        _isLoading.value = true
        _message.value = ""
        
        viewModelScope.launch {
            try {
                val existingRecord = _records.value.find { it.id == recordId }
                var attachmentUrls = existingRecord?.attachments ?: emptyList()
                
                // Subir nuevos archivos adjuntos
                if (newAttachments.isNotEmpty()) {
                    val newUrls = newAttachments.mapNotNull { uri ->
                        val filePath = "medical_records/$recordId/files/attachment_${System.currentTimeMillis()}.jpg"
                        val uploadResult = repository.uploadFile(filePath, uri)
                        if (uploadResult.isSuccess) {
                            uploadResult.getOrNull()
                        } else {
                            Log.e("NEXOGO_HISTORY", "Error subiendo archivo: ${uploadResult.exceptionOrNull()?.message}")
                            null
                        }
                    }
                    attachmentUrls = attachmentUrls + newUrls
                }
                
                val updateData = mapOf(
                    "reason" to reason,
                    "anamnesis" to anamnesis,
                    "physicalExam" to mapOf(
                        "temperature" to physicalExam.temperature,
                        "heartRate" to physicalExam.heartRate,
                        "respiratoryRate" to physicalExam.respiratoryRate,
                        "weight" to physicalExam.weight,
                        "generalAppearance" to physicalExam.generalAppearance,
                        "cardiovascular" to physicalExam.cardiovascular,
                        "respiratory" to physicalExam.respiratory,
                        "digestive" to physicalExam.digestive,
                        "neurological" to physicalExam.neurological,
                        "musculoskeletal" to physicalExam.musculoskeletal,
                        "skin" to physicalExam.skin,
                        "eyes" to physicalExam.eyes,
                        "ears" to physicalExam.ears,
                        "mouth" to physicalExam.mouth
                    ),
                    "problems" to problems,
                    "diagnoses" to diagnoses,
                    "treatmentPlan" to treatmentPlan,
                    "prognosis" to prognosis,
                    "evolution" to evolution,
                    "attachments" to attachmentUrls,
                    "updatedAt" to repository.getCurrentTimestamp()
                )
                
                val result = repository.updateDocument("clinical_records", recordId, updateData)
                if (result.isSuccess) {
                    _message.value = "Historial clínico actualizado exitosamente"
                    Log.d("NEXOGO_HISTORY", "Historial actualizado: $recordId")
                    // Recargar datos desde Firebase para asegurar sincronización
                    loadRecords()
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_HISTORY", "Error actualizando historial: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_HISTORY", "Excepción actualizando historial: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteDocument("clinical_records", recordId)
                if (result.isSuccess) {
                    _message.value = "Historial clínico eliminado exitosamente"
                    Log.d("NEXOGO_HISTORY", "Historial eliminado: $recordId")
                    // Recargar datos desde Firebase para asegurar sincronización
                    loadRecords()
                } else {
                    _message.value = "Error: ${result.exceptionOrNull()?.message}"
                    Log.e("NEXOGO_HISTORY", "Error eliminando historial: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("NEXOGO_HISTORY", "Excepción eliminando historial: ${e.message}")
            }
        }
    }
    
    fun searchRecords(query: String) {
        _searchQuery.value = query
        
        if (query.isEmpty()) {
            _filteredRecords.value = _records.value
        } else {
            val filtered = _records.value.filter { record ->
                record.patientName.contains(query, ignoreCase = true) ||
                record.ownerName.contains(query, ignoreCase = true) ||
                record.reason.contains(query, ignoreCase = true) ||
                record.diagnoses.any { it.contains(query, ignoreCase = true) }
            }
            _filteredRecords.value = filtered
        }
    }
    
    fun getRecordsByPatient(patientId: String): List<ClinicalRecord> {
        return _records.value.filter { it.patientId == patientId }
    }
    
    fun generatePDF(recordId: String): String {
        // TODO: Implementar generación de PDF
        return "PDF generado para historial: $recordId"
    }
    
    fun clearMessage() {
        _message.value = ""
    }
}
