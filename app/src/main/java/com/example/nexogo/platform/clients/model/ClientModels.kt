package com.example.nexogo.platform.clients.model

import com.example.nexogo.platform.tenant.model.TenantAwareEntity
import com.google.firebase.Timestamp

/** Alias requested by Clients module v1. */
typealias ClientModel = Client

/**
 * Core Client entity — multi-industry, always owned by a Company.
 * Does NOT extend or reuse legacy Patient / SimplePatient models.
 *
 * Path: companies/{companyId}/clients/{clientId}
 */
data class Client(
    override val id: String = "",
    override val companyId: String = "",
    val displayName: String = "",
    val clientType: ClientType = ClientType.PERSON,
    val status: ClientStatus = ClientStatus.ACTIVE,
    val primaryContactId: String? = null,
    val emails: List<String> = emptyList(),
    val phones: List<String> = emptyList(),
    val addresses: List<ClientAddress> = emptyList(),
    val taxId: String = "",
    val tags: List<String> = emptyList(),
    val media: ClientMedia = ClientMedia(),
    /**
     * Industry-specific fields (species, breed, accountCode, …).
     * Validated by pack at app layer — never hardcoded as core properties.
     */
    val profile: Map<String, Any> = emptyMap(),
    val searchTokens: List<String> = emptyList(),
    /** Portal user (CLIENT role) linked to this client, if any. */
    val linkedUserId: String? = null,
    val notes: String = "",
    override val createdAt: Timestamp = Timestamp.now(),
    override val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",
    val updatedBy: String = "",
    val archivedAt: Timestamp? = null
) : TenantAwareEntity

enum class ClientType {
    /** Persona (médicos, consultora, contacto principal). */
    PERSON,
    /** Organización / empresa / cuenta B2B. */
    ORG,
    /** Mascota (sujeto de servicio veterinario). */
    PET,
    /** Activo (vehículo, máquina) — talleres. */
    ASSET,
    /** Extensión futura por pack. */
    CUSTOM
}

enum class ClientStatus {
    ACTIVE,
    INACTIVE,
    ARCHIVED
}

data class ClientAddress(
    val label: String = "",
    val line1: String = "",
    val line2: String = "",
    val city: String = "",
    val region: String = "",
    val country: String = "",
    val postalCode: String = "",
    val isPrimary: Boolean = false
)

data class ClientMedia(
    val avatarUrl: String = "",
    val avatarDocumentId: String = ""
)

/**
 * Contact linked to one or more Clients (e.g. pet owner / guardian / billing).
 * Path: companies/{companyId}/contacts/{contactId}
 */
data class ClientContact(
    override val id: String = "",
    override val companyId: String = "",
    val clientIds: List<String> = emptyList(),
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val role: ContactRole = ContactRole.OTHER,
    val isPrimary: Boolean = false,
    val notes: String = "",
    override val createdAt: Timestamp = Timestamp.now(),
    override val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = ""
) : TenantAwareEntity

enum class ContactRole {
    OWNER,
    GUARDIAN,
    BILLING,
    EMERGENCY,
    BUYER,
    DRIVER,
    OTHER
}

/**
 * Well-known profile keys for packs (helpers only — not core fields).
 */
object ClientProfileKeys {
    // veterinary / PET
    const val SPECIES = "species"
    const val BREED = "breed"
    const val SEX = "sex"
    const val BIRTH_DATE = "birthDate"
    const val WEIGHT_KG = "weightKg"
    const val CHIP_ID = "chipId"
    const val NEUTERED = "neutered"
    const val COLOR = "color"

    // clinic / PERSON (paciente humano)
    const val DOCUMENT_ID = "documentId"
    const val BLOOD_TYPE = "bloodType"
    const val ALLERGIES = "allergies"
    const val INSURANCE_PROVIDER = "insuranceProvider"
    const val INSURANCE_NUMBER = "insuranceNumber"

    // ORG / empresa
    const val ACCOUNT_CODE = "accountCode"
    const val LEGAL_NAME = "legalName"
    const val INDUSTRY = "industry"
    const val CREDIT_LIMIT = "creditLimit"
}

/**
 * Factory helpers for common vertical shapes (v1).
 */
object ClientFactories {

    fun person(
        companyId: String,
        displayName: String,
        createdBy: String = "",
        emails: List<String> = emptyList(),
        phones: List<String> = emptyList(),
        profile: Map<String, Any> = emptyMap()
    ): Client = Client(
        companyId = companyId,
        displayName = displayName.trim(),
        clientType = ClientType.PERSON,
        emails = emails,
        phones = phones,
        profile = profile,
        createdBy = createdBy,
        updatedBy = createdBy,
        searchTokens = buildSearchTokens(displayName, emails, phones)
    )

    fun organization(
        companyId: String,
        displayName: String,
        taxId: String = "",
        createdBy: String = "",
        emails: List<String> = emptyList(),
        phones: List<String> = emptyList(),
        profile: Map<String, Any> = emptyMap()
    ): Client = Client(
        companyId = companyId,
        displayName = displayName.trim(),
        clientType = ClientType.ORG,
        taxId = taxId,
        emails = emails,
        phones = phones,
        profile = profile,
        createdBy = createdBy,
        updatedBy = createdBy,
        searchTokens = buildSearchTokens(displayName, emails, phones, listOf(taxId))
    )

    /**
     * Pet as service subject; owners/guardians are separate [ClientContact] records.
     */
    fun pet(
        companyId: String,
        displayName: String,
        createdBy: String = "",
        species: String = "",
        breed: String = "",
        profileExtras: Map<String, Any> = emptyMap()
    ): Client {
        val profile = buildMap<String, Any> {
            if (species.isNotBlank()) put(ClientProfileKeys.SPECIES, species)
            if (breed.isNotBlank()) put(ClientProfileKeys.BREED, breed)
            putAll(profileExtras)
        }
        return Client(
            companyId = companyId,
            displayName = displayName.trim(),
            clientType = ClientType.PET,
            profile = profile,
            createdBy = createdBy,
            updatedBy = createdBy,
            searchTokens = buildSearchTokens(displayName, extra = listOf(species, breed))
        )
    }

    fun petOwnerContact(
        companyId: String,
        fullName: String,
        petClientId: String,
        phone: String = "",
        email: String = "",
        role: ContactRole = ContactRole.OWNER,
        createdBy: String = "",
        isPrimary: Boolean = true
    ): ClientContact = ClientContact(
        companyId = companyId,
        clientIds = listOf(petClientId).filter { it.isNotBlank() },
        fullName = fullName.trim(),
        phone = phone,
        email = email,
        role = role,
        isPrimary = isPrimary,
        createdBy = createdBy
    )

    fun buildSearchTokens(
        displayName: String,
        emails: List<String> = emptyList(),
        phones: List<String> = emptyList(),
        extra: List<String> = emptyList()
    ): List<String> {
        return (listOf(displayName) + emails + phones + extra)
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
    }
}
