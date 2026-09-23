# Notification Foundation Report

**Module:** `platform.notifications`  
**Status:** Foundation complete — compiles (`:app:compileDebugKotlin` BUILD SUCCESSFUL)  
**Constraint:** Additive only. Does not replace legacy FCM (`NexoGoMessagingService` / `FirebaseMessagingRepository`).

---

## Deliverables

| Artifact | Path | Role |
|----------|------|------|
| `Notification` | `platform/notifications/model/NotificationModels.kt` | Tenant-scoped alert document |
| `NotificationType` | same | Domain event categories |
| `NotificationChannel` | same | PUSH / EMAIL / IN_APP |
| `NotificationFactories` | same | Typed builders for domain events |
| `NotificationDispatcher` | `platform/notifications/dispatch/NotificationDispatchers.kt` | Channel adapters (NoOp stubs) |
| `NotificationRepository` | `platform/notifications/data/NotificationRepository.kt` | Persist + prepare dispatch |
| Collection key | `TenantCollections.NOTIFICATIONS` | `"notifications"` |

---

## Data model

**Path:** `companies/{companyId}/notifications/{notificationId}`

```
Notification : TenantAwareEntity
  id, companyId
  recipientUserId, actorUserId
  type: NotificationType
  channels: List<NotificationChannel>
  title, body, deepLink
  resourceType, resourceId
  priority, status
  readAt, pushSentAt, emailSentAt
  pushPayload, emailTo, metadata
  createdAt, updatedAt
```

### NotificationType (events)

| Type | Domain |
|------|--------|
| `TASK` | Tareas (asignada / por vencer) |
| `DOCUMENT` | Documentos (compartido) |
| `SALE` | Ventas (nueva venta) |
| `CLIENT` | Clientes (nuevo cliente) |
| `RECORD` | Expedientes (finalizado) |
| `CHAT` | Chat (bridge opcional) |
| `SYSTEM` | Sistema / billing |

### Channels (prepared)

| Channel | Foundation behavior |
|---------|---------------------|
| `IN_APP` | Source of truth: Firestore document + inbox APIs |
| `PUSH` | `NoOpPushDispatcher` — payload map ready via `toPushDataMap()` |
| `EMAIL` | `NoOpEmailDispatcher` — envelope ready (`emailTo` + request) |

---

## Repository API

### Create / domain helpers

- `createNotification(notification)`
- `notifyTaskAssigned(...)` / `notifyTaskDueSoon(...)`
- `notifyDocumentShared(...)`
- `notifySaleCreated(...)`
- `notifyClientCreated(...)`
- `notifyRecordFinalized(...)`

### Inbox

- `listForUser(companyId, userId, unreadOnly, limit)`
- `listByType(companyId, type, limit)`
- `listenForUser(companyId, userId): Flow`
- `markRead` / `markAllRead` / `dismiss` / `unreadCount`

### Dispatch prep

- Injects `InAppNotificationDispatcher` + NoOp push/email by default
- After write, runs channel pipeline and stamps `pushSentAt` / `emailSentAt` / `status`
- `toPushDataMap(notification)` for future FCM / Cloud Function workers

---

## Event factories (defaults)

| Factory | Channels default | Priority |
|---------|------------------|----------|
| `taskAssigned` | IN_APP + PUSH | HIGH |
| `taskDueSoon` | IN_APP + PUSH | URGENT |
| `documentShared` | IN_APP + PUSH | NORMAL |
| `saleCreated` | IN_APP | NORMAL |
| `clientCreated` | IN_APP | NORMAL |
| `recordFinalized` | IN_APP + PUSH | HIGH |

Deep links: `nexogo://company/{companyId}/{resource}/{id}`.

---

## Isolation & safety

- Extends `TenantAwareRepository`; writes under active `companyId`
- `TenantIsolationGuard.requireCompanyId` on all tenant ops
- Legacy messaging stack untouched
- No network I/O in foundation dispatchers (safe stubs)

---

## Compile

```
./gradlew :app:compileDebugKotlin
→ BUILD SUCCESSFUL
```

---

## Next (out of scope)

1. Wire domain repos (Tasks / Documents / CRM / Clients / Records) to call `notify*`
2. Replace NoOp push with Cloud Function → FCM using `toPushDataMap`
3. Replace NoOp email with transactional provider
4. In-app inbox UI + unread badge in unified navigation
5. Firestore indexes: `recipientUserId`, `type`, `status` + `createdAt`
6. Optional: preference store (per-user channel mute)

---

## Files added

```
app/.../platform/notifications/model/NotificationModels.kt
app/.../platform/notifications/dispatch/NotificationDispatchers.kt
app/.../platform/notifications/data/NotificationRepository.kt
NOTIFICATION_REPORT.md
```

`TenantCollections.kt` — `NOTIFICATIONS = "notifications"` (already registered).
