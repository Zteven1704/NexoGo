package com.example.nexogo.platform.clients.data

import com.example.nexogo.platform.clients.model.Client
import com.example.nexogo.platform.clients.model.ClientContact
import com.example.nexogo.platform.clients.model.ClientFactories
import com.example.nexogo.platform.clients.model.ClientStatus
import com.example.nexogo.platform.clients.model.ClientType
import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.tenant.data.TenantAwareRepository
import com.example.nexogo.platform.tenant.data.TenantCollections
import com.example.nexogo.platform.tenant.guard.TenantIsolationGuard
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Tenant-scoped Clients + Contacts repository.
 * Path: companies/{companyId}/clients|contacts/{id}
 * Does not use or wrap legacy Patient repositories.
 */
class ClientRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : TenantAwareRepository(firestore) {

    override val collectionName: String = TenantCollections.CLIENTS

    private fun contactsCollection(companyId: String) =
        tenantFirestore.collection(scope(companyId), TenantCollections.CONTACTS)

    private fun contactsDocument(companyId: String, contactId: String) =
        tenantFirestore.document(scope(companyId), TenantCollections.CONTACTS, contactId)

    private fun requireTenant(companyId: String?): String =
        TenantIsolationGuard.requireCompanyId(companyId ?: TenantContext.companyId)

    // region Clients

    suspend fun createClient(client: Client): Result<Client> {
        return try {
            val companyId = requireTenant(client.companyId)
            require(client.displayName.isNotBlank()) { "displayName required" }
            val now = Timestamp.now()
            val id = client.id.ifBlank { newId("cli") }
            val toSave = client.copy(
                id = id,
                companyId = companyId,
                searchTokens = if (client.searchTokens.isEmpty()) {
                    ClientFactories.buildSearchTokens(
                        client.displayName,
                        client.emails,
                        client.phones,
                        listOf(client.taxId)
                    )
                } else client.searchTokens,
                createdAt = if (client.createdAt.seconds == 0L) now else client.createdAt,
                updatedAt = now
            )
            setEntity(toSave, merge = false)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateClient(client: Client): Result<Client> {
        return try {
            val companyId = requireTenant(client.companyId)
            require(client.id.isNotBlank()) { "client id required" }
            val toSave = client.copy(
                companyId = companyId,
                updatedAt = Timestamp.now(),
                searchTokens = ClientFactories.buildSearchTokens(
                    client.displayName,
                    client.emails,
                    client.phones,
                    listOf(client.taxId)
                )
            )
            setEntity(toSave, merge = true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClient(companyId: String, clientId: String): Result<Client?> {
        return try {
            val cid = requireTenant(companyId)
            val snap = document(cid, clientId).get().await()
            Result.success(snap.toObject(Client::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listClients(
        companyId: String,
        includeArchived: Boolean = false,
        typeFilter: ClientType? = null
    ): Result<List<Client>> {
        return try {
            val cid = requireTenant(companyId)
            var query: Query = listQuery(cid)
            if (typeFilter != null) {
                query = query.whereEqualTo("clientType", typeFilter.name)
            }
            val snap = query.get().await()
            val clients = snap.documents.mapNotNull { it.toObject(Client::class.java) }
                .filter { includeArchived || it.status != ClientStatus.ARCHIVED }
                .sortedBy { it.displayName.lowercase() }
            Result.success(clients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchClients(companyId: String, queryText: String): Result<List<Client>> {
        return try {
            val cid = requireTenant(companyId)
            val needle = queryText.trim().lowercase()
            if (needle.isBlank()) return listClients(cid)
            val all = listClients(cid).getOrThrow()
            Result.success(
                all.filter { client ->
                    client.searchTokens.any { it.contains(needle) } ||
                        client.displayName.lowercase().contains(needle) ||
                        client.taxId.lowercase().contains(needle)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun archiveClient(companyId: String, clientId: String, updatedBy: String = ""): Result<Client> {
        return try {
            val existing = getClient(companyId, clientId).getOrThrow()
                ?: return Result.failure(IllegalArgumentException("Client not found"))
            updateClient(
                existing.copy(
                    status = ClientStatus.ARCHIVED,
                    archivedAt = Timestamp.now(),
                    updatedBy = updatedBy
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenClients(companyId: String): Flow<List<Client>> = callbackFlow {
        val cid = requireTenant(companyId)
        val registration = listQuery(cid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents
                ?.mapNotNull { it.toObject(Client::class.java) }
                ?.filter { it.status != ClientStatus.ARCHIVED }
                ?.sortedBy { it.displayName.lowercase() }
                .orEmpty()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    // endregion

    // region Contacts (owners / guardians / billing)

    suspend fun createContact(contact: ClientContact): Result<ClientContact> {
        return try {
            val companyId = requireTenant(contact.companyId)
            require(contact.fullName.isNotBlank()) { "fullName required" }
            val now = Timestamp.now()
            val id = contact.id.ifBlank { newId("ctc") }
            val toSave = contact.copy(
                id = id,
                companyId = companyId,
                createdAt = if (contact.createdAt.seconds == 0L) now else contact.createdAt,
                updatedAt = now
            )
            contactsDocument(companyId, id).set(toSave).await()
            Result.success(toSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateContact(contact: ClientContact): Result<ClientContact> {
        return try {
            val companyId = requireTenant(contact.companyId)
            require(contact.id.isNotBlank())
            val toSave = contact.copy(companyId = companyId, updatedAt = Timestamp.now())
            contactsDocument(companyId, toSave.id).set(toSave, SetOptions.merge()).await()
            Result.success(toSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getContact(companyId: String, contactId: String): Result<ClientContact?> {
        return try {
            val cid = requireTenant(companyId)
            val snap = contactsDocument(cid, contactId).get().await()
            Result.success(snap.toObject(ClientContact::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listContactsForClient(companyId: String, clientId: String): Result<List<ClientContact>> {
        return try {
            val cid = requireTenant(companyId)
            val snap = contactsCollection(cid)
                .whereArrayContains("clientIds", clientId)
                .get()
                .await()
            Result.success(
                snap.documents.mapNotNull { it.toObject(ClientContact::class.java) }
                    .sortedByDescending { it.isPrimary }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listContacts(companyId: String): Result<List<ClientContact>> {
        return try {
            val cid = requireTenant(companyId)
            val snap = contactsCollection(cid).get().await()
            Result.success(
                snap.documents.mapNotNull { it.toObject(ClientContact::class.java) }
                    .sortedBy { it.fullName.lowercase() }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a PET client and its OWNER/GUARDIAN contact in one call.
     */
    suspend fun createPetWithOwner(
        companyId: String,
        petName: String,
        ownerName: String,
        createdBy: String,
        species: String = "",
        breed: String = "",
        ownerPhone: String = "",
        ownerEmail: String = ""
    ): Result<PetWithOwner> {
        return try {
            val cid = requireTenant(companyId)
            val pet = createClient(
                ClientFactories.pet(
                    companyId = cid,
                    displayName = petName,
                    createdBy = createdBy,
                    species = species,
                    breed = breed
                )
            ).getOrThrow()

            val owner = createContact(
                ClientFactories.petOwnerContact(
                    companyId = cid,
                    fullName = ownerName,
                    petClientId = pet.id,
                    phone = ownerPhone,
                    email = ownerEmail,
                    createdBy = createdBy
                )
            ).getOrThrow()

            val linked = updateClient(
                pet.copy(primaryContactId = owner.id, updatedBy = createdBy)
            ).getOrThrow()

            Result.success(PetWithOwner(pet = linked, owner = owner))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // endregion

    private fun newId(prefix: String): String =
        "${prefix}_${UUID.randomUUID().toString().replace("-", "").take(20)}"
}

data class PetWithOwner(
    val pet: Client,
    val owner: ClientContact
)
