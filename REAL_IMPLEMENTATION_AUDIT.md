# REAL IMPLEMENTATION AUDIT — NexoGo Platform

**Rol:** CTO + Lead Auditor  
**Fecha:** 2026-09-19  
**Alcance:** Solo código implementado bajo `app/src/main/java/com/example/nexogo/platform/` y sus puntos de enlace reales a la app (`MainActivity`, `NavGraph`, `PersistentAuthViewModel`, `HomeScreen`).  
**Fuera de alcance:** Diseño de módulos nuevos, planes `.md`, roadmaps.  
**Método:** Inventario de archivos, grep de call sites, lectura de repos/NoOp, `NavGraph`/`Screen`, `firestore.rules`/`storage.rules`.

---

## Veredicto en una línea

**Platform es una biblioteca de dominio compilable (~55 `.kt`) con 1 módulo parcialmente vivo (Company). El resto escribe/lee Firestore solo si alguien llama al repo desde código — la UI no lo hace.** Una empresa real hoy opera el **stack legacy vet**, no Platform.

---

## Criterios de columnas

| Columna | Significa |
|--------|-----------|
| **Diseñado** | Modelos / catálogo / paths existen en código |
| **Implementado** | Repository (u engine) con lógica Firestore/CRUD real, no solo interface |
| **Conectado** | Hay call site fuera del propio módulo hacia UI, auth o nav |
| **Funcional** | Un usuario puede completar un flujo E2E desde la app sin APIs internas |
| **Producción** | Seguro (rules tenant), delivery completo (push/storage/AI), sin stubs que mientan éxito |

Valores: **Sí** · **Parcial** · **No**

---

## 1. Componentes que existen realmente en código

**55 archivos `.kt` en `platform/`:**

| Submódulo | Archivos | Artefactos reales |
|-----------|----------|-------------------|
| company | 6 | `Company`, `CompanyRepository`, `CompanySessionManager`, `CompanyProvider` |
| tenant | 8 | `TenantContext`, `TenantFirestore`, `TenantAwareRepository`, `TenantIsolationGuard`, `TenantCollections` |
| role | 10 | `RoleRepository`, `PermissionChecker`, `PermissionEngine`, catalog/bundles |
| clients | 3 | `ClientRepository`, models, `ClientViewModel` |
| records | 3 | `RecordRepository`, models, `RecordViewModel` |
| documents | 3 | `DocumentRepository`, models, `DocumentStoragePaths` |
| chat | 5 | `ChatRepository`, models, paths, push envelope, `ChatDocumentShare` |
| dashboard | 2 | `DashboardRepository`, models (+ widgets placeholder) |
| tasks | 2 | `TaskRepository`, models |
| crm | 2 | `CrmRepository`, models |
| ai | 3 | `AIRepository`, models, `OpenAiClient` / `NoOpOpenAiClient` |
| audit | 5 | `AuditRepository`, `AuditLogger`, `AuditViewer`, IP provider |
| notifications | 3 | `NotificationRepository`, models, dispatchers |

**No existe** en Platform (solo nombre en `TenantCollections`): repos de `appointments`, `products`, `sales`, `services`, `stock_movements` bajo tenant.

---

## 2. Interfaces / placeholders / NoOp

| Componente | Tipo | Evidencia |
|------------|------|-----------|
| `NoOpOpenAiClient` | Stub red | Falla con `OpenAiNotEnabledException`; default en `AIRepository` |
| `NoOpPushDispatcher` / `NoOpEmailDispatcher` | Stub delivery | `Result.success(Unit)` sin enviar |
| `InAppNotificationDispatcher` | Marker | Persistencia la hace el repo; dispatcher no-op |
| `NoOpChatPushDispatcher` | Stub FCM | Chat platform no empuja notificaciones |
| `OpenAiClient` (interface) | Contrato | Sin implementación HTTP en app |
| `ChatPushDispatcher` (interface) | Contrato | Solo NoOp concreto |
| `NotificationDispatcher` (interface) | Contrato | NoOp + InApp marker |
| `EmptyAuditIpProvider` | Stub | IP vacía en audit |
| `DashboardWidgetCatalog.futurePlaceholders()` | UI data stub | Widgets futuros sin métrica real |
| `DocumentRepository.prepareUploadPath` | Path only | **No hay `putFile` / Storage upload** en el repo |
| `PermissionEngine` / `PermissionChecker` | Lógica pura | Sin enforce en nav/repos de producto |
| `LegacyRoleBridge` | Compat | Sin call sites de producto |
| `AuditViewer` | Estado + queries | Sin pantalla Compose / ruta |

---

## 3. Repositorios con lógica funcional (Firestore CRUD)

| Repository | LOC ~ | Métodos suspend públicos ~ | ¿CRUD Firestore real? | Limitación |
|------------|------:|---------------------------:|------------------------|------------|
| `CompanyRepository` | 274 | 9 | **Sí** | Usado por sesión |
| `RoleRepository` | 351 | 14 | **Sí** | Nadie llama `seed`/CRUD desde app |
| `ClientRepository` | 301 | 12 | **Sí** | Huérfano de UI |
| `RecordRepository` | 231 | 8 | **Sí** | Huérfano de UI |
| `DocumentRepository` | 582 | 21 | **Sí (metadata)** | Sin upload Storage |
| `ChatRepository` (platform) | 582 | 18 | **Sí** | Push NoOp; UI usa otro chat |
| `TaskRepository` | 451 | 17 | **Sí** | Huérfano |
| `CrmRepository` | 589 | 24 | **Sí** | Huérfano |
| `DashboardRepository` | 424 | 6 | **Sí** | Full-scan counts; UI no lo usa |
| `AIRepository` | 419 | 19 | **Sí (jobs/meta)** | Ejecución IA NoOp |
| `AuditRepository` | 287 | 10 | **Sí** | Solo login/logout vía Logger |
| `NotificationRepository` | 328 | 13 | **Sí (in-app)** | Push/email NoOp |
| `TenantAwareRepository` | 71 | base | Infra | No dominio |

**Conclusión:** 12 repos de dominio tienen lógica de escritura/lectura. **Ninguno de negocio (salvo Company vía sesión + Audit parcial) está invocado por la UI.**

---

## 4. ViewModels conectados a UI

| ViewModel / Viewer | Ubicación | ¿Importado fuera de `platform/`? | ¿Usado en Compose/Nav? |
|--------------------|-----------|----------------------------------|------------------------|
| `ClientViewModel` | platform/clients | **No** | **No** |
| `RecordViewModel` | platform/records | **No** | **No** |
| `AuditViewer` | platform/audit | **No** | **No** |
| (resto de módulos) | — | Sin VM platform | — |

VMs **sí conectados** a pantallas (todos **legacy**, no platform):  
`PersistentAuthViewModel`, `ClinicalRecordViewModel`, `FirebaseSalesViewModel`, `FirebaseInventoryViewModel`, `FirebaseAppointmentViewModel`, `AppointmentViewModel`, `DashboardViewModel`, `AuthViewModel`, `ProfileViewModel`, chat VMs de `modules/`, etc.

**Único enlace platform→UI Compose:** `CompanyProvider` + `LocalActiveCompany` en `HomeScreen` (muestra `companyName`).

---

## 5. Pantallas accesibles desde navegación

`NavGraph` start: `Screen.Login`. Rutas **composables** reales (legacy):

Auth: Splash*, Login, ModernLogin, UltraSimpleLogin, MinimalLogin, Register  
Hubs: Home, Dashboard  
Core: Profile, Appointments (+ create/edit), Patients (+ create/edit), ClinicalRecords (+ create/edit/view), History*, Inventory (+ product/category), Sales (+ sale/services), MedicalRecords, ChatList/New/Conversation, ChatbotConfig, Settings, Storage, ProductCategories, Reports*, Help, About, Admin, UserApproval, FirebaseTest  

\*Algunas rutas declaradas en `Screen.kt` pueden no tener deep-link desde Home; las listadas con `composable(...)` sí están en el grafo.

**Cero rutas** hacia Clients/Records/Documents/Tasks/CRM/AI/Audit/Notifications/Roles platform.

---

## 6. Colecciones Firestore utilizadas

### 6.1 Platform — paths en código (escritos **si** se llama al repo)

| Path | Quién |
|------|-------|
| `companies/{id}` | CompanyRepository |
| `companies/{id}/settings/main` | CompanyRepository |
| `companies/{id}/memberships/{userId}` | CompanyRepository |
| `company_index/{id}` | CompanyRepository |
| `users/{uid}/company_memberships/{companyId}` | CompanyRepository |
| `companies/{id}/clients\|contacts` | ClientRepository |
| `companies/{id}/records` | RecordRepository |
| `companies/{id}/documents` (+ versions, links, folders, tags, categories) | DocumentRepository |
| `companies/{id}/conversations` (+ messages), `chat_channels`, `chat_groups` | ChatRepository |
| `companies/{id}/tasks` (+ comments) | TaskRepository |
| `companies/{id}/leads\|opportunities\|pipeline_stages\|customer_journeys\|follow_ups` | CrmRepository |
| `companies/{id}/dashboard_layouts\|dashboard_snapshots` | DashboardRepository |
| `companies/{id}/ai_jobs\|ai_analyses\|document_summaries\|extracted_entities` | AIRepository |
| `companies/{id}/audit_logs` (+ `platform/audit_logs`) | AuditRepository |
| `companies/{id}/notifications` | NotificationRepository |
| `permissions_catalog`, `platform_roles`, `role_permissions` | RoleRepository |
| `companies/{id}/roles`, `role_assignments` | RoleRepository |

**Uso en runtime app hoy (evidencia de call sites):**  
`companies` / memberships / index / settings vía `CompanySessionManager` en login.  
`audit_logs` vía `AuditLogger.login/logout` en `PersistentAuthViewModel`.  
El resto: **código listo, sin caller de producto.**

### 6.2 Legacy — colecciones que la app **sí** usa en pantallas

Ejemplos en `repository/` / `core/`:  
`usuarios`, `users`, `citas`, `appointments`, `patients`, `products`, `sales`, `medical_records`, `messages`, `chats`/`mensajes`, `inventario`, `ventas`, `historial_clinico`, `servicios_veterinarios`, `diagnostics`, …

→ **Datos de negocio vivos = root collections legacy, no tenant.**

---

## 7. Reglas de seguridad pendientes

| Ítem | Estado actual | Pendiente |
|------|---------------|-----------|
| Catch-all Firestore | `match /{document=**} { allow read, write: if request.auth != null; }` | **Eliminar**; deny-by-default |
| Reglas `companies/{companyId}/**` | **No existen** | Membership-scoped R/W |
| Rules emergency `if true` | Archivo en repo | No desplegar / borrar |
| Storage `companies/...` | **No existen** (deny final) | Paths tenant + membership |
| Indexes compuestos | Sin `firestore.indexes.json` | Añadir para queries platform |
| Enforce Permission Engine | Solo cliente, no wired | Rules + app gate |
| Credenciales / mock en cold start | `MainActivity` + admin hardcoded | Quitar de release |

Hasta resolver esto, **ningún módulo Platform es “Producción = Sí”.**

---

## 8. Módulos que compilan pero no hacen nada (en producto)

Definición: código presente + build OK + **sin efecto observable** para el usuario (salvo company name / audit login).

| Módulo | Compila | Efecto en usuario hoy |
|--------|---------|----------------------|
| role / Permission Engine | Sí | Ninguno |
| clients (+ VM) | Sí | Ninguno |
| records (+ VM) | Sí | Ninguno |
| documents | Sí | Ninguno |
| chat (platform) | Sí | Ninguno (UI = chat legacy) |
| dashboard (platform) | Sí | Ninguno (UI = Dashboard legacy) |
| tasks | Sí | Ninguno |
| crm | Sí | Ninguno |
| ai | Sí | Ninguno (NoOp) |
| notifications | Sí | Ninguno (nadie llama `notify*`) |
| audit (más allá login/logout) | Sí | Parcial mínimo |
| billing | Solo `.md` | N/A código |

---

## 9. Qué puede usar hoy una empresa real

### Platform (multi-tenant)

| Capacidad | ¿Usable? |
|-----------|----------|
| Login / registro (legacy auth) | Sí |
| Auto-creación / bind de Company en sesión | Sí (fail-open si falla) |
| Ver nombre de empresa en Home | Sí |
| Audit login/logout en `audit_logs` | Sí (si Firestore acepta escritura) |
| Clients / Records / Docs / Tasks / CRM / Chat tenant / AI / Roles UI | **No** |
| Permisos engine en menú | **No** |
| Push / email platform | **No** |

### Legacy (lo que la empresa realmente opera)

Citas, pacientes, historial/clínico, inventario, ventas, chat root, reportes, settings, admin/test — **sí, vía NavGraph**, sobre colecciones root, con reglas permisivas.

---

## 10. Qué fallaría / rompería en producción

| Riesgo | Por qué |
|--------|---------|
| Fuga cross-tenant | Catch-all auth → cualquier user lee/escribe `companies/*` |
| Credencial admin conocida | `admin@nexogo.com` / `123456` en cliente |
| Mock/diagnostics en arranque | Contamina datos y carga Firebase |
| Documentos platform “subidos” | Solo metadata; Storage path sin `putFile` + sin rules path |
| AI “completado” | Jobs encolables; ejecución on-device siempre falla (NoOp) |
| Notificaciones “enviadas” | NoOp marca éxito sin FCM/email |
| Chat platform vs legacy | Dos mundos de datos; UI no usa tenant chat |
| Dashboard platform metrics | Full collection scans; no cableado; indexes ausentes |
| Queries compuestas | Sin indexes → fallos runtime al usar repos |
| Storage tenant | Deny-all sin match `companies/` → uploads fallan |
| Empresa sin membership | Sesión fail-open → app legacy sin aislamiento |
| IAM | PermissionEngine no bloquea acciones |

---

## Tabla maestra

| Módulo | Diseñado | Implementado | Conectado | Funcional | Producción |
|--------|:--------:|:------------:|:---------:|:---------:|:----------:|
| company | Sí | Sí | Sí | Parcial | No |
| tenant (infra) | Sí | Sí | Sí | Parcial | No |
| role / Permission Engine | Sí | Sí | No | No | No |
| clients | Sí | Sí | No | No | No |
| records | Sí | Sí | No | No | No |
| documents | Sí | Parcial | No | No | No |
| chat (platform) | Sí | Parcial | No | No | No |
| dashboard (platform) | Sí | Sí | No | No | No |
| tasks | Sí | Sí | No | No | No |
| crm | Sí | Sí | No | No | No |
| ai | Sí | Parcial | No | No | No |
| audit | Sí | Sí | Parcial | Parcial | No |
| notifications | Sí | Parcial | No | No | No |
| appointments/sales/inventory (tenant) | Parcial* | No | No | No | No |
| billing | Parcial† | No | No | No | No |

\*Solo constantes en `TenantCollections`.  
†Solo documento de arquitectura, sin código.

---

## Porcentaje real de avance — NexoGo Platform

### Metodología (explícita)

13 módulos de plataforma evaluados (company → notifications; excluye billing/docs-only).

Por módulo, score 0–1:

| Columna | Peso | Sí | Parcial | No |
|--------|------|----|---------|-----|
| Diseñado | 10% | 1.0 | 0.5 | 0 |
| Implementado | 25% | 1.0 | 0.5 | 0 |
| Conectado | 25% | 1.0 | 0.5 | 0 |
| Funcional | 25% | 1.0 | 0.5 | 0 |
| Producción | 15% | 1.0 | 0.5 | 0 |

### Scores por módulo

| Módulo | Score |
|--------|------:|
| company | 0.725 |
| tenant | 0.725 |
| role | 0.350 |
| clients | 0.350 |
| records | 0.350 |
| documents | 0.225 |
| chat | 0.225 |
| dashboard | 0.350 |
| tasks | 0.350 |
| crm | 0.350 |
| ai | 0.225 |
| audit | 0.600 |
| notifications | 0.225 |
| **Promedio** | **0.388** |

### Resultados oficiales

| Métrica | Valor | Lectura |
|---------|------:|---------|
| **Avance Platform (ponderado)** | **39%** | Fundación de código + casi nulo cutover |
| Solo “hay código / Diseñado+Implementado” | **~72%** | Infla; no es producto |
| Solo “Conectado+Funcional+Producción” | **~18%** | Realidad de valor para empresa |
| Producción = Sí (algún módulo) | **0 / 13** | **0%** ship-ready Platform |

### Cifra que debe usar el CTO

> **NexoGo Platform ≈ 39% de avance de foundation; ≈ 18% de valor de producto; 0% listo para producción multi-tenant.**

El porcentaje “bonito” (~70%+) solo cuenta archivos escritos. El porcentaje **real** para una empresa es el de **Conectado × Funcional × Producción**.

---

## Anexo A — Call sites platform fuera de `platform/`

| Call site | Qué usa |
|-----------|---------|
| `MainActivity` | `CompanyProvider` |
| `PersistentAuthViewModel` | `CompanySessionManager`, `AuditLogger` |
| `HomeScreen` | `LocalActiveCompany` (nombre) |

Nada más.

---

## Anexo B — Contraste Legacy vs Platform (hoy)

| Capacidad negocio | Legacy UI | Platform |
|-------------------|-----------|----------|
| Pacientes / clientes | Sí (`patients`) | Repo sí / UI no |
| Expedientes | Sí (clinical/history) | Repo sí / UI no |
| Chat | Sí (root) | Repo sí / UI no |
| Inventario / ventas | Sí | Solo constante tenant |
| Tareas / CRM / Docs / AI | No / parcial legacy | Repo sí / UI no |
| Multi-empresa | Nombre en Home | Sesión sí / aislamiento server no |

---

*Auditoría basada en código. Sin propuestas de módulos nuevos. Próximo informe de acción debería ser un plan de cutover P0 (rules + un módulo UI), no más foundations.*
