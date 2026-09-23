# USER MANAGEMENT v1 Report

**Fecha:** 2026-09-20  
**Compilación:** `:app:compileDebugKotlin` — **BUILD SUCCESSFUL**  
**Alcance:** Invitar / activar / desactivar / cambiar rol / eliminar acceso, integrado con Company, Roles y PermissionEngine.

---

## Resumen

| Capacidad | Estado |
|-----------|--------|
| Invitar usuario (email) | ✅ |
| Activar usuario | ✅ |
| Desactivar usuario | ✅ |
| Cambiar rol | ✅ |
| Eliminar acceso (REVOKED) | ✅ |
| PermissionEngine (ADMIN) | ✅ |
| Aceptar invitación (invitee) | ✅ |
| Rules + indexes | ✅ |

---

## Flujo

### Admin (company ACTIVE + rol ADMIN)

1. Home → Administración → **Usuarios de la empresa** (`platform_user_management`)
2. FAB **Invitar** → email + rol (ADMIN / MANAGER / EMPLOYEE / CLIENT)
3. Crea doc `company_invites/{id}` status `PENDING`
4. Lista miembros (`companies/{id}/memberships`) con acciones:
   - **Activar** → `ACTIVE` + assignment ACTIVE  
   - **Desactivar** → `SUSPENDED`  
   - **Eliminar acceso** → `REVOKED`  
   - **Cambiar rol** → `roleCodes` + `RoleRepository.upsertAssignment`
5. No puede desactivarse / eliminarse a sí mismo

### Invitee

1. Login / pantalla “Configura tu empresa”
2. `PendingInvitesSection` lista invites por Auth email
3. **Aceptar e ingresar** → membership ACTIVE + espejo `users/{uid}/company_memberships` + assignment + `setActiveCompany` → hub

---

## Integraciones

| Capa | Uso |
|------|-----|
| **Company** | Memberships + invites scoped a `companyId`; session bind exige ACTIVE |
| **Roles** | `BaseRoleCodes` / `CompanyRoleCodes`; `upsertAssignment` en accept/role/status |
| **PermissionEngine** | UI gated con `NavPermissionFactory.showAdmin` (ADMIN / SUPER_ADMIN) |

---

## Persistencia

| Path | Uso |
|------|-----|
| `company_invites/{inviteId}` | Invite descubrible por email (sin membership previo) |
| `companies/{id}/memberships/{uid}` | Estado y roles del miembro |
| `users/{uid}/company_memberships/{id}` | Espejo escrito por el **invitee** al aceptar |
| `companies/{id}/role_assignments/{uid}` | Assignment IAM |

---

## Rules / indexes

**firestore.rules**

- `company_invites`: create por miembro ACTIVE; read/update por miembro o por email Auth
- `memberships` update: invitee puede aceptar INVITED/PENDING → ACTIVE

**firestore.indexes.json**

- `company_invites`: `(email, status)`, `(companyId, status)`

Deploy: `firebase deploy --only firestore:rules,firestore:indexes`

---

## Archivos principales

```
platform/users/data/UserManagementRepository.kt
platform/users/viewmodel/UserManagementViewModel.kt
platform/users/viewmodel/PendingInvitesViewModel.kt
platform/users/ui/PlatformUserManagementScreen.kt
platform/users/ui/PendingInvitesSection.kt
platform/company/model/CompanyInvite.kt
navigation/Screen.kt → PlatformUserManagement
ui/screens/admin/AdminScreen.kt
ui/screens/home/HomeScreen.kt (pending invites)
firestore.rules
firestore.indexes.json
```

---

## Limitaciones v1

- Rules de invite: cualquier miembro ACTIVE puede crear invite (UI restringe a ADMIN).
- Mirror de status/rol al desactivar otro usuario: best-effort (solo el dueño puede escribir su espejo); bind real valida membership ACTIVE.
- Sin email transaccional (invite = doc Firestore; el invitee debe abrir la app).
- Aprobación legacy (`usuarios.isApproved`) sigue separada en Admin.

---

*User Management v1. Compilación verificada.*
