# Role Foundation Report

**Fecha:** 2026-09-18  
**Alcance:** Fundación IAM de plataforma (Role / Permission / UserRoleAssignment)  
**Compilación:** `:app:compileDebugKotlin --rerun-tasks` — **BUILD SUCCESSFUL**  
**UI:** No conectada (por diseño)

---

## Objetivo

Implementar la capa mínima de roles y permisos de NexoGo Platform sin tocar el sistema legacy `UserRole` (ADMIN / VET / VET_ASSISTANT / USER) ni pantallas Compose.

---

## Entregables

| Entregable | Ubicación | Estado |
|------------|-----------|--------|
| **Role** | `platform/role/model/RoleModels.kt` | ✅ |
| **Permission** | `platform/role/model/RoleModels.kt` + `PermissionCatalog.kt` | ✅ |
| **UserRoleAssignment** | `platform/role/model/RoleModels.kt` | ✅ |
| **Firestore models / paths** | `RolePaths.kt` | ✅ |
| **Repository** | `platform/role/data/RoleRepository.kt` | ✅ |
| **Permission checker** | `platform/role/check/PermissionChecker.kt` | ✅ |
| **Compatibilidad legacy** | `platform/role/compat/LegacyRoleBridge.kt` | ✅ |

---

## Roles base

| Código | Ámbito | Descripción |
|--------|--------|-------------|
| `SUPER_ADMIN` | Plataforma | Cross-tenant / SaaS |
| `ADMIN` | Empresa | Control total del tenant |
| `MANAGER` | Empresa | Liderazgo operativo |
| `EMPLOYEE` | Empresa | Staff operativo |
| `CLIENT` | Empresa (portal) | Recursos propios |

Constantes: `BaseRoleCodes` (alineado con `CompanyRoleCodes` existente).  
Bundles default: `BaseRolePermissionBundles` (catálogo `PermissionKeys` según `ROLE_SYSTEM.md` §5).

---

## Modelos Firestore

| Colección / path | Documento | Contenido |
|------------------|-----------|-----------|
| `permissions_catalog/{key}` | permiso atómico | `Permission` |
| `platform_roles/{code}` | rol sistema | `Role` (`isSystem=true`) |
| `companies/{companyId}/roles/{roleId}` | rol custom | `Role` (`companyId` set) |
| `companies/{companyId}/role_assignments/{userId}` | asignación | `UserRoleAssignment` |
| `role_permissions/{companyId\|_platform}_{roleCode}` | efectivo compilado | `RolePermissionsDoc` |

---

## Repository (`RoleRepository`)

- `seedPlatformFoundation()` — catalog + 5 roles sistema + `role_permissions` (idempotente)
- CRUD roles custom (niega `platform.*`)
- `upsertAssignment` / `getAssignment` / `listAssignments` / `revokeAssignment`
- `resolveEffectivePermissions` / `resolveFromAssignment` (OR de roles; fallback in-memory si Firestore falla)

---

## PermissionChecker

API pura (sin UI):

- `hasPermission` / `hasAny` / `hasAll`
- `hasRole` / `isSuperAdmin`
- `canEnterModule(module)`
- Factories: `fromRoleCodes`, `fromAssignment`, `fromEffective`
- Regla auxiliar: `*.manage` implica CRUD del mismo recurso

---

## Compatibilidad legacy

| Legacy `UserRole` | Platform `BaseRoleCodes` |
|-------------------|--------------------------|
| `ADMIN` | `ADMIN` |
| `VET` | `MANAGER` |
| `VET_ASSISTANT` | `EMPLOYEE` |
| `USER` | `CLIENT` |

- **No se eliminó** ni se modificó `core.models.UserRole`.
- Auth / pantallas legacy siguen usando el enum vet.
- `LegacyRoleBridge` permite mapear cuando se integre IAM más adelante.

---

## Reglas respetadas

| Regla | Cumplimiento |
|-------|--------------|
| No conectar a UI | ✅ Ningún cambio en Compose / NavGraph |
| No eliminar roles legacy | ✅ `UserRole` intacto |
| Mantener compatibilidad | ✅ Paquete aditivo `platform.role` |
| No romper compilación | ✅ BUILD SUCCESSFUL |

---

## Archivos nuevos

```
app/src/main/java/com/example/nexogo/platform/role/
  model/RoleModels.kt
  model/PermissionCatalog.kt
  data/RolePaths.kt
  data/RoleRepository.kt
  check/PermissionChecker.kt
  compat/LegacyRoleBridge.kt
```

**Sin cambios** en módulos legacy (pacientes, historial, auth UI, Company session wiring).

---

## Compilación

```bash
bash ./gradlew :app:compileDebugKotlin --no-daemon --rerun-tasks
# BUILD SUCCESSFUL
```

---

## Fuera de alcance (siguiente)

- Conectar `PermissionChecker` a Home / NavGraph
- Claims Firebase Auth
- Sync `UserRoleAssignment` ↔ `CompanyMembership.roleCodes`
- Llamar `seedPlatformFoundation()` en bootstrap
- Security Rules basadas en `role_permissions`
- Pantallas de gestión de roles custom

---

## Veredicto

**ROLE FOUNDATION completa y compilando.** Capas de modelo, Firestore, repositorio y checker listas; legacy intacto; UI sin cablear.
