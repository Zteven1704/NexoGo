# Permission Engine Report

**Módulo:** `platform.role.engine`  
**Estado:** Foundation complete — `:app:compileDebugKotlin` **BUILD SUCCESSFUL**  
**Capa:** Aditiva sobre ROLE FOUNDATION (`PermissionKeys` / `PermissionChecker`). No reemplaza IAM legacy.

---

## Objetivo

Motor de permisos por **módulo × acción** con vocabulario uniforme:

| Acciones | Módulos |
|----------|---------|
| `view` · `create` · `edit` · `delete` · `export` · `approve` | `clientes` · `documentos` · `ventas` · `inventario` · `chat` · `tareas` |

Clave canónica: `{module}.{action}` → ej. `clients.view`, `tasks.approve`.

---

## Entregables

| Artefacto | Path |
|-----------|------|
| `PermissionAction` / `PermissionModule` / `PermissionMatrix` | `platform/role/engine/PermissionEngineModels.kt` |
| `EnginePermissionKeys` | `platform/role/engine/EnginePermissionKeys.kt` |
| `LegacyPermissionBridge` | `platform/role/engine/LegacyPermissionBridge.kt` |
| `PermissionEngine` | `platform/role/engine/PermissionEngine.kt` |
| Tasks + engine keys en catálogo | `platform/role/model/PermissionCatalog.kt` |
| `canEnterModule("tasks")` | `platform/role/check/PermissionChecker.kt` |

---

## Matriz (6 × 6)

| Módulo | view | create | edit | delete | export | approve |
|--------|:----:|:------:|:----:|:------:|:------:|:-------:|
| clients | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| documents | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| sales | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| inventory | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| chat | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| tasks | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |

36 claves engine sembradas vía `EnginePermissionKeys.allKeys()`.

---

## API `PermissionEngine`

```kotlin
val engine = PermissionEngine.fromRoleCodes(listOf("EMPLOYEE"))

engine.can(PermissionModule.CLIENTS, PermissionAction.VIEW)
engine.canView / canCreate / canEdit / canDelete / canExport / canApprove
engine.canEnter(PermissionModule.TASKS)
engine.allowedActions(PermissionModule.SALES)
engine.enabledModules()
engine.decide(module, action)   // PermissionDecision + reason
engine.matrix()
engine.toChecker()              // bridge → PermissionChecker
```

Factories: `fromKeys`, `fromMatrix`, `fromRoleCodes`, `fromAssignment`, `fromChecker`.

Reglas:
- `moduleAccess[module] == false` → deny
- `SUPER_ADMIN` → bypass total
- Claves mixtas (legacy + engine) se consolidan en `PermissionMatrix`

---

## Bridge legacy

`LegacyPermissionBridge` mapea `{domain}.{resource}.{action}` → grants engine, p.ej.:

| Legacy | Engine |
|--------|--------|
| `clients.client.read` | `clients.view` |
| `clients.client.update` | `clients.edit` |
| `documents.file.upload` | `documents.create` + `documents.edit` |
| `sales.sale.export` | `sales.export` |
| `sales.sale.charge` | `sales.approve` |
| `inventory.product.manage` | create + edit + delete |
| `chat.conversation.moderate` | edit + delete |
| `tasks.task.approve` | `tasks.approve` |

Así roles existentes ganan matriz engine sin migrar call sites.

---

## Catálogo ampliado

Nuevas claves legacy (tasks):

- `tasks.task.read` / `create` / `update` / `delete` / `export` / `approve` / `read_own`

Bundles base:

| Rol | Engine |
|-----|--------|
| ADMIN / MANAGER / SUPER_ADMIN | `ENGINE_FULL` (36 claves) + tasks CRUD |
| EMPLOYEE | view/create/edit en clients, documents, sales, tasks; inventory view; chat view/create |
| CLIENT | view (clients, documents, sales, tasks) + chat view/create |

---

## Compilación

```
./gradlew :app:compileDebugKotlin
→ BUILD SUCCESSFUL
```

---

## Fuera de alcance (siguiente)

1. UI role editor (matriz checkbox módulo × acción)
2. Enforce en repositories (Tasks / Clients / Documents / CRM)
3. Firestore rules alineadas a engine keys
4. Preferencias por usuario (deny overrides)

---

## Archivos

```
platform/role/engine/PermissionEngineModels.kt
platform/role/engine/EnginePermissionKeys.kt
platform/role/engine/LegacyPermissionBridge.kt
platform/role/engine/PermissionEngine.kt
platform/role/model/PermissionCatalog.kt   (mod)
platform/role/check/PermissionChecker.kt   (mod)
PERMISSION_ENGINE_REPORT.md
```
