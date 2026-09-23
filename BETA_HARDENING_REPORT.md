# BETA Hardening Report — Auditoría de uso real

**Rol:** Senior QA Engineer  
**Fecha:** 2026-09-19  
**Alcance:** Flujos de producto beta post S0/S1/S2  
**Método:** Auditoría estática de código (call chains UI → VM → Repo → Rules). **Sin modificar código.**  
**Build de referencia:** `:app:compileDebugKotlin` SUCCESS (S2)

---

## Veredicto ejecutivo

| # | Flujo | ¿Happy path usable en app? | Madurez | Bloqueante piloto multi-usuario |
|---|--------|----------------------------|---------|----------------------------------|
| 1 | Creación de empresa | **Parcial** (auto-bootstrap silencioso) | Media | No (1 admin OK) |
| 2 | Invitación de usuarios | **No** (modelo + rules parcial; sin producto) | Muy baja | **Sí** |
| 3 | Login | **Sí** | Media-alta | No |
| 4 | Permisos | **Parcial** (solo hub/nav) | Baja-media | Sí si hay roles ≠ ADMIN |
| 5 | Clientes | **Sí** (list + create) | Media | No |
| 6 | Expedientes | **Sí** (list + create ligado a cliente) | Media | No |
| 7 | Documentos | **Sí** (list + upload) | Media | No |

**Conclusión:** El piloto **1 usuario = 1 company auto-creada** puede recorrer Cliente → Expediente → Documento. Un piloto con **varios empleados en la misma company** está **bloqueado** por ausencia de invitación/join y por un espejo de memberships (`users/{uid}/company_memberships`) que el invitador **no puede escribir** bajo rules actuales.

**Aclaración:** la aprobación legacy de usuarios (`UserApprovalScreen` → `usuarios.isApproved`) **no** es invitación a company; no escribe `memberships`.

---

## 0. Mapa de entrada real (beta)

```
Splash → SimpleLoginScreen (o Register)
       → PersistentAuthViewModel.setCurrentUser
       → CompanySessionManager.ensureSessionForUser
       → HomeScreen (gate hasActiveCompany)
            ├─ Clientes     → PlatformClientsScreen
            ├─ Expedientes  → PlatformRecordsScreen
            └─ Documentos   → PlatformDocumentsScreen
```

Fuentes: `NavGraph.kt`, `SimpleLoginScreen.kt`, `PersistentAuthViewModel.kt`, `CompanySessionManager.kt`, `HomeScreen.kt`.

---

## 1. Flujo completo — Creación de empresa

### 1.1 Happy path (como está implementado)

1. Usuario autenticado llega a Home / `setCurrentUser`.
2. `PersistentAuthViewModel.bindCompanySession` → `CompanySessionManager.ensureSessionForUser(userId, displayName)`.
3. Si no hay refs en `users/{uid}/company_memberships` (o bind previo falla):
   - `CompanyRepository.createCompany(name = "Empresa de {nombre}" | "Mi Empresa NexoGo", industryPacks = [VETERINARY])`.
4. Batch escribe:
   - `companies/{cmp_…}`
   - `companies/{id}/settings/main`
   - `company_index/{id}`
   - `companies/{id}/memberships/{uid}` con `status=ACTIVE`, `roleCodes=[ADMIN]`, `invitedBy=self`
   - espejo `users/{uid}/company_memberships/{companyId}`
5. `bindCompany` exige membership **ACTIVE**; setea `TenantContext`; seed IAM + `upsertAssignment`.
6. Home muestra nombre de company y módulos platform.

**No hay wizard UI.** La “creación” es efecto colateral del bootstrap de sesión.

### 1.2 Errores posibles

| Error | Comportamiento actual |
|-------|----------------------|
| `PERMISSION_DENIED` en create (rules no desplegadas / Auth null) | `ensureSessionForUser` catch → sesión `isReady=true` **sin** company + `errorMessage` |
| Batch parcial / red | Result.failure; hub bloqueado con Reintentar / Cerrar sesión |
| Nombre vacío / “Usuario” | Nombre por defecto “Mi Empresa NexoGo” (no valida industria ni plan) |
| Company existe en refs pero membership no ACTIVE | Bind falla; código intenta **crear otra** company (puede proliferar tenants huérfanos) |
| Seed roles falla | Solo `Log.w`; sesión company sigue |

### 1.3 Edge cases

- Usuario con **varias** companies en espejo: elige last prefs → default → first; **sin switcher UI** (TOP_20 #20).
- Reintento tras fallo de create: puede crear **segunda** company si el fallo fue después del write o si refs están desync.
- `createCompany` permite `createAdminMembership=false`, pero el session manager siempre crea con admin.
- Rules: `companies` create solo exige `request.resource.data.id == companyId` (cualquier auth puede crear company).
- Rules: `settings` **create** si `isSignedIn()` **sin** ser miembro (sobre-permiso bootstrap).
- `CompanySessionState.hasActiveCompany` solo chequea `companyId` + `company` **no nulos**; **no** revalida `membership.status == ACTIVE` en el getter (la validación ocurre solo en `bindCompany`).
- `setActiveCompany()` existe en session manager pero **no tiene UI** (sin switcher).
- Si hay ref preferida pero bind falla (membership no ACTIVE / company missing) → fallthrough a **otro** `createCompany` → riesgo de **tenants huérfanos**.

### 1.4 Validaciones faltantes

- [ ] UI explícita: nombre, industria, confirmación, “ya tengo empresa / unirme”.
- [ ] Idempotencia: no crear company nueva si create previo quedó a medias.
- [ ] Validar unicidad / límites por usuario (anti-spam de companies).
- [ ] Renombrar / settings post-create en producto.
- [ ] Diferenciar “sin membership” vs “INVITED/PENDING” en UX (hoy ambos = bloqueo genérico).
- [ ] No auto-crear company si el usuario debería **aceptar invitación**.

### 1.5 Severidad hardening

**P0 producto** para onboarding consciente; **P1** para piloto single-admin (auto-create basta).

---

## 2. Flujo completo — Invitación de usuarios

### 2.1 Happy path

**No existe en producto.**

**No confundir con:** `RegisterScreen` → `usuarios.isApproved=false` + `UserApprovalScreen` / Admin “User Approval”. Eso es **aprobación global legacy**, no join a `companies/{id}/memberships`.

Lo que hay:

| Capa | Estado |
|------|--------|
| Modelo | `MembershipStatus.INVITED / PENDING / ACTIVE / SUSPENDED / REVOKED`; campos `invitedBy`, `approvedBy` |
| Repo | `upsertMembership(membership)` escribe membership + espejo en `users/{userId}/…` |
| UI | Ninguna pantalla invite / accept / approve / list members |
| Deep link / email / token | No |
| Session | `bindCompany` **solo** acepta `ACTIVE`; INVITED/PENDING → “Membership ACTIVE required” |

### 2.2 Errores posibles (si se intentara vía consola / API futura)

| Escenario | Resultado real |
|-----------|----------------|
| Admin crea `companies/X/memberships/B` ACTIVE | Rules **permiten** (`isCompanyMember`) |
| Admin intenta espejo `users/B/company_memberships/X` | Rules: solo `isOwner(B)` → **DENIED** |
| B hace login | `listUserCompanyRefs` vacío → **auto-crea su propia company** → nunca ve X |
| B tiene membership ACTIVE en X pero sin espejo | Igual: no descubre X por el camino actual |
| Membership INVITED | Bind rechaza; hub bloqueado; no UI “Aceptar invitación” |

**Hallazgo crítico:** el diseño de discoverability (`listUserCompanyRefs` sobre espejo writable solo por el invitee) **rompe** invitación admin→empleado sin un paso donde B escriba su propio espejo o un Cloud Function con Admin SDK.

### 2.3 Edge cases

- Rules permiten a **cualquier** miembro ACTIVE crear membership de terceros (sin check de rol ADMIN) → riesgo de privilege escalation lateral si hubiera UI.
- Invitee no puede ser encontrado por email en Auth desde cliente de forma segura (falta backend).
- `invitedBy` en createCompany del owner es self-invite semántico, no invitación.

### 2.4 Validaciones faltantes

- [ ] API `inviteMember(email|uid, roleCodes)` + estado INVITED.
- [ ] Flujo accept → ACTIVE + escritura espejo por el invitee (o Admin SDK).
- [ ] Approve/reject PENDING; suspend/revoke.
- [ ] UI miembros + roles.
- [ ] Rules: solo ADMIN puede crear memberships de terceros; campos status/roleCodes validados.
- [ ] Evitar auto-`createCompany` cuando exista invitación pendiente.
- [ ] Notificación al invitee.

### 2.5 Severidad

**P0 bloqueante** para beta multi-usuario. Workaround actual: seed manual en Firebase Console + posiblemente arreglar espejo a mano / Admin SDK.

---

## 3. Flujo completo — Login

### 3.1 Happy path

1. Splash → `SimpleLoginScreen`.
2. Validaciones locales: email/password no vacíos, `@` en email, password ≥ 6.
3. `FirebaseAuth.signInWithEmailAndPassword`.
4. Carga perfil `AppDataStore` o crea `User` básico (`role=USER`, `isApproved=true`, avatar Unsplash).
5. `PersistentAuthViewModel.setCurrentUser` → bind company session + `AuditLogger.login`.
6. `NavGraph` → Home (`popUpTo` Login inclusive).

Registro: `RegisterScreen` (ruta viva); puede ir a Home o Login según resultado.

### 3.2 Errores posibles

| Error | Manejo |
|-------|--------|
| Campos vacíos / email sin `@` / pass corta | Mensaje UI; no llama Auth |
| user-not-found / wrong-password / invalid-email / network | Mensajes mapeados (parcial; Firebase a menudo devuelve `INVALID_LOGIN_CREDENTIALS` genérico → cae en `else`) |
| user == null tras Auth | “Error al autenticar” |
| Excepción company bind | Log + audit con metadata; Auth ya navegó a Home → gate company |

### 3.3 Edge cases

- **Sin restore de sesión en Splash:** cold start siempre Login aunque Auth persista (sesión Firebase puede existir; UX pide login de nuevo / o DataStore desync).
- Login **no** usa `FirebaseAuthRepository.loginUser` en el camino vivo; bypass directo Auth + DataStore.
- **Bypass de aprobación:** `loginUser` sí bloquea `!isApproved`, pero el camino beta **no lo llama**. Registro deja `usuarios.isApproved=false`; `SimpleLoginScreen` con DataStore vacío crea User local con **`isApproved=true`** → entra al hub.
- Cold start `getUserById` puede cargar usuario no aprobado y aún así `bindCompanySession` (sin gate).
- Perfil DataStore de **otro** UID previo: se reescribe `id` al uid Auth (riesgo de datos cruzados locales).
- `authRepository` se instancia en `SimpleLoginScreen` pero **no se usa** para el sign-in.
- Logins experimentales fuera del grafo beta (OK S1); archivos legacy aún en disco.
- Logout: `PersistentAuthViewModel.logout` limpia Auth + DataStore + `clearSession` (verificar todos los botones UI).

### 3.4 Validaciones faltantes

- [ ] Restore session: Splash → si Auth.currentUser → Home + ensureSession.
- [ ] Mapear `auth/invalid-credential` (API moderna).
- [ ] Rate limit / lockout UX.
- [ ] Email verified (si política beta lo exige).
- [ ] Unificar login vía `FirebaseAuthRepository.loginUser` + sync `usuarios` → DataStore → `setCurrentUser`.
- [ ] Enforce `isApproved` en SimpleLoginScreen / `setCurrentUser` / `getUserById` de forma consistente.
- [ ] No navegar a Home hasta `companySession.isReady` (hoy race: Home puede mostrar loading pane — aceptable si estable).

### 3.5 Severidad

**P1** (usable); P0 si se exige sesión persistente o perfil Firestore canónico.

---

## 4. Flujo completo — Permisos

### 4.1 Happy path

1. Membership `roleCodes` (o legacy `UserRole` vía `LegacyRoleBridge`).
2. `NavPermissionFactory.forHub` → `PermissionEngine.fromRoleCodes` (bundles base).
3. Home filtra drawer / quick actions (`showPatients`→Clientes, `showClinical`→Expedientes, `showDocuments`, etc.).
4. En `bindCompany`: `seedPlatformFoundation` + `upsertAssignment` (idempotente create-oriented).

### 4.2 Errores posibles

| Error | Manejo |
|-------|--------|
| Seed / assignment fail | Warning log; nav sigue con codes de membership |
| roleCodes vacíos | Fallback ADMIN en bootstrap assignment; en nav usa LegacyRoleBridge |
| Usuario sin canEnter(CLIENTS) | No ve Clientes en hub |
| Deep link / ruta directa `platform_*` | **No re-chequea** PermissionEngine en composable |

### 4.3 Edge cases

- **Enforcement solo en nav:** `PlatformClients/Records/Documents` y repos **no** llaman `canCreate` / `PermissionChecker`.
- **Firestore rules = membership ACTIVE**, no keys del engine → cualquier miembro escribe clients/records/docs.
- **Records sin `PermissionModule.RECORDS`:** se expone con proxy `showClinical` (CLIENTS \|\| DOCUMENTS).
- SUPER_ADMIN bypass en engine; poco probable en piloto.
- Matrix en memoria desde bundles hardcode; no lee `role_permissions` de Firestore en runtime del hub.

### 4.4 Validaciones faltantes

- [ ] Gate FAB create/upload con `engine.canCreate(...)`.
- [ ] Check en ViewModel/Repository antes de write.
- [ ] Módulo RECORDS en PermissionEngine + factory dedicada.
- [ ] Deny en rules por rol (o Custom Claims) si el piloto diferencia staff.
- [ ] UI admin de roles / assignments.
- [ ] Proteger rutas `platform_*` con redirect si !canEnter.
- [ ] Tests de matriz por rol (ADMIN vs ASSISTANT vs CLIENT).

### 4.5 Severidad

**P0** si beta incluye roles restringidos. **P1** si todos son ADMIN (permisos “decorativos”).

---

## 5. Flujo completo — Clientes

### 5.1 Happy path

1. Home → Clientes (`platform_clients`) si `showPatients`.
2. `PlatformClientsScreen` + `ClientViewModel.bindCompany(companyId)`.
3. List / listen `companies/{id}/clients`.
4. FAB → nombre (+ phone/email opcionales) → `createPerson` → `ClientRepository.createClient` con `TenantIsolationGuard`.
5. Cliente aparece en lista.

### 5.2 Errores posibles

| Error | Manejo |
|-------|--------|
| Sin company | Error UI “No hay empresa activa” |
| PERMISSION_DENIED / red | `error` en uiState |
| Nombre vacío | Botón create deshabilitado / no envía (UI) |
| Índices faltantes en queries filtradas | Posible fallo list/filter |

### 5.3 Edge cases

- Solo tipo **persona** en UI; `createOrganization` / `createPetWithOwner` existen en VM sin UI cutover.
- Sin edit / archive / detail / contacts UI.
- `createdBy` puede ir vacío si `currentUser?.id` null (Auth ok pero ViewModel user no set).
- Listas **sin limit/paginación**.
- Search: path repo + filtro local; puede ser costoso.
- Comentario obsoleto en VM: “Not wired to UI” (ya wired S2).
- Listener Firestore con error → emite **lista vacía** sin `ui.error` (falso “sin clientes”).

### 5.4 Validaciones faltantes

- [ ] Email/phone format; duplicados.
- [ ] `canCreate(CLIENTS)` en FAB/VM.
- [ ] Edit + archive.
- [ ] Confirmación delete/archive.
- [ ] `.limit` + paging.
- [ ] Audit/notify en create.
- [ ] Bloquear screen si `LocalActiveCompany == null` (empty state fuerte).

### 5.5 Severidad

**P1** (happy path OK). P0 si se requiere integridad de contacto / anti-duplicado clínico.

---

## 6. Flujo completo — Expedientes (Records)

### 6.1 Happy path

1. Home → Expedientes si `showClinical`.
2. Carga records + clients de la company.
3. FAB → elegir cliente (lista clickable) + título + resumen → `createClinicalHistory`.
4. Persistencia bajo `companies/{id}/records/{id}` con `clientId`.

### 6.2 Errores posibles

| Error | Manejo |
|-------|--------|
| Sin clientes | Aviso “Crea un cliente primero…”; create disabled |
| Sin título / sin clientId | Create no dispara |
| Sin company | Error en VM |
| Fail write | `error` en uiState |

### 6.3 Edge cases

- Solo **clinical history** en UI; contract/invoice/report factories sin cutover.
- No valida que `clientId` pertenezca a la company (confía en lista cargada).
- Sin edit / finalize / archive / sections editor.
- Sin link a documentos desde UI.
- Picker muestra solo `take(8)` clientes → **clientes 9+ invisibles** para crear expediente.
- Permiso nav grueso (no módulo RECORDS).

### 6.4 Validaciones faltantes

- [ ] Selector completo / search de clientes (no tope 8).
- [ ] Verificar client pertenece al tenant en repo.
- [ ] `canCreate` records.
- [ ] Ciclo de vida (draft/final/archive) en UI.
- [ ] Adjuntar documento al crear/ver.
- [ ] Paginación lista.
- [ ] Audit.

### 6.5 Severidad

**P1** happy path; **P0 UX** el límite de 8 clientes en picker si el piloto crece.

---

## 7. Flujo completo — Documentos

### 7.1 Happy path

1. Home → Documentos si `showDocuments`.
2. FAB → `OpenDocument` picker → lee bytes → `DocumentViewModel.uploadFromUri` → `DocumentRepository.uploadAndRegister`:
   - `putBytes` Storage path tenant
   - `registerUpload` metadata Firestore
   - link opcional (API; **UI no pasa link**).
3. Reload lista.

### 7.2 Errores posibles

| Error | Manejo |
|-------|--------|
| Cancelar picker | No-op |
| No se puede leer Uri | Error “No se pudo leer el archivo” |
| bytes vacíos | Repo `require(bytes.isNotEmpty())` |
| Storage/Firestore DENIED | Error mensaje |
| Put OK + register fail | **Orphan file en Storage** posible |
| Register OK + link fail | Doc existe sin link (si se usara link) |

### 7.3 Edge cases

- Archivo grande: `readBytes()` en memoria → OOM riesgo; sin límite de tamaño.
- MIME: picker filtra tipos; `registerUpload` puede **rechazar** familia `OTHER` (PDF/Word/Excel/imagen). Desalineación picker vs gate de familia posible con `octet-stream`.
- Sin open/download/share/version UI; `downloadUrl` no se popula en este flujo.
- Folders/categories/tags modelados, no en cutover UI.
- `createdBy` vacío posible.
- Lista documentos: **one-shot** (no realtime listener); stale hasta re-entrar o re-upload.
- Storage rules: membership ACTIVE only (alineado tenant).
- Solo `MAX_FILE_NAME` en paths; no MAX file size.
- Roles con `READ_OWN` en catálogo: `listDocuments` **no** filtra por owner.

### 7.4 Validaciones faltantes

- [ ] Max size (p.ej. 10–25 MB) + progress.
- [ ] Transacción / compensación orphan (delete storage si register falla).
- [ ] `canCreate(DOCUMENTS)` en upload.
- [ ] Link UI a client/record.
- [ ] Preview / download.
- [ ] Virus/malware: fuera de alcance cliente; documentar.
- [ ] takePersistableUriPermission si se reusa Uri.

### 7.5 Severidad

**P1** happy path pequeño archivo; **P0** si se suben PDFs grandes sin límite.

---

## 8. Matriz de riesgos (priorizada)

| ID | Riesgo | Flujo | Sev | Evidencia |
|----|--------|-------|-----|-----------|
| H-01 | No hay invitación/join; multi-user imposible en app | Invite | P0 | Sin UI/API; espejo users solo owner |
| H-02 | Auto-create company oculta “unirme a empresa” | Company/Invite | P0 | `ensureSessionForUser` siempre create si no refs |
| H-03 | Permisos no enforced en write | Permisos + C/R/D | P0/P1 | Solo NavPermissionFactory |
| H-04 | Rules membership ≠ role matrix | Permisos | P0/P1 | `isCompanyMember` only |
| H-05 | Picker records `take(8)` | Records | P0 UX | `PlatformRecordsScreen` |
| H-06 | Upload sin size limit / orphan storage | Documents | P0/P1 | `readBytes` + put then register |
| H-07 | Session restore débil en Splash | Login | P1 | Siempre Login |
| H-08 | Company create sin UX / spam tenants | Company | P1 | Silent bootstrap |
| H-09 | CRUD incompleto (no edit/archive) | C/R/D | P1 | Screens cutover mínimas |
| H-10 | Sin módulo RECORDS en engine | Permisos | P1 | `showClinical` proxy |
| H-11 | Bypass `isApproved` en login beta | Login | P0/P1 | SimpleLogin ≠ `loginUser` |
| H-12 | Bind fallido → nueva company (huérfanos) | Company | P0 | `ensureSessionForUser` fallthrough |
| H-13 | Listeners clients/records fallan en silencio | C/R | P1 | empty list, no error |
| H-14 | `hasActiveCompany` no rechequea membership | Company | P1 | getter solo ids |

---

## 9. Happy path E2E viable hoy (single admin)

```
Login OK
 → auto company ACTIVE (ADMIN)
 → Crear cliente
 → Crear expediente (cliente ≤ #8 en lista cargada)
 → Subir documento pequeño
 → Re-login (manual) → datos persisten en misma company
```

**No viable hoy:** segundo usuario unido a la misma company por flujo de producto.

---

## 10. Recomendaciones de hardening (sin implementar aún)

### Must-fix antes de beta multi-usuario
1. Diseñar invite/accept (Cloud Function recomendada para espejo + email).
2. En `ensureSessionForUser`: **no** auto-create si hay INVITED/PENDING o código de invite; **no** fallthrough a create cuando bind de ref existente falla (evitar tenants huérfanos).
3. Gate writes con PermissionEngine + (opcional) Custom Claims/rules.
4. Límite tamaño upload + manejo orphan.
5. Quitar `take(8)` o paginar selector de clientes.
6. Unificar login con `loginUser` / enforce `isApproved` si el piloto usa aprobación legacy.

### Should-fix piloto single-admin
7. Restore sesión Splash.
8. Wizard nombre de empresa (o al menos editar nombre).
9. Edit/archive básico clients/records.
10. Link documento → cliente/expediente.
11. `.limit` en listados; errores de listener visibles.

### Nice-to-have
11. Role editor UI.
12. Company switcher (#20).
13. Audit visible en CUD.
14. Alinear mensajes Auth API nueva.

---

## 11. Criterio QA de “beta hardened”

| Check | Estado actual |
|-------|---------------|
| 1 admin: login → company → client → record → doc | **PASS probable** |
| 2º usuario same company vía app | **FAIL** |
| Rol no-admin no puede crear | **FAIL** (solo hide nav) |
| Upload > memoria / deny parcial | **Riesgo abierto** |
| Invitación documentada en BETA_ACCEPTANCE_TESTS | Cubierta como gap, no como PASS |

---

## 12. Archivos clave auditados

```
platform/company/session/CompanySessionManager.kt
platform/company/data/CompanyRepository.kt
platform/company/model/CompanyModels.kt
platform/company/ui/CompanyProvider.kt
ui/screens/auth/SimpleLoginScreen.kt
viewmodel/PersistentAuthViewModel.kt
navigation/NavGraph.kt
ui/screens/home/HomeScreen.kt
platform/role/nav/NavPermissionFactory.kt
platform/role/engine/PermissionEngine.kt
platform/clients/ui/PlatformClientsScreen.kt
platform/clients/viewmodel/ClientViewModel.kt
platform/records/ui/PlatformRecordsScreen.kt
platform/records/viewmodel/RecordViewModel.kt
platform/documents/ui/PlatformDocumentsScreen.kt
platform/documents/viewmodel/DocumentViewModel.kt
platform/documents/data/DocumentRepository.kt
firestore.rules
storage.rules
```

---

*Auditoría de uso real. No sustituye ejecución manual de `BETA_ACCEPTANCE_TESTS.md`; la complementa con gaps de producto/seguridad observados en código. Sin cambios de código en este entregable.*
