package com.example.nexogo.platform.records.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.platform.company.session.CompanySessionManager
import com.example.nexogo.platform.records.data.RecordRepository
import com.example.nexogo.platform.records.model.Record
import com.example.nexogo.platform.records.model.RecordFactories
import com.example.nexogo.platform.records.model.RecordModel
import com.example.nexogo.platform.records.model.RecordParty
import com.example.nexogo.platform.records.model.RecordSection
import com.example.nexogo.platform.records.model.RecordType
import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.usage.UsageAnalytics
import com.google.firebase.Timestamp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordsUiState(
    val companyId: String? = null,
    val clientIdFilter: String? = null,
    val records: List<RecordModel> = emptyList(),
    val selectedRecord: RecordModel? = null,
    val filterType: RecordType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

/**
 * Records (Expedientes) module v1 ViewModel.
 * Requires active Company. Not wired to UI navigation yet.
 */
class RecordViewModel(
    private val repository: RecordRepository = RecordRepository(),
    private val sessionManager: CompanySessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordsUiState())
    val uiState: StateFlow<RecordsUiState> = _uiState.asStateFlow()

    private val _records = MutableStateFlow<List<RecordModel>>(emptyList())
    val records: StateFlow<List<RecordModel>> = _records.asStateFlow()

    private var listenJob: Job? = null

    private fun resolveCompanyId(explicit: String? = null): String? {
        explicit?.takeIf { it.isNotBlank() }?.let { return it }
        sessionManager?.activeCompanyId?.takeIf { it.isNotBlank() }?.let { return it }
        return TenantContext.companyId
    }

    fun bindCompany(companyId: String, clientId: String? = null) {
        _uiState.update {
            it.copy(companyId = companyId, clientIdFilter = clientId, error = null)
        }
        loadRecords(companyId, clientId)
        observeRecords(companyId, clientId)
    }

    fun loadRecords(companyId: String? = null, clientId: String? = null) {
        val cid = resolveCompanyId(companyId)
        if (cid.isNullOrBlank()) {
            _uiState.update {
                it.copy(error = "No hay empresa activa — Records requiere Company", isLoading = false)
            }
            return
        }
        val clientFilter = clientId ?: _uiState.value.clientIdFilter
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, companyId = cid, error = null) }
            val result = repository.listRecords(
                companyId = cid,
                clientId = clientFilter,
                typeFilter = _uiState.value.filterType
            )
            if (result.isSuccess) {
                val list = result.getOrThrow()
                _records.value = list
                _uiState.update {
                    it.copy(
                        records = list,
                        clientIdFilter = clientFilter,
                        isLoading = false,
                        message = "Expedientes cargados"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al cargar expedientes"
                    )
                }
            }
        }
    }

    fun observeRecords(companyId: String? = null, clientId: String? = null) {
        val cid = resolveCompanyId(companyId) ?: return
        val clientFilter = clientId ?: _uiState.value.clientIdFilter
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            repository.listenRecords(cid, clientFilter).collect { list ->
                val filtered = _uiState.value.filterType?.let { type ->
                    list.filter { it.recordType == type }
                } ?: list
                val query = _uiState.value.searchQuery
                val shown = if (query.isBlank()) filtered else filtered.filter { record ->
                    record.title.contains(query, ignoreCase = true) ||
                        record.searchTokens.any { it.contains(query.lowercase()) }
                }
                _records.value = shown
                _uiState.update { it.copy(records = shown, companyId = cid) }
            }
        }
    }

    fun setTypeFilter(type: RecordType?) {
        _uiState.update { it.copy(filterType = type) }
        loadRecords()
    }

    fun setClientFilter(clientId: String?) {
        _uiState.update { it.copy(clientIdFilter = clientId) }
        loadRecords(clientId = clientId)
        observeRecords(clientId = clientId)
    }

    fun search(query: String) {
        val cid = resolveCompanyId() ?: return
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            val result = repository.searchRecords(cid, query)
            if (result.isSuccess) {
                val list = result.getOrThrow()
                _records.value = list
                _uiState.update { it.copy(records = list) }
            }
        }
    }

    fun selectRecord(recordId: String) {
        val cid = resolveCompanyId() ?: return
        viewModelScope.launch {
            val record = repository.getRecord(cid, recordId).getOrNull()
            _uiState.update { it.copy(selectedRecord = record) }
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedRecord = null) }
    }

    fun createClinicalHistory(
        clientId: String,
        title: String,
        authoredBy: String,
        summary: String = "",
        sections: List<RecordSection> = emptyList(),
        industryPayload: Map<String, Any> = emptyMap()
    ) {
        createFromFactory {
            RecordFactories.clinicalHistory(
                companyId = it,
                clientId = clientId,
                title = title,
                authoredBy = authoredBy,
                summary = summary,
                sections = sections,
                industryPayload = industryPayload
            )
        }
    }

    fun createContract(
        clientId: String,
        title: String,
        authoredBy: String,
        validFrom: Timestamp? = null,
        validTo: Timestamp? = null,
        parties: List<RecordParty> = emptyList(),
        summary: String = ""
    ) {
        createFromFactory {
            RecordFactories.contract(
                companyId = it,
                clientId = clientId,
                title = title,
                authoredBy = authoredBy,
                validFrom = validFrom,
                validTo = validTo,
                parties = parties,
                summary = summary
            )
        }
    }

    fun createInvoice(
        clientId: String,
        title: String,
        authoredBy: String,
        amount: Double? = null,
        currency: String = "COP",
        saleId: String? = null,
        folio: String = "",
        summary: String = ""
    ) {
        createFromFactory {
            RecordFactories.invoice(
                companyId = it,
                clientId = clientId,
                title = title,
                authoredBy = authoredBy,
                amount = amount,
                currency = currency,
                saleId = saleId,
                folio = folio,
                summary = summary
            )
        }
    }

    fun createReport(
        clientId: String,
        title: String,
        authoredBy: String,
        summary: String = "",
        findings: String = "",
        conclusions: String = ""
    ) {
        createFromFactory {
            RecordFactories.report(
                companyId = it,
                clientId = clientId,
                title = title,
                authoredBy = authoredBy,
                summary = summary,
                findings = findings,
                conclusions = conclusions
            )
        }
    }

    fun createTechnicalDoc(
        clientId: String,
        title: String,
        authoredBy: String,
        summary: String = "",
        docVersion: String = "1.0",
        systemScope: String = ""
    ) {
        createFromFactory {
            RecordFactories.technicalDoc(
                companyId = it,
                clientId = clientId,
                title = title,
                authoredBy = authoredBy,
                summary = summary,
                docVersion = docVersion,
                systemScope = systemScope
            )
        }
    }

    fun saveRecord(record: Record) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = if (record.id.isBlank()) {
                repository.createRecord(record)
            } else {
                repository.updateRecord(record)
            }
            handleWriteResult(result, "Expediente guardado")
        }
    }

    fun finalizeRecord(recordId: String, finalizedBy: String) {
        val cid = resolveCompanyId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.finalizeRecord(cid, recordId, finalizedBy)
            handleWriteResult(result, "Expediente finalizado")
        }
    }

    fun archiveRecord(recordId: String, updatedBy: String = "") {
        val cid = resolveCompanyId() ?: return
        viewModelScope.launch {
            val result = repository.archiveRecord(cid, recordId, updatedBy)
            handleWriteResult(result, "Expediente archivado")
        }
    }

    fun attachDocument(recordId: String, documentId: String, setAsPrimary: Boolean = false, updatedBy: String = "") {
        val cid = resolveCompanyId() ?: return
        viewModelScope.launch {
            val result = repository.attachDocumentId(cid, recordId, documentId, setAsPrimary, updatedBy)
            handleWriteResult(result, "Documento vinculado")
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun createFromFactory(factory: (companyId: String) -> Record) {
        val cid = resolveCompanyId()
        if (cid.isNullOrBlank()) {
            _uiState.update { it.copy(error = "No hay empresa activa") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.createRecord(factory(cid))
            handleWriteResult(result, "Expediente creado", trackCreate = true)
        }
    }

    private fun handleWriteResult(result: Result<Record>, successMessage: String, trackCreate: Boolean = false) {
        if (result.isSuccess) {
            val record = result.getOrNull()
            if (trackCreate && record != null) {
                UsageAnalytics.recordCreated(
                    userId = record.authoredBy.ifBlank { record.createdBy },
                    recordId = record.id,
                    companyId = record.companyId
                )
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = successMessage,
                    selectedRecord = record
                )
            }
            loadRecords()
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Error de escritura"
                )
            }
        }
    }

    override fun onCleared() {
        listenJob?.cancel()
        super.onCleared()
    }
}
