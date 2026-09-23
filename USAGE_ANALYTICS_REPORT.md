# USAGE ANALYTICS FOUNDATION Report

**Fecha:** 2026-09-20  
**Compilación:** `:app:compileDebugKotlin` — **BUILD SUCCESSFUL**  
**Objetivo:** Registrar login, clientes creados, expedientes creados, documentos cargados y usuarios activos por company.

---

## Resumen

| Entrega | Estado |
|---------|--------|
| `UsageEvent` | ✅ |
| `UsageMetrics` | ✅ |
| `UsageRepository` | ✅ |
| Facade `UsageAnalytics` | ✅ |
| Hooks login / clients / records / docs / DAU | ✅ |

---

## Modelo

### UsageEvent
Path: `companies/{companyId}/usage_events/{eventId}`

| Campo | Uso |
|-------|-----|
| type | `login`, `client_created`, `record_created`, `document_uploaded`, `active_user` |
| userId / resourceId | Quién / qué |
| occurredAt | Timestamp |

### UsageMetrics
Path: `companies/{companyId}/usage_metrics/day_yyyy-MM-dd`

Contadores diarios (UTC) vía `FieldValue.increment`:

- `loginCount`
- `clientsCreated`
- `recordsCreated`
- `documentsUploaded`
- `activeUsers` (distintos del día)

### UsageActiveUser (DAU)
Path: `companies/{companyId}/usage_active_users/{yyyy-MM-dd}_{userId}`

Primer sighting del día incrementa `activeUsers`; re-visitas solo actualizan `lastSeenAt`.

---

## API

```kotlin
UsageAnalytics.login(userId, companyId?)
UsageAnalytics.clientCreated(userId, clientId, companyId?)
UsageAnalytics.recordCreated(userId, recordId, companyId?)
UsageAnalytics.documentUploaded(userId, documentId, companyId?)
UsageAnalytics.activeUser(userId, companyId?)
```

Fire-and-forget (IO scope); fallos no rompen UX.

`UsageRepository`: `recordEvent`, `markActiveUser`, `getDailyMetrics`, `listRecentEvents`.

---

## Integraciones

| Evento | Origen |
|--------|--------|
| Login | `PersistentAuthViewModel.bindCompanySession` (tras AuditLogger) |
| Usuarios activos | Login + `CompanySessionManager.bindCompany` |
| Cliente creado | `ClientViewModel` (person / org / pet) |
| Expediente creado | `RecordViewModel.createFromFactory` |
| Documento cargado | `DocumentViewModel.uploadFromUri` |

Sin company activa: login/usage se omiten (requieren tenant).

---

## Rules

Cubierto por `companies/{companyId}/{subcollection}` (miembros ACTIVE). Sin rules nuevas.

---

## Archivos

```
platform/usage/model/UsageModels.kt
platform/usage/data/UsageRepository.kt
platform/usage/UsageAnalytics.kt
platform/tenant/data/TenantCollections.kt
viewmodel/PersistentAuthViewModel.kt
platform/company/session/CompanySessionManager.kt
platform/clients/viewmodel/ClientViewModel.kt
platform/records/viewmodel/RecordViewModel.kt
platform/documents/viewmodel/DocumentViewModel.kt
```

---

## Fuera de alcance (siguiente)

- UI dashboard de métricas  
- Agregados mensuales / billing  
- Export analytics  
- Index compuesto si queries por type + date crecen  

---

*Usage Analytics Foundation. Compilación verificada.*
