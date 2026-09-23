package com.example.nexogo.platform.documents.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.platform.company.session.CompanySessionManager
import com.example.nexogo.platform.documents.data.DocumentRepository
import com.example.nexogo.platform.documents.model.Document
import com.example.nexogo.platform.documents.model.DocumentEntityType
import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.usage.UsageAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DocumentsUiState(
    val companyId: String? = null,
    val documents: List<Document> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

/**
 * Documents cutover ViewModel — list + upload within active company.
 */
class DocumentViewModel(
    private val repository: DocumentRepository = DocumentRepository(),
    private val sessionManager: CompanySessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()

    private fun resolveCompanyId(explicit: String? = null): String? {
        explicit?.takeIf { it.isNotBlank() }?.let { return it }
        sessionManager?.activeCompanyId?.takeIf { it.isNotBlank() }?.let { return it }
        return TenantContext.companyId
    }

    fun bindCompany(companyId: String) {
        _uiState.update { it.copy(companyId = companyId, error = null) }
        loadDocuments(companyId)
    }

    fun loadDocuments(companyId: String? = null) {
        val cid = resolveCompanyId(companyId)
        if (cid.isNullOrBlank()) {
            _uiState.update {
                it.copy(error = "No hay empresa activa — Documents requiere Company", isLoading = false)
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, companyId = cid, error = null) }
            val result = repository.listDocuments(cid)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(documents = result.getOrThrow(), isLoading = false, message = "Documentos cargados")
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al cargar documentos"
                    )
                }
            }
        }
    }

    fun uploadFromUri(
        context: Context,
        uri: Uri,
        fileName: String,
        mimeType: String,
        createdBy: String,
        linkEntityType: DocumentEntityType? = null,
        linkEntityId: String? = null
    ) {
        val cid = resolveCompanyId()
        if (cid.isNullOrBlank()) {
            _uiState.update { it.copy(error = "No hay empresa activa") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null, message = null) }
            try {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw IllegalStateException("No se pudo leer el archivo")
                }
                val result = repository.uploadAndRegister(
                    companyId = cid,
                    fileName = fileName.ifBlank { "document.bin" },
                    mimeType = mimeType,
                    bytes = bytes,
                    createdBy = createdBy,
                    displayName = fileName.substringBeforeLast('.').ifBlank { fileName },
                    linkEntityType = linkEntityType,
                    linkEntityId = linkEntityId
                )
                if (result.isSuccess) {
                    val doc = result.getOrThrow()
                    UsageAnalytics.documentUploaded(
                        userId = createdBy,
                        documentId = doc.id,
                        companyId = cid,
                        metadata = mapOf(
                            "fileName" to fileName,
                            "mimeType" to mimeType,
                            "size" to bytes.size.toLong()
                        )
                    )
                    _uiState.update {
                        it.copy(isUploading = false, message = "Documento subido: ${doc.name}")
                    }
                    loadDocuments(cid)
                } else {
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            error = result.exceptionOrNull()?.message ?: "Error al subir"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isUploading = false, error = e.message ?: "Error al subir")
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}
