# MODULE INTEGRATION REPORT — NexoGo Platform Hub

**Rol:** Lead Engineer  
**Fecha:** 2026-09-21  
**Compilación:** `:app:compileDebugKotlin` — **BUILD SUCCESSFUL**  
**Objetivo:** Conectar Clients, Records, Documents, Users, Roles, Permissions a la navegación principal; eliminar rutas muertas del hub; preservar multiempresa, tenant isolation y security rules.  
**Regla:** Sin módulos de dominio nuevos; solo UI/nav sobre repos existentes.

---

## 1. Resumen ejecutivo

| Antes | Después |
|-------|---------|
| Hub mezclaba legacy (Citas, Pacientes, Inventario…) con platform | Hub **solo platform** |
| Users solo vía Admin | Users en drawer + quick actions |
| Roles / Permissions sin pantalla | `PlatformRolesScreen` + `PlatformPermissionsScreen` cableados |
| Rutas Screen huérfanas (logins experimentales, FirebaseTest, dashboards) | Eliminadas de `Screen.kt` |
| `PendingInvitesSection` sin call site | Cableado en pane “empresa requerida” |

**Preservado:** `CompanySessionManager` / `LocalActiveCompany`, paths `companies/{companyId}/…`, `firestore.rules` / `storage.rules` (sin cambios en este lote).

---

## 2. Módulos conectados a la navegación principal

| Módulo | Ruta | UI | Entrada hub | Gate |
|--------|------|-----|-------------|------|
| **Clients** | `platform_clients` | `PlatformClientsScreen` | Drawer + quick action | `NavPermissionFactory.showClients` |
| **Records** | `platform_records` | `PlatformRecordsScreen` | Drawer + quick action | `showRecords` |
| **Documents** | `platform_documents` | `PlatformDocumentsScreen` | Drawer + quick action | `showDocuments` |
| **Users** | `platform_user_management` | `PlatformUserManagementScreen` | Drawer + quick action (+ Admin) | `showUsers` (ADMIN+) |
| **Roles** | `platform_roles` | `PlatformRolesScreen` (**nuevo**) | Drawer + quick action (+ Admin) | `showRoles` (ADMIN+) |
| **Permissions** | `platform_permissions` | `PlatformPermissionsScreen` (**nuevo**) | Drawer + quick action (+ Admin) | `showPermissions` (ADMIN+) |

Flujo:

```
Login → (Company ACTIVE | Onboarding / Pending invites)
  → Home
       → Clientes | Expedientes | Documentos
       → Usuarios | Roles | Permisos   (ADMIN+)
```

---

## 3. Pantallas nuevas (sobre código existente)

### 3.1 `PlatformRolesScreen`

- **Repo:** `RoleRepository.listSystemRoles` / `listCustomRoles` / `seedPlatformFoundation`
- **Tenant:** lista custom roles de `LocalActiveCompany.id`
- **Admin:** botón “Sembrar roles y permisos base” (idempotente)

### 3.2 `PlatformPermissionsScreen`

- **Repo:** `RoleRepository.getPermissionCatalog` (fallback a `PermissionKeys.catalog()`)
- **Engine:** matriz efectiva vía `NavPermissionFactory.forHub` + `PermissionEngine.allowedActions`
- Muestra roles de membership + catálogo completo

---

## 4. Limpieza de rutas muertas / hub

### 4.1 Eliminadas de `Screen.kt` (sin composable en grafo)

| Ruta eliminada | Motivo |
|----------------|--------|
| `modern_login` / `ultra_simple_login` / `minimal_login` | Logins experimentales no cableados |
| `firebase_test` | Ya fuera del grafo (S1); objeto Screen muerto |
| `admin_dashboard` / `professional_dashboard` / `patient_dashboard` | Sin composable |
| `user_management` (legacy id) | Sustituido por `platform_user_management` |
| `chat` (ruta genérica) | Solo existen `chat_list` / conversation |

### 4.2 Desconectadas del hub Home (siguen en NavGraph por compat / Settings)

Legacy: Appointments, Patients, Clinical/History, Inventory, Sales, Chat, Reports, Admin, UserApproval.

No aparecen en drawer ni quick actions. Accesibles solo por deep-link o rutas internas (p.ej. Settings → legacy helpers).

### 4.3 Componentes re-cableados

| Componente | Estado |
|------------|--------|
| `PendingInvitesSection` | Home · pane sin company (`onJoined` → retry sesión) |
| `AdminScreen` | Links a Users + Roles + Permissions |

---

## 5. Multiempresa · Tenant · Security

| Capacidad | Estado post-integración |
|-----------|-------------------------|
| **Multiempresa** | Sesión por `CompanySessionManager`; invites pendientes en hub; sin switcher (igual que Beta-1) |
| **Tenant isolation (app)** | Screens usan `LocalActiveCompany` / `bindCompany(companyId)` |
| **Security rules** | **Sin modificar** `firestore.rules` / `storage.rules` en este cambio |
| **IAM UI** | PermissionEngine filtra hub; seed de foundation desde Roles (ADMIN) |

> Nota CTO: KI-002 (self-join membership) y demás KNOWN_ISSUES **siguen abiertos**; esta integración no los cierra.

---

## 6. Archivos tocados

| Archivo | Cambio |
|---------|--------|
| `platform/role/ui/PlatformRolesScreen.kt` | **Nuevo** |
| `platform/role/ui/PlatformPermissionsScreen.kt` | **Nuevo** |
| `platform/role/nav/NavPermissionFactory.kt` | Gates `showClients/Records/Users/Roles/Permissions` |
| `navigation/Screen.kt` | Rutas platform + purge dead objects |
| `navigation/NavGraph.kt` | Home → 6 módulos; composables Roles/Permissions |
| `ui/screens/home/HomeScreen.kt` | Hub platform-only + pending invites |
| `ui/screens/admin/AdminScreen.kt` | Cards Roles / Permissions |

---

## 7. Qué queda fuera (a propósito)

| Ítem | Motivo |
|------|--------|
| CRM / Tasks / AI / Chat platform / Dashboard platform UI | Repos sin cutover; no abrir en hub |
| Notification inbox / AuditViewer | Sin contrato hub |
| Borrar archivos `.kt` legacy | Compat; solo desconectados del hub |
| Fix KI security | Fuera de alcance de integración nav |
| Company switcher | TOP_20 #20 diferido |

---

## 8. Verificación

| Check | Resultado |
|-------|-----------|
| `:app:compileDebugKotlin` | **BUILD SUCCESSFUL** |
| Rutas platform en `NavGraph` | 6 composables + onboarding |
| Hub sin legacy en drawer/quick | Sí |
| Rules files unchanged | Sí |

### Smoke manual sugerido

1. Login admin → Home muestra Clientes, Expedientes, Documentos, Usuarios, Roles, Permisos.  
2. Abrir Roles → Sembrar → ver roles sistema.  
3. Abrir Permisos → ver catálogo + matriz.  
4. Staff EMPLOYEE → no ve Usuarios/Roles/Permisos.  
5. Usuario sin company → ve crear empresa + invitaciones pendientes.

---

## 9. Conclusión

La navegación principal queda alineada al **núcleo Platform** (C/E/D + Users + Roles + Permissions), con sesión multiempresa y aislamiento por `companyId` intactos. Las pantallas huérfanas de Screen y el menú legacy del hub fueron retiradas del producto visible; el código legacy permanece compilable pero fuera del camino principal.

*Integración de módulos existentes. No añade dominios nuevos.*
