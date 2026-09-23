package com.example.nexogo.platform.usage.data

import com.example.nexogo.platform.tenant.context.TenantContext
import com.example.nexogo.platform.tenant.data.TenantCollections
import com.example.nexogo.platform.tenant.guard.TenantIsolationGuard
import com.example.nexogo.platform.usage.model.UsageActiveUser
import com.example.nexogo.platform.usage.model.UsageEvent
import com.example.nexogo.platform.usage.model.UsageEventType
import com.example.nexogo.platform.usage.model.UsageMetrics
import com.example.nexogo.platform.usage.model.UsagePeriodType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Persists usage events and maintains daily [UsageMetrics] counters.
 */
class UsageRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun companyRef(companyId: String) =
        firestore.collection("companies").document(companyId)

    private fun eventsCol(companyId: String) =
        companyRef(companyId).collection(TenantCollections.USAGE_EVENTS)

    private fun metricsCol(companyId: String) =
        companyRef(companyId).collection(TenantCollections.USAGE_METRICS)

    private fun activeUsersCol(companyId: String) =
        companyRef(companyId).collection(TenantCollections.USAGE_ACTIVE_USERS)

    /**
     * Appends an event and increments the matching daily metric counter.
     * For [UsageEventType.LOGIN] also marks the user as active (DAU).
     */
    suspend fun recordEvent(
        type: String,
        userId: String,
        companyId: String? = null,
        resourceType: String = "",
        resourceId: String = "",
        metadata: Map<String, Any> = emptyMap()
    ): Result<UsageEvent> {
        return try {
            val cid = resolveCompanyId(companyId)
            if (cid.isNullOrBlank()) {
                return Result.failure(IllegalStateException("companyId required for usage analytics"))
            }
            TenantIsolationGuard.requireCompanyId(cid)
            val now = Timestamp.now()
            val id = "usg_${UUID.randomUUID().toString().replace("-", "").take(16)}"
            val event = UsageEvent(
                id = id,
                companyId = cid,
                type = type,
                userId = userId,
                resourceType = resourceType,
                resourceId = resourceId,
                metadata = metadata,
                occurredAt = now,
                createdAt = now
            )
            eventsCol(cid).document(id).set(event).await()
            incrementMetric(cid, type)
            if (type == UsageEventType.LOGIN || type == UsageEventType.ACTIVE_USER) {
                markActiveUser(cid, userId)
            }
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markActiveUser(
        companyId: String,
        userId: String
    ): Result<UsageActiveUser> {
        return try {
            val cid = TenantIsolationGuard.requireCompanyId(companyId)
            if (userId.isBlank()) {
                return Result.failure(IllegalArgumentException("userId required"))
            }
            val dayKey = dayKeyUtc()
            val docId = "${dayKey}_$userId"
            val ref = activeUsersCol(cid).document(docId)
            val existing = ref.get().await()
            val now = Timestamp.now()
            if (existing.exists()) {
                ref.update("lastSeenAt", now).await()
                val active = existing.toObject(UsageActiveUser::class.java)
                    ?.copy(id = docId, lastSeenAt = now)
                    ?: UsageActiveUser(id = docId, companyId = cid, userId = userId, dayKey = dayKey, lastSeenAt = now)
                return Result.success(active)
            }
            val active = UsageActiveUser(
                id = docId,
                companyId = cid,
                userId = userId,
                dayKey = dayKey,
                lastSeenAt = now
            )
            ref.set(active).await()
            // First sighting today → bump distinct activeUsers on metrics
            val periodKey = periodKeyForDay(dayKey)
            metricsCol(cid).document(periodKey).set(
                mapOf(
                    "id" to periodKey,
                    "companyId" to cid,
                    "periodKey" to periodKey,
                    "periodType" to UsagePeriodType.DAY,
                    "activeUsers" to FieldValue.increment(1),
                    "updatedAt" to now
                ),
                SetOptions.merge()
            ).await()
            Result.success(active)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDailyMetrics(
        companyId: String,
        dayKey: String = dayKeyUtc()
    ): Result<UsageMetrics?> {
        return try {
            val cid = TenantIsolationGuard.requireCompanyId(companyId)
            val periodKey = periodKeyForDay(dayKey)
            val snap = metricsCol(cid).document(periodKey).get().await()
            if (!snap.exists()) {
                Result.success(null)
            } else {
                Result.success(snap.toObject(UsageMetrics::class.java)?.copy(id = snap.id))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listRecentEvents(
        companyId: String,
        limit: Long = 50
    ): Result<List<UsageEvent>> {
        return try {
            val cid = TenantIsolationGuard.requireCompanyId(companyId)
            val snap = eventsCol(cid)
                .orderBy("occurredAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            Result.success(
                snap.documents.mapNotNull { doc ->
                    doc.toObject(UsageEvent::class.java)?.copy(id = doc.id)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun incrementMetric(companyId: String, type: String) {
        val dayKey = dayKeyUtc()
        val periodKey = periodKeyForDay(dayKey)
        val field = when (type) {
            UsageEventType.LOGIN -> "loginCount"
            UsageEventType.CLIENT_CREATED -> "clientsCreated"
            UsageEventType.RECORD_CREATED -> "recordsCreated"
            UsageEventType.DOCUMENT_UPLOADED -> "documentsUploaded"
            else -> return
        }
        metricsCol(companyId).document(periodKey).set(
            mapOf(
                "id" to periodKey,
                "companyId" to companyId,
                "periodKey" to periodKey,
                "periodType" to UsagePeriodType.DAY,
                field to FieldValue.increment(1),
                "updatedAt" to Timestamp.now()
            ),
            SetOptions.merge()
        ).await()
    }

    private fun resolveCompanyId(explicit: String?): String? {
        explicit?.takeIf { it.isNotBlank() }?.let { return it }
        return TenantContext.companyId?.takeIf { it.isNotBlank() }
    }

    companion object {
        fun dayKeyUtc(date: Date = Date()): String {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            fmt.timeZone = TimeZone.getTimeZone("UTC")
            return fmt.format(date)
        }

        fun periodKeyForDay(dayKey: String): String = "day_$dayKey"
    }
}
