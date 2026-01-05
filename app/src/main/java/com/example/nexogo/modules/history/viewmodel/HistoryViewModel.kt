package com.example.nexogo.modules.history.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.example.nexogo.modules.history.models.ClinicalRecord
import com.example.nexogo.modules.history.models.HistoryUiState
import com.example.nexogo.modules.history.repo.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el módulo de historial clínico
 * Maneja el estado de la UI y las operaciones CRUD
 */
class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {
    companion object {
        private const val TAG = "NEXOGO_HISTORY"
    }

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Carga los historiales clínicos de un propietario
     */
    fun loadRecords(ownerId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando historiales para propietario: $ownerId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                historyRepository.getRecordsByOwner(ownerId).collect { records ->
                    _uiState.value = _uiState.value.copy(
                        records = records,
                        isLoading = false
                    )
                    Log.d(TAG, "Historiales cargados: ${records.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando historiales: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error cargando historiales: ${e.message}"
                )
            }
        }
    }

    /**
     * Carga un historial clínico específico
     */
    fun loadRecord(recordId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cargando historial: $recordId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val record = historyRepository.getRecordById(recordId)
                _uiState.value = _uiState.value.copy(
                    currentRecord = record,
                    isLoading = false
                )
                Log.d(TAG, "Historial cargado: ${record?.petName}")
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando historial: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error cargando historial: ${e.message}"
                )
            }
        }
    }

    /**
     * Crea un nuevo historial clínico
     */
    fun createRecord(record: ClinicalRecord) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Creando historial para mascota: ${record.petName}")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = historyRepository.createRecord(record)
                if (result.isSuccess) {
                    val recordId = result.getOrNull() ?: ""
                    Log.d(TAG, "Historial creado exitosamente: $recordId")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    // Recargar la lista de historiales
                    loadRecords(record.ownerId)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "Error creando historial: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error creando historial: $error"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creando historial: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error creando historial: ${e.message}"
                )
            }
        }
    }

    /**
     * Actualiza un historial clínico existente
     */
    fun updateRecord(recordId: String, record: ClinicalRecord) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Actualizando historial: $recordId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = historyRepository.updateRecord(recordId, record)
                if (result.isSuccess) {
                    Log.d(TAG, "Historial actualizado exitosamente: $recordId")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    // Recargar el historial actual
                    loadRecord(recordId)
                    // Recargar la lista de historiales
                    loadRecords(record.ownerId)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "Error actualizando historial: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error actualizando historial: $error"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando historial: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error actualizando historial: ${e.message}"
                )
            }
        }
    }

    /**
     * Elimina un historial clínico
     */
    fun deleteRecord(recordId: String, ownerId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Eliminando historial: $recordId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val result = historyRepository.deleteRecord(recordId)
                if (result.isSuccess) {
                    Log.d(TAG, "Historial eliminado exitosamente: $recordId")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                    // Recargar la lista de historiales
                    loadRecords(ownerId)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "Error eliminando historial: $error")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error eliminando historial: $error"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando historial: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error eliminando historial: ${e.message}"
                )
            }
        }
    }

    /**
     * Sube un archivo adjunto
     */
    fun uploadAttachment(
        recordId: String,
        fileUri: Uri,
        fileName: String,
        onProgress: (Float) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Subiendo archivo: $fileName para historial: $recordId")
                _uiState.value = _uiState.value.copy(isUploading = true, uploadProgress = 0f)
                
                val result = historyRepository.uploadAttachment(recordId, fileUri, fileName) { progress ->
                    _uiState.value = _uiState.value.copy(uploadProgress = progress)
                    onProgress(progress)
                }
                
                if (result.isSuccess) {
                    val downloadUrl = result.getOrNull() ?: ""
                    Log.d(TAG, "Archivo subido exitosamente: $downloadUrl")
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        uploadProgress = 1f,
                        error = null
                    )
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Error desconocido"
                    Log.e(TAG, "Error subiendo archivo: $error")
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        uploadProgress = 0f,
                        error = "Error subiendo archivo: $error"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error subiendo archivo: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    uploadProgress = 0f,
                    error = "Error subiendo archivo: ${e.message}"
                )
            }
        }
    }

    /**
     * Genera y sube un PDF del historial
     */
    fun generatePdf(recordId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Generando PDF para historial: $recordId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val record = _uiState.value.currentRecord
                if (record != null) {
                    // TODO: Implementar generación de PDF
                    // val pdfBytes = PdfGenerator.generatePdf(record)
                    // val result = historyRepository.uploadPdf(recordId, pdfBytes) { progress ->
                    //     _uiState.value = _uiState.value.copy(uploadProgress = progress)
                    // }
                    
                    // Por ahora, simulamos la generación
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pdfUrl = "https://example.com/pdf/$recordId.pdf"
                    )
                    Log.d(TAG, "PDF generado exitosamente")
                } else {
                    Log.e(TAG, "No hay historial cargado para generar PDF")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No hay historial cargado para generar PDF"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generando PDF: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error generando PDF: ${e.message}"
                )
            }
        }
    }

    /**
     * Limpia el error actual
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Limpia el historial actual
     */
    fun clearCurrentRecord() {
        _uiState.value = _uiState.value.copy(currentRecord = null)
    }

    /**
     * Limpia la URL del PDF
     */
    fun clearPdfUrl() {
        _uiState.value = _uiState.value.copy(pdfUrl = null)
    }
}
