# KNOWN ISSUES — NexoGo

**Rol:** QA Lead  
**Fecha:** 2026-09-20  
**Método:** Re-auditoría de código y `firestore.rules` (rutas vivas / platform).  
**Criterio:** Solo defectos **confirmados** con evidencia en repo. Sin sugerencias teóricas, sin arquitectura, sin módulos nuevos.

---

## Resumen

| Severidad | Cantidad |
|-----------|----------|
| CRÍTICO | 3 |
| ALTO | 5 |
| MEDIO | 7 |
| BAJO | 4 |

---

## CRÍTICO

### KI-001 — Login beta no aplica `isApproved`
**Estado:** CERRADO (política Platform) — acceso = FirebaseAuth + membership ACTIVE + company session. Gates `isApproved` eliminados de login/registro/nav. Campo retenido solo como legacy.

### KI-002 — Self-join: cualquier Auth puede crear membership ACTIVE
**Estado:** **CLOSED** (FR-01 en `firestore.rules`) — **requiere deploy** a Firebase para efectividad en cloud.
**Evidencia (antes):** create permitido con solo `uid == userId && status == ACTIVE`.
**Fix:** create solo CASO 1 bootstrap (`createdBy`) | CASO 2 invite PENDING + `inviteId` | CASO 3 ADMIN. Catch-all excluye `memberships`.
**App:** `acceptInvite` escribe `inviteId` en membership.

### KI-003 — Registro fuerza `isApproved = true` en sesión local
**Estado:** CERRADO — registro ya no depende de `isApproved` para onboarding; campo histórico inerte.

---

## ALTO

### KI-004 — Bind de company preferida no prueba otras memberships
**Evidencia:** `CompanySessionManager.ensureSessionForUser` (~L68–98): un solo `preferredId`; si `bindCompany` falla, no itera refs; deja `CompanySessionState(isReady = true, errorMessage = null)`. `listUserCompanyRefs` no filtra por ACTIVE.  
**Impacto:** Usuario con otra company ACTIVE puede quedar en onboarding / sin mensaje del fallo real.

### KI-005 — Mirror `users/.../company_memberships` no lo puede escribir el admin
**Evidencia:** `UserManagementRepository.changeRole` / `setMemberStatus` (~L268–280, ~L319–326) actualizan espejo de `userId` ajeno dentro de `runCatching`; rules `users/{userId}/company_memberships` solo `isOwner(userId)`.  
**Impacto:** Membership canónica cambia; espejo puede seguir ACTIVE → `listUserCompanyRefs` / restore desfasados.

### KI-006 — Re-aceptar invite tras REVOKED falla por rules
**Evidencia:** `acceptInvite` (~L157–179) hace `.set()` sobre membership REVOKED existente (update). Rules update del owner (~L63–69): solo `ACTIVE` propio o `INVITED|PENDING → ACTIVE` — no `REVOKED → ACTIVE`.  
**Impacto:** Tras “Eliminar acceso” + nueva invite, accept falla en Firestore.

### KI-007 — Pantallas platform sin gate de escritura por rol
**Evidencia:** `PlatformClientsScreen` / `PlatformRecordsScreen` / `PlatformDocumentsScreen`: sin `PermissionEngine` / `canCreate`. Rutas `platform_*` en `NavGraph` sin wrapper de permiso. Rules tenant: `read, write` si `isCompanyMember`.  
**Impacto:** Cualquier miembro ACTIVE que llegue a la ruta puede crear/subir; el hub solo oculta nav.

### KI-008 — Aprobar usuarios en Admin vía DataStore, no Firestore
**Evidencia:** `PersistentAuthViewModel.approveUser` / `rejectUser` (~L370+) mutan `_pendingUsers` + DataStore; no llaman `FirebaseAuthRepository.approveUser`.  
**Impacto:** UI de pendientes en `AdminScreen` puede “aprobar” sin cambiar `usuarios.isApproved`.

---

## MEDIO

### KI-009 — Picker de clientes en expedientes limitado a 8
**Evidencia:** `PlatformRecordsScreen.kt` (~L117) `clientsUi.clients.take(8).forEach`.  
**Impacto:** Con >8 clientes no se pueden seleccionar los restantes al crear expediente.

### KI-010 — Upload puede dejar huérfano en Storage / link ignorado
**Evidencia:** `DocumentRepository.uploadAndRegister` (~L168–180): `putBytes` luego `registerUpload().getOrThrow()` sin delete compensatorio. Fallos de `linkDocument` posteriores no fallan el `Result`.  
**Impacto:** Bytes en Storage sin metadata; o éxito UI sin link.

### KI-011 — Listeners clients/records ocultan errores
**Evidencia:** `ClientRepository.listenClients` (~L157–160): `error != null` → `trySend(emptyList())`. Mismo patrón en `RecordRepository` listener.  
**Impacto:** UI muestra lista vacía ante PERMISSION_DENIED / red / índice.

### KI-012 — Splash siempre navega a Login
**Evidencia:** `MinimalSplashScreen` (~L77–79) `delay(2000)` → `onNavigateToLogin()`; no consulta `FirebaseAuth.currentUser`.  
**Impacto:** Cold start fuerza login aunque Auth tenga sesión.

### KI-013 — Home mezcla nav legacy y platform
**Evidencia:** `HomeScreen` drawer (~L480–488): “Pacientes” + “Clientes”; clínico legacy + “Expedientes” bajo gates similares.  
**Impacto:** Dos superficies de datos (root legacy vs `companies/{id}/…`).

### KI-014 — `PersistentAuthViewModel.login` es mock
**Evidencia:** `login()` (~L226+) `delay(1000)` + match DataStore; sin Firebase Auth.  
**Impacto:** Si alguna UI lo llama, “login” sin Auth. (Beta usa `SimpleLoginScreen`.)

### KI-019 — `acceptInvite` ignora fallo de `upsertAssignment`
**Evidencia:** `UserManagementRepository.acceptInvite` (~L196–208): `upsertAssignment` sin chequear `Result`; retorna `success(membership)`.  
**Impacto:** Entrada a company sin assignment IAM coherente.

---

## BAJO

### KI-015 — KDoc obsoleto en Client/Record ViewModels
**Evidencia:** Comentarios “Not wired to UI navigation yet” en `ClientViewModel` / `RecordViewModel`; pantallas ya en `NavGraph`.  
**Impacto:** Solo documentación engañosa.

### KI-016 — TODos / stubs en pantallas legacy alcanzables
**Evidencia:** p.ej. `modules/chat/ChatScreen.kt` `senderId == "current_user_id"`; History/Inventory/Medical TODs; `NavGraph` TODOs de reportes/PDF.  
**Impacto:** Features incompletas si el usuario entra por KI-013.

### KI-017 — Upload carga archivo entero en RAM
**Evidencia:** `DocumentViewModel.uploadFromUri` (~L96) `readBytes()`.  
**Impacto:** OOM con archivos grandes (sin tope en código).

### KI-018 — `settings` de company: create si solo `isSignedIn`
**Evidencia:** `firestore.rules` `settings` (~L75) `allow create: if isSignedIn()`.  
**Impacto:** Create de settings sin membership (menor que KI-002, real).

---

## Fuera de alcance de este archivo

- Ideas de features / módulos nuevos  
- Deuda arquitectónica genérica  
- “Falta X” sin bug observable en código  

---

## Prioridad de fix (piloto)

1. **KI-002** (rules self-join)  
2. **KI-001 / KI-003** (approval / login)  
3. **KI-006 / KI-005 / KI-019** (invite / mirror / assignment)  
4. **KI-007 / KI-013** (superficie Beta-1)  
5. **KI-004, KI-009, KI-010, KI-011**  

---

*Catálogo QA. Re-verificado 2026-09-20. Cerrar cada KI con referencia a fix.*
