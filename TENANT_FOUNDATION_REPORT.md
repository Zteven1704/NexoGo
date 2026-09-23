# Tenant Isolation Foundation Report

**Fecha:** 2026-09-18  
**Alcance:** Infraestructura de aislamiento por Company (tenant)  
**Compilación:** `:app:compileDebugKotlin --rerun-tasks` — **BUILD SUCCESSFUL**  
**Migración legacy:** No realizada (por diseño)

---

## Objetivo

Preparar la base para que **toda entidad futura** pertenezca a una Company, con paths Firestore/Storage y repositorios scoped — sin migrar pacientes, citas, historial u otros módulos legacy.

---

## Entregables

| Entregable | Ubicación | Estado |
|------------|-----------|--------|
| **TenantAwareEntity** | `platform/tenant/model/TenantAwareEntity.kt` | ✅ |
| **TenantContext** | `platform/tenant/context/TenantContext.kt` | ✅ |
| **CompanyScope** | `platform/tenant/scope/CompanyScope.kt` | ✅ |
| **Firestore queries** | `platform/tenant/data/TenantFirestore.kt` | ✅ |
| **Repositories base** | `platform/tenant/data/TenantAwareRepository.kt` | ✅ |
| **Storage paths** | `platform/tenant/storage/TenantStoragePaths.kt` | ✅ |
| **Collections catalog** | `platform/tenant/data/TenantCollections.kt` | ✅ |
| **Isolation guard** | `platform/tenant/guard/TenantIsolationGuard.kt` | ✅ |

---

## Contratos

### TenantAwareEntity

Toda entidad de negocio futura debe exponer:

- `id`
- `companyId` (obligatorio, denormalizado)
- `createdAt` / `updatedAt`

### TenantContext

Holder de sesión activa (`companyId`, `userId`).

- Se actualiza desde `CompanySessionManager` al bind / clear.
- `requireCompanyId()` falla si no hay tenant (deny-by-default para repos nuevos).

### CompanyScope

Ámbito de operaciones:

```
companies/{companyId}
companies/{companyId}/{collection}
companies/{companyId}/{collection}/{documentId}
```

Incluye `assertOwns(entity)` para bloquear lecturas/escrituras cross-tenant en código.

---

## Firestore preparado

| Helper | Uso |
|--------|-----|
| `TenantFirestore.collection(scope, name)` | Subcolección bajo company |
| `TenantFirestore.scopedQuery(...)` | Query + `whereEqualTo("companyId", …)` (defensa en profundidad) |
| Accesos tipados | `clients`, `records`, `appointments`, `products`, `sales`, `documents`, `conversations` |

Árbol canónico (futuro), alineado a `MULTITENANT_ARCHITECTURE.md` con raíz `companies/` (no `organizations/`):

```
companies/{companyId}/clients|records|appointments|products|sales|…
```

---

## Repositories

`TenantAwareRepository` (abstracto):

- Obliga `collectionName`
- Resuelve scope vía `TenantContext` o `companyId` explícito
- `setEntity` / `deleteEntity` con validación de tenant
- **Ningún** repo legacy extiende esta clase todavía

---

## Storage paths

Prefijo: `companies/{companyId}/…`

Helpers: `documents`, `records`, `clients`, `chat`, `sales`, `branding`, `tmp`  
Validación: `TenantStoragePaths.belongsToCompany` / `assertBelongsToCompany`

---

## Cableado mínimo

| Archivo | Cambio |
|---------|--------|
| `CompanySessionManager` | `TenantContext.bind` en bind exitoso; `clear` en logout / fallo |

Sin cambios en pantallas Compose ni en repos de pacientes/citas/historial.

---

## Reglas respetadas

| Regla | Cumplimiento |
|-------|--------------|
| Toda entidad futura → Company | ✅ Contrato + scope + paths |
| No migrar módulos legacy | ✅ |
| Solo infraestructura | ✅ |
| Compilar | ✅ BUILD SUCCESSFUL |

---

## Archivos nuevos

```
app/src/main/java/com/example/nexogo/platform/tenant/
  model/TenantAwareEntity.kt
  context/TenantContext.kt
  scope/CompanyScope.kt
  data/TenantCollections.kt
  data/TenantFirestore.kt
  data/TenantAwareRepository.kt
  storage/TenantStoragePaths.kt
  guard/TenantIsolationGuard.kt
```

**Modificado (hook):** `platform/company/session/CompanySessionManager.kt`

---

## Uso previsto (módulos nuevos)

```kotlin
data class Client(
    override val id: String = "",
    override val companyId: String = "",
    val name: String = "",
    override val createdAt: Timestamp = Timestamp.now(),
    override val updatedAt: Timestamp = Timestamp.now()
) : TenantAwareEntity

class ClientRepository : TenantAwareRepository() {
    override val collectionName = TenantCollections.CLIENTS
}
```

---

## Fuera de alcance (siguiente)

- Migrar `Patient` / citas / clinical records a `companies/{id}/…`
- Security Rules tenant-scoped
- Mover bytes legacy de Storage
- UI de switcher multi-company

---

## Veredicto

**TENANT ISOLATION FOUNDATION lista y compilando.** Infraestructura lista para módulos nuevos bajo Company; legacy intacto.
