# Audit Foundation Report

**Fecha:** 2026-09-19  
**Alcance:** Fundación de auditoría empresarial multiempresa  
**Compilación:** `:app:compileDebugKotlin` — **BUILD SUCCESSFUL** (~3m 34s)  
**UI NavGraph:** No cableada (AuditViewer listo para pantallas futuras)

---

## Objetivo

Registrar acciones sensibles (login, logout, creación, edición, eliminación, descargas) con **usuario**, **empresa**, **fecha** e **IP** (si está disponible), sin romper el flujo de autenticación.

---

## Entregables

| Entregable | Ubicación | Estado |
|------------|-----------|--------|
| **AuditEvent** | `platform/audit/model/AuditModels.kt` | ✅ |
| **AuditRepository** | `platform/audit/data/AuditRepository.kt` | ✅ |
| **AuditViewer** | `platform/audit/viewer/AuditViewer.kt` | ✅ |
| **AuditLogger** | `platform/audit/AuditLogger.kt` (facade fire-and-forget) | ✅ |
| IP provider | `platform/audit/ip/AuditIpProvider.kt` | ✅ |
| Auth wiring | `PersistentAuthViewModel` login/logout | ✅ |

---

## Acciones registradas

| `AuditAction` | Helper | Uso |
|---------------|--------|-----|
| `LOGIN` | `recordLogin` / `AuditLogger.login` | Tras bind de Company en auth |
| `LOGOUT` | `recordLogout` / `AuditLogger.logout` | Antes de clear session |
| `CREATE` | `recordCreate` / `AuditLogger.create` | Módulos (Clients, Docs, …) |
| `UPDATE` | `recordUpdate` / `AuditLogger.update` | Idem |
| `DELETE` | `recordDelete` / `AuditLogger.delete` | Idem |
| `DOWNLOAD` | `recordDownload` / `AuditLogger.download` | Documentos / exports |

---

## Campos obligatorios por evento

| Campo | Fuente |
|-------|--------|
| `userId` (+ displayName / email) | Auth / caller |
| `companyId` | TenantContext / session / argumento |
| `occurredAt` / `createdAt` | `Timestamp.now()` |
| `ipAddress` | `AuditIpProvider` (vacío si no disponible en device) |
| `action`, `resourceType`, `resourceId`, `summary` | Caller / defaults |

---

## Persistencia

```
companies/{companyId}/audit_logs/{eventId}
platform/audit/audit_logs/{eventId}     # sentinel _platform (auth sin tenant)
```

Usa `TenantCollections.AUDIT_LOGS` existente.

---

## AuditViewer

ViewModel de lectura:

- `bindCompany` / `refresh`
- Filtros: action, userId, resourceType
- `selectEvent` / `formatEventLine` → `user · ACTION · date · IP`
- Estado: `AuditViewerState`

Sin ruta Compose todavía (Configuración → Auditoría en plan de nav unificada).

---

## Integración Auth

`PersistentAuthViewModel`:

- `bindCompanySession` → `AuditLogger.login(...)` (fail-open)
- `logout` → captura user/company → `AuditLogger.logout(...)` → clear

`AuditLogger` nunca propaga excepciones al UX.

---

## IP

- `EmptyAuditIpProvider` por defecto (IP `""` → UI “IP n/d”).
- `MutableAuditIpProvider` para inyectar IP desde backend / Cloud Function más adelante.
- En móvil la IP pública real requiere hop de red; la foundation ya persiste el campo.

---

## Uso desde otros módulos

```kotlin
AuditLogger.create(
    userId = uid,
    resourceType = AuditResourceTypes.DOCUMENT,
    resourceId = docId,
    resourceLabel = docName
)
AuditLogger.download(userId = uid, resourceType = AuditResourceTypes.DOCUMENT, resourceId = docId)
```

---

## Archivos

```
app/src/main/java/com/example/nexogo/platform/audit/
  model/AuditModels.kt
  data/AuditRepository.kt
  viewer/AuditViewer.kt
  ip/AuditIpProvider.kt
  AuditLogger.kt
```

**Modificado:** `PersistentAuthViewModel.kt` (login/logout audit)

---

## Compilación

```bash
bash ./gradlew :app:compileDebugKotlin --no-daemon
# BUILD SUCCESSFUL
```

---

## Fuera de alcance (siguiente)

- Pantalla Compose AuditViewer en Configuración
- Instrumentar DocumentRepository / ClientRepository create-update-delete-download
- IP real vía Callable Function
- Retención / export de audit logs (ENTERPRISE)
- Security Rules: solo ADMIN lee audit_logs

---

## Veredicto

**Audit Foundation completa y compilando.** Eventos con usuario/empresa/fecha/IP; login/logout cableados; create/update/delete/download listos vía `AuditLogger`.
