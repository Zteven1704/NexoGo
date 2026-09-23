package com.example.nexogo.platform.clients.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexogo.platform.clients.data.ClientRepository
import com.example.nexogo.platform.clients.data.PetWithOwner
import com.example.nexogo.platform.clients.model.Client
import com.example.nexogo.platform.clients.model.ClientContact
import com.example.nexogo.platform.clients.model.ClientFactories
import com.example.nexogo.platform.clients.model.ClientModel
import com.example.nexogo.platform.clients.model.ClientType
import com.example.nexogo.platform.company.session.CompanySessionManager
import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.usage.UsageAnalytics
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientsUiState(
    val companyId: String? = null,
    val clients: List<ClientModel> = emptyList(),
    val contacts: List<ClientContact> = emptyList(),
    val selectedClient: ClientModel? = null,
    val selectedContacts: List<ClientContact> = emptyList(),
    val filterType: ClientType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

/**
 * Clients module v1 ViewModel.
 * Requires an active Company (TenantContext / CompanySession).
 * Not wired to UI navigation yet — ready for future screens.
 */
class ClientViewModel(
    private val repository: ClientRepository = ClientRepository(),
    private val sessionManager: CompanySessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientsUiState())
    val uiState: StateFlow<ClientsUiState> = _uiState.asStateFlow()

    private val _clients = MutableStateFlow<List<ClientModel>>(emptyList())
    val clients: StateFlow<List<ClientModel>> = _clients.asStateFlow()

    private var listenJob: Job? = null

    private fun resolveCompanyId(explicit: String? = null): String? {
        explicit?.takeIf { it.isNotBlank() }?.let { return it }
        sessionManager?.activeCompanyId?.takeIf { it.isNotBlank() }?.let { return it }
        return TenantContext.companyId
    }

    fun bindCompany(companyId: String) {
        _uiState.update { it.copy(companyId = companyId, error = null) }
        loadClients(companyId)
        observeClients(companyId)
    }

    fun loadClients(companyId: String? = null) {
        val cid = resolveCompanyId(companyId)
        if (cid.isNullOrBlank()) {
            _uiState.update {
                it.copy(error = "No hay empresa activa — Clients requiere Company", isLoading = false)
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, companyId = cid, error = null) }
            val result = repository.listClients(cid, typeFilter = _uiState.value.filterType)
            if (result.isSuccess) {
                val list = result.getOrThrow()
                _clients.value = list
                _uiState.update {
                    it.copy(clients = list, isLoading = false, message = "Clientes cargados")
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al cargar clientes"
                    )
                }
            }
        }
    }

    fun observeClients(companyId: String? = null) {
        val cid = resolveCompanyId(companyId) ?: return
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            repository.listenClients(cid).collect { list ->
                val filtered = _uiState.value.filterType?.let { type ->
                    list.filter { it.clientType == type }
                } ?: list
                val query = _uiState.value.searchQuery
                val shown = if (query.isBlank()) filtered else filtered.filter { client ->
                    client.displayName.contains(query, ignoreCase = true) ||
                        client.searchTokens.any { it.contains(query.lowercase()) }
                }
                _clients.value = shown
                _uiState.update { it.copy(clients = shown, companyId = cid) }
            }
        }
    }

    fun setTypeFilter(type: ClientType?) {
        _uiState.update { it.copy(filterType = type) }
        loadClients()
    }

    fun search(query: String) {
        val cid = resolveCompanyId() ?: return
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            val result = repository.searchClients(cid, query)
            if (result.isSuccess) {
                val list = result.getOrThrow()
                _clients.value = list
                _uiState.update { it.copy(clients = list) }
            }
        }
    }

    fun selectClient(clientId: String) {
        val cid = resolveCompanyId() ?: return
        viewModelScope.launch {
            val client = repository.getClient(cid, clientId).getOrNull()
            val contacts = if (client != null) {
                repository.listContactsForClient(cid, clientId).getOrElse { emptyList() }
            } else emptyList()
            _uiState.update {
                it.copy(selectedClient = client, selectedContacts = contacts)
            }
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedClient = null, selectedContacts = emptyList()) }
    }

    fun createPerson(
        displayName: String,
        createdBy: String,
        emails: List<String> = emptyList(),
        phones: List<String> = emptyList(),
        profile: Map<String, Any> = emptyMap()
    ) {
        val cid = resolveCompanyId()
        if (cid.isNullOrBlank()) {
            _uiState.update { it.copy(error = "No hay empresa activa") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.createClient(
                ClientFactories.person(cid, displayName, createdBy, emails, phones, profile)
            )
            handleWriteResult(result, "Persona creada", trackCreate = true, createdBy = createdBy)
        }
    }

    fun createOrganization(
        displayName: String,
        createdBy: String,
        taxId: String = "",
        emails: List<String> = emptyList(),
        phones: List<String> = emptyList()
    ) {
        val cid = resolveCompanyId()
        if (cid.isNullOrBlank()) {
            _uiState.update { it.copy(error = "No hay empresa activa") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.createClient(
                ClientFactories.organization(cid, displayName, taxId, createdBy, emails, phones)
            )
            handleWriteResult(result, "Empresa cliente creada", trackCreate = true, createdBy = createdBy)
        }
    }

    fun createPetWithOwner(
        petName: String,
        ownerName: String,
        createdBy: String,
        species: String = "",
        breed: String = "",
        ownerPhone: String = "",
        ownerEmail: String = ""
    ) {
        val cid = resolveCompanyId()
        if (cid.isNullOrBlank()) {
            _uiState.update { it.copy(error = "No hay empresa activa") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.createPetWithOwner(
                companyId = cid,
                petName = petName,
                ownerName = ownerName,
                createdBy = createdBy,
                species = species,
                breed = breed,
                ownerPhone = ownerPhone,
                ownerEmail = ownerEmail
            )
            if (result.isSuccess) {
                val created: PetWithOwner = result.getOrThrow()
                UsageAnalytics.clientCreated(
                    userId = createdBy,
                    clientId = created.pet.id,
                    companyId = cid,
                    metadata = mapOf("kind" to "pet_with_owner")
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Mascota y propietario creados",
                        selectedClient = created.pet,
                        selectedContacts = listOf(created.owner)
                    )
                }
                loadClients(cid)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al crear mascota"
                    )
                }
            }
        }
    }

    fun saveClient(client: Client) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = if (client.id.isBlank()) {
                repository.createClient(client)
            } else {
                repository.updateClient(client)
            }
            handleWriteResult(result, "Cliente guardado")
        }
    }

    fun archiveClient(clientId: String, updatedBy: String = "") {
        val cid = resolveCompanyId() ?: return
        viewModelScope.launch {
            val result = repository.archiveClient(cid, clientId, updatedBy)
            handleWriteResult(result, "Cliente archivado")
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun handleWriteResult(
        result: Result<Client>,
        successMessage: String,
        trackCreate: Boolean = false,
        createdBy: String = ""
    ) {
        if (result.isSuccess) {
            val client = result.getOrNull()
            if (trackCreate && client != null) {
                UsageAnalytics.clientCreated(
                    userId = createdBy.ifBlank { client.createdBy },
                    clientId = client.id,
                    companyId = client.companyId
                )
            }
            _uiState.update {
                it.copy(isLoading = false, message = successMessage, selectedClient = client)
            }
            loadClients()
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
