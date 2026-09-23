package com.example.nexogo.platform.records.model

import com.example.nexogo.platform.tenant.model.TenantAwareEntity
import com.google.firebase.Timestamp

/** Alias requested by Records module v1. */
typealias RecordModel = Record

/**
 * Universal Record (Expediente) — clinical, contracts, invoices, reports, technical docs.
 * Always owned by a Company and linked to a Client.
 * Does NOT reuse legacy ClinicalRecord / medical_records models.
 *
 * Path: companies/{companyId}/records/{recordId}
 * Binary files belong in Documents (referenced via documentIds) — not embedded here.
 */
data class Record(
    override val id: String = "",
    override val companyId: String = "",
    /** Required link to platform Client (not legacy Patient). */
    val clientId: String = "",
    val appointmentId: String? = null,
    val saleId: String? = null,
    val title: String = "",
    val recordType: RecordType = RecordType.NOTE,
    val category: String = "",
    val status: RecordStatus = RecordStatus.DRAFT,
    val summary: String = "",
    val serviceDate: Timestamp? = null,
    val validFrom: Timestamp? = null,
    val validTo: Timestamp? = null,
    val currency: String = "",
    val amount: Double? = null,
    val parties: List<RecordParty> = emptyList(),
    val sections: List<RecordSection> = emptyList(),
    /** Pack-specific structured payload (clinical exam, contract clauses meta, …). */
    val industryPayload: Map<String, Any> = emptyMap(),
    val documentIds: List<String> = emptyList(),
    val primaryDocumentId: String? = null,
    val tags: List<String> = emptyList(),
    val confidentiality: RecordConfidentiality = RecordConfidentiality.STANDARD,
    val authoredBy: String = "",
    val finalizedBy: String? = null,
    val finalizedAt: Timestamp? = null,
    val templateId: String? = null,
    val templateVersion: String? = null,
    val searchTokens: List<String> = emptyList(),
    val legacySource: String? = null,
    override val createdAt: Timestamp = Timestamp.now(),
    override val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",
    val updatedBy: String = "",
    val archivedAt: Timestamp? = null
) : TenantAwareEntity

enum class RecordType {
    /** Historias clínicas (vet / médico). */
    CLINICAL_HISTORY,
    /** Contratos. */
    CONTRACT,
    /** Facturas / comprobantes de expediente (no reemplaza Ventas). */
    INVOICE,
    /** Informes técnicos, periciales, de avance. */
    REPORT,
    /** Documentación técnica (manuales, specs, planos). */
    TECHNICAL_DOC,
    /** Expediente centrado en adjuntos. */
    ATTACHMENT_PACK,
    /** Nota / acta breve. */
    NOTE,
    /** Extensión por pack. */
    CUSTOM
}

enum class RecordStatus {
    DRAFT,
    FINAL,
    ARCHIVED
}

enum class RecordConfidentiality {
    STANDARD,
    SENSITIVE,
    LEGAL_HOLD
}

data class RecordParty(
    val role: String = "",
    val name: String = "",
    val contactId: String? = null,
    val clientId: String? = null
)

data class RecordSection(
    val id: String = "",
    val key: String = "",
    val title: String = "",
    val body: String = "",
    val data: Map<String, Any> = emptyMap()
)

/**
 * Optional template metadata under companies/{companyId}/record_templates/{id}.
 */
data class RecordTemplate(
    override val id: String = "",
    override val companyId: String = "",
    val recordType: RecordType = RecordType.NOTE,
    val name: String = "",
    val version: String = "1",
    val sectionsSchema: List<String> = emptyList(),
    val defaultConfidentiality: RecordConfidentiality = RecordConfidentiality.STANDARD,
    val isActive: Boolean = true,
    override val createdAt: Timestamp = Timestamp.now(),
    override val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = ""
) : TenantAwareEntity

/**
 * Well-known industryPayload keys (helpers — not core fields).
 */
object RecordPayloadKeys {
    // clinical
    const val ANAMNESIS = "anamnesis"
    const val PHYSICAL_EXAM = "physicalExam"
    const val DIAGNOSIS = "diagnosis"
    const val PLAN = "plan"
    const val VITALS = "vitals"

    // contract
    const val CONTRACT_OBJECT = "object"
    const val CLAUSES_REF = "clausesRef"
    const val SIGNED = "signed"

    // invoice
    const val FOLIO = "folio"
    const val ITEMS_REF = "itemsRef"
    const val TAX_TOTAL = "taxTotal"

    // technical / report
    const val FINDINGS = "findings"
    const val CONCLUSIONS = "conclusions"
    const val DOC_VERSION = "docVersion"
    const val SYSTEM_SCOPE = "systemScope"
}

/**
 * Factories for common expediente shapes (v1).
 */
object RecordFactories {

    fun clinicalHistory(
        companyId: String,
        clientId: String,
        title: String,
        authoredBy: String,
        summary: String = "",
        sections: List<RecordSection> = emptyList(),
        industryPayload: Map<String, Any> = emptyMap()
    ): Record = base(
        companyId = companyId,
        clientId = clientId,
        title = title,
        recordType = RecordType.CLINICAL_HISTORY,
        authoredBy = authoredBy,
        summary = summary,
        sections = sections,
        industryPayload = industryPayload,
        confidentiality = RecordConfidentiality.SENSITIVE
    )

    fun contract(
        companyId: String,
        clientId: String,
        title: String,
        authoredBy: String,
        validFrom: Timestamp? = null,
        validTo: Timestamp? = null,
        parties: List<RecordParty> = emptyList(),
        summary: String = ""
    ): Record = base(
        companyId = companyId,
        clientId = clientId,
        title = title,
        recordType = RecordType.CONTRACT,
        authoredBy = authoredBy,
        summary = summary,
        parties = parties,
        validFrom = validFrom,
        validTo = validTo,
        confidentiality = RecordConfidentiality.LEGAL_HOLD
    )

    fun invoice(
        companyId: String,
        clientId: String,
        title: String,
        authoredBy: String,
        amount: Double? = null,
        currency: String = "COP",
        saleId: String? = null,
        folio: String = "",
        summary: String = ""
    ): Record {
        val payload = if (folio.isBlank()) emptyMap() else mapOf(RecordPayloadKeys.FOLIO to folio)
        return base(
            companyId = companyId,
            clientId = clientId,
            title = title,
            recordType = RecordType.INVOICE,
            authoredBy = authoredBy,
            summary = summary,
            amount = amount,
            currency = currency,
            saleId = saleId,
            industryPayload = payload
        )
    }

    fun report(
        companyId: String,
        clientId: String,
        title: String,
        authoredBy: String,
        summary: String = "",
        findings: String = "",
        conclusions: String = ""
    ): Record {
        val payload = buildMap<String, Any> {
            if (findings.isNotBlank()) put(RecordPayloadKeys.FINDINGS, findings)
            if (conclusions.isNotBlank()) put(RecordPayloadKeys.CONCLUSIONS, conclusions)
        }
        return base(
            companyId = companyId,
            clientId = clientId,
            title = title,
            recordType = RecordType.REPORT,
            authoredBy = authoredBy,
            summary = summary,
            industryPayload = payload
        )
    }

    fun technicalDoc(
        companyId: String,
        clientId: String,
        title: String,
        authoredBy: String,
        summary: String = "",
        docVersion: String = "1.0",
        systemScope: String = ""
    ): Record {
        val payload = buildMap<String, Any> {
            put(RecordPayloadKeys.DOC_VERSION, docVersion)
            if (systemScope.isNotBlank()) put(RecordPayloadKeys.SYSTEM_SCOPE, systemScope)
        }
        return base(
            companyId = companyId,
            clientId = clientId,
            title = title,
            recordType = RecordType.TECHNICAL_DOC,
            authoredBy = authoredBy,
            summary = summary,
            industryPayload = payload
        )
    }

    fun buildSearchTokens(
        title: String,
        summary: String = "",
        tags: List<String> = emptyList(),
        category: String = "",
        extra: List<String> = emptyList()
    ): List<String> {
        return (listOf(title, summary, category) + tags + extra)
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun base(
        companyId: String,
        clientId: String,
        title: String,
        recordType: RecordType,
        authoredBy: String,
        summary: String = "",
        sections: List<RecordSection> = emptyList(),
        parties: List<RecordParty> = emptyList(),
        industryPayload: Map<String, Any> = emptyMap(),
        confidentiality: RecordConfidentiality = RecordConfidentiality.STANDARD,
        amount: Double? = null,
        currency: String = "",
        saleId: String? = null,
        validFrom: Timestamp? = null,
        validTo: Timestamp? = null
    ): Record {
        val now = Timestamp.now()
        return Record(
            companyId = companyId,
            clientId = clientId,
            title = title.trim(),
            recordType = recordType,
            status = RecordStatus.DRAFT,
            summary = summary,
            serviceDate = now,
            sections = sections,
            parties = parties,
            industryPayload = industryPayload,
            confidentiality = confidentiality,
            amount = amount,
            currency = currency,
            saleId = saleId,
            validFrom = validFrom,
            validTo = validTo,
            authoredBy = authoredBy,
            createdBy = authoredBy,
            updatedBy = authoredBy,
            searchTokens = buildSearchTokens(title, summary)
        )
    }
}
