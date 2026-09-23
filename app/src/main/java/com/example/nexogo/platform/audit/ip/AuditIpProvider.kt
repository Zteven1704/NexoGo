package com.example.nexogo.platform.audit.ip

/**
 * Resolves client IP for audit events.
 * On Android the public IP is usually unknown without a backend;
 * implementations may return empty / LAN / value injected by Cloud Function.
 */
fun interface AuditIpProvider {
    suspend fun currentIp(): String
}

object EmptyAuditIpProvider : AuditIpProvider {
    override suspend fun currentIp(): String = ""
}

/**
 * Holds last known IP from a trusted source (e.g. Cloud Function response).
 */
class MutableAuditIpProvider : AuditIpProvider {
    @Volatile
    var lastKnownIp: String = ""

    override suspend fun currentIp(): String = lastKnownIp.trim()
}
