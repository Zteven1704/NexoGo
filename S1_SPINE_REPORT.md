# S1 Spine Report

**Sprint:** S1 Spine (`TOP_20_NEXT_ACTIONS.md` #6, #7, #8, #9, #14)  
**Fecha:** 2026-09-19  
**Compilación:** `:app:compileDebugKotlin` — **BUILD SUCCESSFUL**  
**Restricciones:** compatibilidad; sin tocar módulos legacy de negocio; sin features nuevas.

---

## Resumen

| # | Acción | Estado |
|---|--------|--------|
| 6 | CompanySession obligatoria | ✅ |
| 7 | Un único hub post-login (Home) | ✅ |
| 8 | PermissionEngine en navegación Home | ✅ |
| 9 | Seed roles base + assignment | ✅ |
| 14 | Quitar FirebaseTest / logins experimentales del grafo beta | ✅ |

---

## #6 CompanySession obligatoria

**`CompanySessionManager`**
- Bind exige membership `ACTIVE`; si falta → sesión ready **sin** company.
- Fallos ya no son “silent continue”: `errorMessage` + `TenantContext.clear()`.
- Hub (`HomeScreen`) bloquea contenido si `!hasActiveCompany` (loading / pane Reintentar + Cerrar sesión).

**Compat:** auth legacy sigue completando login; el bloqueo es en el hub, no en Auth APIs.

---

## #7 Único hub

- Post-login / registro → **solo** `Screen.Home`.
- `Screen.Dashboard` redirige a Home (`LaunchedEffect`).
- Rutas `ModernLogin` / `UltraSimpleLogin` / `MinimalLogin` **fuera** del `NavHost` beta (constantes en `Screen.kt` conservadas).

---

## #8 PermissionEngine → navegación

- Nuevo helper: `platform/role/nav/NavPermissionFactory.kt`
- Motor desde `membership.roleCodes` o `LegacyRoleBridge` (legacy `UserRole`).
- Drawer + quick actions filtrados (`clients`, `documents`, `sales`, `inventory`, `chat`, admin).
- **No** se modificaron pantallas/repos de `modules/*` legacy.

---

## #9 Seed de roles

En cada `bindCompany` exitoso:
1. `RoleRepository.seedPlatformFoundation()` (idempotente; create-only en rules).
2. `upsertAssignment` con `roleCodes` del membership (fallback `ADMIN`).

**Rules (ajuste mínimo S1):**  
`permissions_catalog` / `platform_roles` / `role_permissions` — `allow create` si autenticado; update/delete denegados.

---

## #14 Nav beta limpia

Eliminado del grafo:
- Composables de login experimentales
- `FirebaseTest` / FAB / quick action “Prueba Firebase”
- Hub Dashboard como destino vivo

Pantallas/archivos legacy **no borrados** (compat / compile).

---

## Archivos principales

```
platform/company/session/CompanySessionManager.kt
platform/role/nav/NavPermissionFactory.kt          (nuevo)
ui/screens/home/HomeScreen.kt
navigation/NavGraph.kt
viewmodel/PersistentAuthViewModel.kt              (comentario)
firestore.rules                                   (create IAM catalog)
S1_SPINE_REPORT.md
```

---

## Compilación

```
./gradlew :app:compileDebugKotlin
→ BUILD SUCCESSFUL
```

---

## Ops / siguiente

1. Redesplegar `firestore.rules` (create catalog) junto a las de S0.  
2. Probar: login → company auto / membership → Home con menú filtrado.  
3. **S2 Cutover:** Clients + Records en NavGraph (`TOP_20` #10–#13).
