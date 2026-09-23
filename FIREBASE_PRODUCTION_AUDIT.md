# FIREBASE PRODUCTION AUDIT — NexoGo

**Rol:** Firebase Architect  
**Fecha:** 2026-09-23 (actualizado: FR-01/02, AU-01/02 **CERRADOS** en código)  
**Alcance:** Configuración en repo + alineación con app Platform V1 (post cleanup)  
**Método:** Revisión de `firestore.rules`, `storage.rules`, `firestore.indexes.json`, `google-services.json`, Auth en cliente, sesión company / tenant guard  
**Clasificación:** **OK** · **WARNING** · **CRITICAL** · **CLOSED**  
**No modifica** infraestructura desplegada (auditoría estática). **Deploy cloud pendiente** para rules.

---

## Veredicto ejecutivo

| Dimensión | Estado | Nota |
|-----------|--------|------|
| Firestore Rules (estructura deny-by-default) | **WARNING** | FR-01/FR-02 cerrados en repo; quedan WARNING legacy/settings/IAM |
| Storage Rules (tenant path) | **WARNING** | `companies/{id}/**` OK; legacy paths + sin límites de archivo |
| Auth | **OK (Platform)** | AU-01/02 cerrados: Auth + membership ACTIVE + company session |
| Indexes | **WARNING** | Archivo + `firebase.json` presentes; deploy no verificable |
| Company Isolation | **OK (repo)** | Create membership: bootstrap / invite / admin only (FR-01) |

**Producción abierta / multi-tenant de pago:** **NO GO** hasta **deploy** de rules + WARNING residuales revisados.  
**Piloto cerrado:** **GO condicionado** a deploy rules + build firmado + QA (`RC1_CHECKLIST` §8).

---

## Resumen de hallazgos

| Sev | Cantidad |
|-----|----------|
| CRITICAL (abiertos) | 0 |
| CRITICAL (cerrados en código) | 4 (FR-01, FR-02, AU-01, AU-02) |
| WARNING | 14 |
| OK | 9+ |

---

## 1. Firestore Rules

**Archivo:** `firestore.rules` (S0 Secure, deny-by-default + catch-all final `false`).

| ID | Sev | Hallazgo | Evidencia | Impacto producción |
|----|-----|----------|-----------|-------------------|
| **FR-01** | **CLOSED** | Self-join eliminado + harden update/invite/company create | bootstrap / invite+`inviteId` / ADMIN; catch-all excluye memberships; invitee no reabre PENDING; company.createdBy == uid | Requiere **deploy** |
| **FR-02** | **CLOSED** | `mensajes/{id}` deny total | `allow read, write: if false` | Requiere **deploy** |
| **FR-03** | **WARNING** | `companies/.../settings` create si solo `isSignedIn()` | L140+ | Create de settings sin ser miembro (KI-018) |
| **FR-04** | **WARNING** | Cualquier miembro ACTIVE puede `write` en `/{subcollection}/{docId}` sin check de rol | catch-all tenant | IAM de app no enforced en servidor (KI-007) |
| **FR-05** | **WARNING** | Colecciones legacy root (`patients`, `appointments`, `sales`, …) siguen abiertas a `isLegacyStaff()` | legacy block | Superficie de datos legacy aunque UI vet se eliminó |
| **FR-06** | **WARNING** | Catálogo IAM: cualquier signed-in puede `create` en `permissions_catalog` / `platform_roles` / `role_permissions` | IAM block | Contaminación / DoS de catálogo (update/delete bloqueados OK) |
| **FR-07** | **WARNING** | `company_index` create: signed-in + `companyId` match, sin membership | company_index | Índices huérfanos / ruido |
| **FR-08** | **OK** | Deny-by-default final `match /{document=**} { allow: if false }` | final | No hay catch-all auth abierto |
| **FR-09** | **OK** | Lectura/update de company doc exige `isCompanyMember` | companies | |
| **FR-10** | **OK** | Invites: create por miembro; read por email token o miembro | company_invites | Patrón razonable para discoverability |
| **FR-11** | **OK** | `users/{uid}` y espejo `company_memberships` solo owner | users | Correcto; implica KI-005 (admin no escribe espejo ajeno) |

### Validación isolation (rules)

| Escenario | Resultado esperado hoy (repo) |
|-----------|-------------------------------|
| User A ACTIVE en company1 lee `companies/company1/clients` | Allow |
| User A lee `companies/company2/clients` sin membership | Deny |
| User A sin membership crea `memberships/A` ACTIVE en company2 | **Deny (FR-01 CLOSED)** |
| Unauthenticated | Deny (salvo que no haya rules desplegadas) |

---

## 2. Storage Rules

**Archivo:** `storage.rules`

| ID | Sev | Hallazgo | Evidencia | Impacto |
|----|-----|----------|-----------|---------|
| **ST-01** | **OK** | Tenant: `companies/{companyId}/{allPaths=**}` exige membership ACTIVE | L53–55 | Alineado a `DocumentStoragePaths` |
| **ST-02** | **OK** | Deny final fuera de allow-lists | L110–112 | |
| **ST-03** | **WARNING** | Paths legacy (`patients`, `medical_records`, `chats`, `inventory`, …) siguen permitidos a staff legacy | L63–97 | Datos legacy accesibles vía Storage API |
| **ST-04** | **WARNING** | Sin validación `request.resource.size` / `contentType` | — | Uploads grandes / tipos peligrosos (coste + OOM cliente) |
| **ST-05** | **WARNING** | `public/{fileName}` readable por cualquier signed-in | L103–106 | Ampliar superficie si se usa |
| **ST-06** | **OK** | Profile bajo `users/{uid}/profile` solo owner | L59–61 | |

### Alineación app ↔ Storage

| Path app | Rules |
|----------|-------|
| `companies/{cid}/documents/...` | Cubierto por ST-01 |
| Guard `TenantStoragePaths.assertBelongsToCompany` | Defensa en cliente; **no sustituye** rules |

---

## 3. Auth

| ID | Sev | Hallazgo | Evidencia | Impacto |
|----|-----|----------|-----------|---------|
| **AU-01** | **CLOSED** | Login no gatea por `isApproved`; Auth → Home; hub por company session | `SimpleLoginScreen` + `FirebaseAuthRepository.loginUser` sin check | Política Platform |
| **AU-02** | **CLOSED** | Registro sin dependencia funcional de `isApproved` | `RegisterScreen` + `registerUser` escriben campo inerte | KI-003 cerrado |
| **AU-03** | **WARNING** | `applicationId` / package = `com.example.nexogo` | `google-services.json` + `build.gradle.kts` | No apto Play Store; mezcla percepción “demo” |
| **AU-04** | **WARNING** | Proyecto Firebase único en cliente: `nexogo-82003` | `google-services.json` | Sin evidencia de separación pilot/prod en repo |
| **AU-05** | **WARNING** | Sin App Check / MFA en producto | Beta-1 scope; release plan | Abuso de API / credenciales robadas |
| **AU-06** | **WARNING** | Deploy rules/indexes no verificado en cloud | `firebase.json` añadido; falta evidencia deploy | Ops RC1 |
| **AU-07** | **OK** | Flujo principal: Email/Password Firebase Auth | `signInWithEmailAndPassword` / `createUserWithEmailAndPassword` | Adecuado para piloto |
| **AU-08** | **OK** | Sesión post-login exige company ACTIVE en hub (app) | `CompanySessionManager.bindCompany` | Cierre de “fail-open” de producto |

**Política Auth Platform (oficial):** FirebaseAuth `currentUser` + membership **ACTIVE** + company session válida. `isApproved` = legacy only.

---

## 4. Indexes

**Archivo:** `firestore.indexes.json` (presente; **deploy no verificado** en esta auditoría).

| ID | Sev | Hallazgo | Detalle |
|----|-----|----------|---------|
| **IX-01** | **WARNING** | Deploy status desconocido | Sin `firebase.json` ni evidencia de `firebase deploy --only firestore:indexes` |
| **IX-02** | **OK** | Indexes para `company_invites` (email+status, companyId+status) | Cubren list pending invites |
| **IX-03** | **OK** | Indexes clients/records/documents (+ document_links) | Cubren filtros Platform list |
| **IX-04** | **WARNING** | Indexes `tasks`, `leads`, `opportunities`, `messages`, `notifications` | Módulos UI/repos eliminados o no en hub; ruido / coste de mantenimiento |
| **IX-05** | **WARNING** | Muchos índices usan `companyId` en collection group | Queries actuales suelen ser subcolección bajo `companies/{id}/…` (path ya aísla); indexes útiles sobre todo para collection group / filtros compuestos — validar en runtime missing-index |
| **IX-06** | **WARNING** | Listados hub sin `orderBy`+`limit` fuertes | Riesgo de queries full-scan (ver PERFORMANCE plan); indexes no mitigan full get |

---

## 5. Company Isolation

### 5.1 Capas

| Capa | Mecanismo | Sev |
|------|-----------|-----|
| **App sesión** | `CompanySessionManager` exige membership ACTIVE; `TenantContext.bind` | **OK** |
| **App repos** | `TenantIsolationGuard` / `requireTenant` / paths `companies/{id}/…` | **OK** |
| **Storage paths** | `DocumentStoragePaths` bajo `companies/{cid}/…` | **OK** |
| **Firestore rules** | `isCompanyMember` en subcolecciones tenant | **OK** |
| **Bootstrap membership** | FR-01 CLOSED — bootstrap / invite / admin | **OK (repo)** |
| **IAM roles en writes** | Solo cliente / PermissionEngine nav | **WARNING** |
| **Mirror membership** | Solo el propio uid puede escribir espejo | **WARNING** (KI-005) |

### 5.2 Matriz de aislamiento

| Ataque | ¿Bloqueado? |
|--------|-------------|
| Leer otra company sin membership | Sí (rules) |
| Self-join a company ajena con `companyId` conocido | **Sí — FR-01 CLOSED** |
| Subir archivo a `companies/otra/...` | Deny sin membership ACTIVE |
| Admin company A invita solo por email | OK (invites) |
| Staff EMPLOYEE escribe clientes | Sí si ACTIVE (sin deny por rol) — WARNING producto |

### 5.3 Post-cleanup

UI legacy eliminada reduce *exposición accidental*, pero **rules legacy y Storage legacy siguen** → datos root aún accesibles vía SDK/API si el usuario es “legacy staff” en `usuarios.rol`.

---

## 6. Checklist de validación completa (ops)

Ejecutar en proyecto Firebase **destino** (pilot/prod). Marcar en go-live:

| # | Prueba | Esperado | ☐ |
|---|--------|----------|---|
| V1 | Consola: rules Firestore = contenido del tag release | Match hash/commit | ☐ |
| V2 | Consola: rules Storage = tag release | Match | ☐ |
| V3 | Indexes del JSON en estado **Enabled** | Sin failed | ☐ |
| V4 | Auth providers: solo Email/Password (u otros explícitos) | Sin anónimos abiertos | ☐ |
| V5 | User A company1 crea cliente OK | Success | ☐ |
| V6 | User B **sin** membership lee clients de company1 | `PERMISSION_DENIED` | ☐ |
| V7 | User B intenta create membership ACTIVE en company1 | **Debe DENY** tras fix FR-01 | ☐ |
| V8 | Invite email → accept → ACTIVE → acceso | Success | ☐ |
| V9 | Upload doc a path `companies/{cid}/documents/...` | Success | ☐ |
| V10 | Upload a `companies/{otra}/...` sin membership | Deny | ☐ |
| V11 | `google-services.json` package = APK firmado | Match | ☐ |
| V12 | Budget alerts + App Check plan documentado | Configurado / waiver | ☐ |

---

## 7. Clasificación para decisión de release

### CRITICAL — bloquean Production

_Ninguno abierto en código._ Cerrados: **FR-01**, **FR-02**, **AU-01**, **AU-02**.  
**Ops:** deploy de `firestore.rules` al proyecto RC1 sigue siendo bloqueante de efectividad en cloud.
### WARNING — piloto posible con mitigación; no Production amplia

- Settings create sin membership (FR-03)  
- Writes sin rol en rules (FR-04)  
- Legacy Firestore/Storage paths (FR-05, ST-03)  
- IAM catalog create / company_index create (FR-06, FR-07)  
- Storage sin size/type (ST-04)  
- Package `com.example` + un solo projectId en repo (AU-03, AU-04)  
- Sin App Check / MFA / firebase.json (AU-05, AU-06)  
- Indexes deploy + dead indexes (IX-01, IX-04)  

### OK — mantener

- Deny-by-default Firestore/Storage  
- Tenant path membership gate (cuando membership es legítima)  
- Invites model  
- Company session ACTIVE en app  
- Storage tenant path + client path asserts  
- Indexes base C/E/D + invites  

---

## 8. Acciones recomendadas (prioridad)

| Prio | Acción | Cierra |
|------|--------|--------|
| ~~P0~~ | ~~Restringir create membership~~ | **FR-01 CLOSED** |
| ~~P0~~ | ~~Deny `mensajes`~~ | **FR-02 CLOSED** |
| ~~P0~~ | ~~Unificar política Auth~~ | **AU-01/02 CLOSED** |
| P0 | **Deploy** `firestore.rules` (+ storage/indexes) a proyecto RC1 | Ops (efectividad FR-01/02) |
| P1 | `settings` create solo member o solo create company flow | FR-03 |
| P1 | Verificar deploy indexes Enabled | IX-01, AU-06 |
| P1 | Límites size/contentType en Storage | ST-04 |
| P2 | Deny o archivar rules legacy root + Storage legacy | FR-05, ST-03 |
| P2 | Role-aware writes (rules claims o admin-only mutaciones sensibles) | FR-04 |
| P2 | Separar proyectos Firebase pilot vs prod; rename applicationId | AU-03/04 |
| P3 | Limpiar indexes de tasks/CRM no usados | IX-04 |

---

## 9. Conclusión

**FR-01, FR-02, AU-01, AU-02 = CLOSED** en código (2026-09-23).  
Producción multi-tenant: aún **NO GO** por WARNING residuales + **deploy** no verificado.  
Piloto RC1: seguridad crítica de código lista; falta deploy rules, build firmado `0.1.0-rc1`, QA.

---

*Firebase Architect · audit estático · actualizar tras `firebase deploy --only firestore:rules`.*

*Auditoría estática de arquitectura Firebase. Validación runtime = checklist §6 en el proyecto desplegado.*
