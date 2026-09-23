# V1 INTEGRATION REPORT — NexoGo Platform Coherent App

**Rol:** Lead Engineer  
**Fecha:** 2026-09-23  
**Compilación:** `:app:compileDebugKotlin` — **BUILD SUCCESSFUL**  
**Objetivo:** Una sola app coherente (entrada → post-login → hub Platform), sin romper Company / Roles / Tenant Isolation ni funcionalidades activas del núcleo.

---

## 1. Veredicto

NexoGo Platform queda como **única superficie navegable**:

```
Splash (sesión) → Login | Home
Login / Register → Home | CompanyOnboarding → Home
Home → Clients | Records | Documents | Users | Roles | Permissions
     → Profile | Settings → Help | About
```

Legacy (citas, pacientes, inventario, ventas, chat, reportes, admin approval) **ya no está en el grafo**. Company session, Roles/Permissions UI y aislamiento `companies/{companyId}` se mantienen.

---

## 2. Objetivos cumplidos

| # | Objetivo | Resultado |
|---|----------|-----------|
| 1 | Único punto de entrada | `MainActivity` → `NavGraph` → `SessionSplashScreen` (único launcher; test activity `exported=false`) |
| 2 | Único flujo post-login | Login/Register → **Home** (gate company) o **CompanyOnboarding**; sin Dashboard dual |
| 3 | Navegación consistente | Un solo `NavGraph` (~15 rutas); Home drawer/quick = mismos módulos |
| 4 | Eliminar rutas huérfanas | `Screen.kt` reducido; sin Dashboard redirect, sin experimental logins, sin legacy routes |
| 5 | Pantallas no accesibles | Sacadas del grafo (código legacy queda en disco como dead source; ver §6) |
| 6 | ViewModels no usados | Eliminados 4 `Firebase*ViewModel` sin referencias |
| 7 | Repositorios no usados | Eliminados módulos platform sin UI + 5 impls Hilt huérfanas + `FileUploadRepository` |
| 8 | Consolidar C / R / D | Rutas canónicas `clients` / `records` / `documents` desde Home |

---

## 3. Flujo de aplicación (contrato)

### 3.1 Entrada

1. `SessionSplashScreen` comprueba `FirebaseAuth.currentUser`.  
2. Sin sesión → `Login`.  
3. Con sesión → refresca `PersistentAuthViewModel` → `Home`.  
4. `Home` sin company ACTIVE → pane onboarding + `PendingInvitesSection`.

### 3.2 Post-login

| Origen | Destino |
|--------|---------|
| Login OK | `Home` |
| Register | `Home` o `CompanyOnboarding` |
| Onboarding complete | `Home` |
| Logout | `Login` (clear back stack) |

### 3.3 Hub operativo

| Ruta | Pantalla | Scope |
|------|----------|-------|
| `clients` | `PlatformClientsScreen` | Tenant |
| `records` | `PlatformRecordsScreen` | Tenant |
| `documents` | `PlatformDocumentsScreen` | Tenant |
| `users` | `PlatformUserManagementScreen` | Tenant + ADMIN |
| `roles` | `PlatformRolesScreen` | Tenant + ADMIN |
| `permissions` | `PlatformPermissionsScreen` | Tenant + ADMIN |

Gates: `NavPermissionFactory` + `CompanySession` / `LocalActiveCompany`.

---

## 4. Cambios principales (archivos)

| Archivo | Cambio |
|---------|--------|
| `navigation/Screen.kt` | Solo rutas V1 activas |
| `navigation/NavGraph.kt` | Reescrito Platform-only |
| `ui/screens/auth/SessionSplashScreen.kt` | **Nuevo** — router de sesión |
| `ui/screens/settings/PlatformSettingsScreen.kt` | **Nuevo** — settings sin legacy |
| `AndroidManifest.xml` | `FirebaseFullTestActivity` `exported=false` |
| `HomeScreen` | Ya platform-first (sin cambios de firma) |

### Eliminado (seguro, 0 call sites de producto)

**Platform (sin UI):**

- `platform/ai/`  
- `platform/crm/`  
- `platform/tasks/`  
- `platform/dashboard/`  
- `platform/notifications/`  
- `platform/chat/`  
- `platform/audit/viewer/AuditViewer.kt`  

**ViewModels:**

- `FirebaseChatViewModel`  
- `FirebaseMedicalRecordViewModel`  
- `FirebasePatientViewModel`  
- `FirebaseSettingsViewModel`  

**Repos huérfanos:**

- `repository/AuthRepository.kt` (Impl)  
- `LocalDataRepository.kt`  
- `PatientRepository.kt`  
- `ProductRepository.kt`  
- `UserRepository.kt`  
- `core/repository/firebase/FileUploadRepository.kt`  

### Conservado (activo)

- `platform/company` (+ onboarding, session)  
- `platform/tenant`  
- `platform/role` (+ UI Roles/Permissions)  
- `platform/clients` / `records` / `documents`  
- `platform/users`  
- `platform/audit` (AuditLogger + AuditRepository)  
- `platform/usage`  
- `firestore.rules` / `storage.rules` (sin tocar)

---

## 5. Qué no se borró (a propósito)

| Ítem | Motivo |
|------|--------|
| Fuentes bajo `modules/` y `ui/screens/{appointments,patients,…}` | Compilan aún como dead code; borrado masivo arriesga imports cruzados. **Inaccesibles** vía nav. |
| `viewmodel/ChatViewModel`, `AppointmentViewModel`, etc. | Referenciados por pantallas legacy aún en el árbol fuente |
| `TenantCollections` constantes CRM/AI/… | Catálogo infra; inofensivo |
| `PermissionModule.TASKS` etc. | Engine compartido; sin UI |

**Siguiente limpieza (opcional):** flavor `platformOnly` o mover `modules/` → `archived/` en un PR dedicado.

---

## 6. Coherencia vs deuda residual

| Dimensión | Estado |
|-----------|--------|
| Nav / producto visible | **Coherente V1** |
| APK size / dex | Aún incluye fuentes legacy no linkeadas desde NavHost |
| Performance hub | Pendiente `PERFORMANCE_HARDENING_PLAN` (limits/listeners) |
| Seguridad KI-002 etc. | Pendiente `KNOWN_ISSUES` |

---

## 7. Verificación

| Check | Resultado |
|-------|-----------|
| Compile debug Kotlin | **BUILD SUCCESSFUL** |
| Rutas en `Screen` | 15 (splash, auth, hub, C/R/D, IAM, settings) |
| Entrada launcher | Solo `MainActivity` |
| Company / Roles / Tenant | Intactos |

### Smoke manual sugerido

1. Cold start sin sesión → Splash → Login.  
2. Login → Home (o pane crear empresa / invitaciones).  
3. Abrir Clients → Records → Documents → back stack OK.  
4. Admin: Users / Roles / Permissions.  
5. Settings → Help / About / Logout → Login.  
6. Confirmar que no hay menú Citas/Pacientes/Inventario.

---

## 8. Conclusión

La app navegable es **Platform end-to-end**. Se unificó entrada y post-login, se consolidaron Clients/Records/Documents, y se eliminaron rutas, módulos platform sin UI, ViewModels y repos sin uso — sin romper compilación ni el núcleo Company / Roles / Tenant.

*Informe de integración V1. Fuentes legacy fuera del grafo quedan como limpieza diferida.*
