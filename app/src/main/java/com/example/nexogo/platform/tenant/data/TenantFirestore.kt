package com.example.nexogo.platform.tenant.data

import com.example.nexogo.platform.tenant.scope.CompanyScope
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Firestore helpers that always resolve collections under companies/{companyId}/…
 * Prepared for future modules — does not touch legacy root collections.
 */
class TenantFirestore(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun companyDocument(scope: CompanyScope): DocumentReference =
        firestore.document(scope.rootPath())

    fun collection(scope: CompanyScope, collection: String): CollectionReference =
        firestore.collection(scope.collectionPath(collection))

    fun document(
        scope: CompanyScope,
        collection: String,
        documentId: String
    ): DocumentReference = firestore.document(scope.documentPath(collection, documentId))

    fun clients(scope: CompanyScope): CollectionReference =
        collection(scope, TenantCollections.CLIENTS)

    fun records(scope: CompanyScope): CollectionReference =
        collection(scope, TenantCollections.RECORDS)

    fun appointments(scope: CompanyScope): CollectionReference =
        collection(scope, TenantCollections.APPOINTMENTS)

    fun products(scope: CompanyScope): CollectionReference =
        collection(scope, TenantCollections.PRODUCTS)

    fun sales(scope: CompanyScope): CollectionReference =
        collection(scope, TenantCollections.SALES)

    fun opportunities(scope: CompanyScope): CollectionReference =
        collection(scope, TenantCollections.OPPORTUNITIES)

    fun leads(scope: CompanyScope): CollectionReference =
        collection(scope, TenantCollections.LEADS)

    fun documents(scope: CompanyScope): CollectionReference =
        collection(scope, TenantCollections.DOCUMENTS)

    fun conversations(scope: CompanyScope): CollectionReference =
        collection(scope, TenantCollections.CONVERSATIONS)

    fun tasks(scope: CompanyScope): CollectionReference =
        collection(scope, TenantCollections.TASKS)

    fun conversationMessages(scope: CompanyScope, conversationId: String): CollectionReference =
        document(scope, TenantCollections.CONVERSATIONS, conversationId)
            .collection(TenantCollections.MESSAGES)

    /**
     * Query within a tenant collection, optionally filtering denormalized companyId
     * (defense in depth — path already isolates).
     */
    fun scopedQuery(
        scope: CompanyScope,
        collection: String,
        enforceCompanyField: Boolean = true
    ): Query {
        val base: Query = collection(scope, collection)
        return if (enforceCompanyField) {
            base.whereEqualTo(FIELD_COMPANY_ID, scope.companyId)
        } else {
            base
        }
    }

    companion object {
        const val FIELD_COMPANY_ID = "companyId"
    }
}
