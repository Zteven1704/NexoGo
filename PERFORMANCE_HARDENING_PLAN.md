# PERFORMANCE HARDENING PLAN — NexoGo

**Rol:** Performance Engineer  
**Fecha:** 2026-09-23  
**Método:** Auditoría estática de código (`platform/`, `viewmodel/`, `repository/`, `modules/`, `navigation/`, pantallas hub)  
**Regla:** Sin funcionalidades nuevas. Solo endurecer, limitar, deduplicar y congelar lo existente.  
**Alcance prioritario:** Hub vivo (Clients / Records / Documents / Users / Roles / Permissions) + deuda que aún genera coste en APK.

---

## Veredicto

El hub Platform es usable, pero **lista sin `limit` + listener snapshot + `get()` inicial en el mismo `bindCompany`** multiplica lecturas Firestore. Varios ViewModels se crean con `remember { }` (no overviven con `onCleared` de Navigation) o son **singletons de proceso** que retienen estado/listeners. Hay **stacks duplicados** (chat/auth/pacientes) que inflan APK y confusión; la mayoría legacy ya no está en el hub, pero sigue en el grafo.

| Área | Riesgo hoy (hub) | Riesgo latente (repos no hub) |
|------|------------------|-------------------------------|
| Queries costosas | **ALTO** | **CRÍTICO** si se cablea Dashboard/CRM |
| Listeners | **ALTO** | MEDIO–ALTO (chat/tasks/notif) |
| Memory | **ALTO** (upload RAM + VM lifecycle) | ALTO (singletons legacy) |
| Nav / VM / Repo dup | **MEDIO** (hub limpio; grafo pesado) | ALTO |

---

## 1. Consultas Firestore costosas

### Hallazgos (evidencia)

| ID | Severidad | Evidencia | Problema |
|----|-----------|-----------|----------|
| **PF-Q01** | **ALTO** | `ClientRepository.listClients` — `query.get()` sin `.limit()` (~L100–115) | Carga **toda** la colección `clients` del tenant |
| **PF-Q02** | **ALTO** | `ClientRepository.searchClients` — llama `listClients` y filtra en cliente (~L121–127) | Doble coste: full read + CPU |
| **PF-Q03** | **ALTO** | `RecordRepository.listRecords` — `query.get()` sin limit (~L102–107) | Full scan de `records` |
| **PF-Q04** | **ALTO** | `DocumentRepository.listDocuments` — `query.get()` sin limit (~L289–297) | Full scan de `documents` (+ versions/folders/tags sin limit en otros métodos) |
| **PF-Q05** | **ALTO** | `ClientViewModel.bindCompany` → `loadClients` **y** `observeClients` (~L59–63) | **Get + Snapshot** sobre la misma colección al abrir Clientes |
| **PF-Q06** | **ALTO** | `RecordViewModel.bindCompany` → `loadRecords` **y** `observeRecords` (~L58–64) | Igual patrón get+listen |
| **PF-Q07** | **MEDIO** | `PlatformRecordsScreen` instancia `RecordViewModel` + `ClientViewModel` (~L55–56) | Dos binds → hasta 2 gets + 2 listeners al abrir Expedientes |
| **PF-Q08** | **MEDIO** | `UserManagementRepository` — `membershipsCol.get()` sin limit (~L39) | OK para ≤15 users; escala mal |
| **PF-Q09** | **MEDIO** | `RoleRepository.getPermissionCatalog` — `catalogCollection().get()` (~L86) | Full catalog en cada apertura de Permisos |
| **PF-Q10** | **MEDIO** | `AuditRepository` — `limit(limit * 3)` + sort/take en cliente (~L240–246) | Sobre-lectura deliberada |
| **PF-Q11** | **MEDIO** | `NotificationRepository` — `limit(limit * 2)` + sort cliente (~L148–158) | Idem |
| **PF-Q12** | **CRÍTICO*** | `DashboardRepository.countClients/Documents/Inventory/Sales` — `.get()` de colección completa (~L268–319) | Full-scan ×4 por métricas; **no en hub hoy** |
| **PF-Q13** | **ALTO*** | `CrmRepository` / `TaskRepository` listados sin limit (p.ej. leads/opportunities/tasks `.get()`) | Latente si se abre UI |
| **PF-Q14** | **BAJO** | `UsageRepository` increment: read-modify-write por evento (~L95) | Contención y lecturas extra bajo uso intenso |

\*Severidad si se usa; hoy no cableado al hub → tratar como **congelar / no invocar**.

### Plan de hardening (sin features nuevas)

| Prioridad | Acción | Target |
|-----------|--------|--------|
| P0 | Añadir `.limit(N)` por defecto (p.ej. 50–100) a `listClients` / `listRecords` / `listDocuments` | PF-Q01/03/04 |
| P0 | En `bindCompany`: **o** listener **o** one-shot get — no ambos | PF-Q05/06 |
| P1 | En Expedientes: cargar clientes con `listClients(limit=…)` one-shot; no `observeClients` | PF-Q07 |
| P1 | `searchClients`: no full-scan; buscar sobre página en memoria o query acotada existente | PF-Q02 |
| P2 | Cache en memoria del permission catalog por sesión de proceso | PF-Q09 |
| P2 | Alinear Audit/Notification a `limit` exacto + `orderBy` server-side (ya hay indexes) | PF-Q10/11 |
| P3 | Marcar `DashboardRepository` aggregations como **no usar en runtime** hasta count aggregates / snapshots | PF-Q12 |
| P3 | No cablear CRM/Tasks UI hasta tener limits | PF-Q13 |

---

## 2. Listeners innecesarios

### Hallazgos

| ID | Severidad | Evidencia | Problema |
|----|-----------|-----------|----------|
| **PF-L01** | **ALTO** | `listenClients` — snapshot sin limit sobre `listQuery` (~L155–169) | Listener de colección completa mientras la pantalla vive |
| **PF-L02** | **ALTO** | `listenRecords` — snapshot sin limit (~L208–214) | Idem records |
| **PF-L03** | **MEDIO** | `ClientViewModel.observeClients` + `loadClients` juntos | Primer snapshot ya trae datos; get inicial es redundante |
| **PF-L04** | **MEDIO** | `awaitClose { registration.remove() }` presente en platform listeners | Correcto **si** el Flow se cancela; roto si el VM no se limpia (ver §3) |
| **PF-L05** | **BAJO** | `listenConversationsForUser` / `listenMessages` / `listenTasks` / `listenForUser` (notif) | No hub; coste latente |
| **PF-L06** | **MEDIO** | Chat legacy `ChatViewModel.getInstance` — proceso-wide | Listeners/estado de chat pueden vivir tras salir de pantalla |

### Plan

| Prioridad | Acción |
|-----------|--------|
| P0 | Elegir modo lista hub: **snapshot-only** (cancel get) **o** **get-only** (sin listen) en Clients/Records |
| P0 | Añadir `.limit(N)` también a queries de snapshot (misma N que list) |
| P1 | Al salir de pantalla: garantizar cancel de `listenJob` vía `ViewModel.onCleared` **y** ownership Navigation (`viewModel()` / `ViewModelStoreOwner`) |
| P2 | No suscribir chat/tasks/notifications platform hasta producto |
| P2 | Dejar de usar `ChatViewModel.getInstance` en rutas legacy; scope por pantalla |

---

## 3. Memory leaks / lifecycle

### Hallazgos

| ID | Severidad | Evidencia | Problema |
|----|-----------|-----------|----------|
| **PF-M01** | **CRÍTICO** | `DocumentViewModel.uploadFromUri` — `readBytes()` (~L95–97) | Archivo entero en heap (alineado KI-017); OOM con PDF/fotos grandes |
| **PF-M02** | **ALTO** | Platform screens: `remember { ClientViewModel(...) }` / Record / Document / UserManagement | **No** es `androidx.lifecycle.viewmodel`; al abandonar composition puede no llamar `onCleared` de forma fiable como Nav back stack VM → jobs/listeners huérfanos o VMs “zombie” recreados |
| **PF-M03** | **ALTO** | Singletons: `PersistentAuthViewModel`, `AuthViewModel`, `ChatViewModel`, `PatientViewModel`, `ClinicalRecordViewModel`, `AppointmentViewModel`, `SettingsViewModel`, `ProductCategoryViewModel` (`getInstance`) | Estado + posibles listeners viven **toda la vida del proceso** |
| **PF-M04** | **MEDIO** | `NavGraph` crea `HistoryViewModel(historyRepository)` inline en composable (~L348, ~L389) | Nueva instancia por recomposición/entrada; sin store |
| **PF-M05** | **MEDIO** | `CompanySessionManager.getInstance(context)` + auth singleton | Aceptable con `applicationContext`; riesgo si algún call site pasa Activity |
| **PF-M06** | **BAJO** | Bytes retenidos en `UsageAnalytics` metadata size tras upload | Menor |

### Plan

| Prioridad | Acción |
|-----------|--------|
| P0 | Upload: stream / `putStream` / chunk; **tope de tamaño** antes de leer (reusa política &lt;5 MB del piloto) — sin feature nueva, solo harden path existente |
| P0 | Sustituir `remember { XViewModel() }` en pantallas platform por `viewModel()` (factory) scoped a la ruta Nav |
| P1 | Verificar `onCleared` cancela `listenJob` en Client/Record VM (ya hay hook en Client; auditar Record) |
| P1 | Congelar creación de nuevos singletons VM; migrar call sites hub lejos de `getInstance` donde duela |
| P2 | History: `viewModel` scoped o `remember` + clear explícito |
| P2 | Documentar: solo `applicationContext` en managers |

---

## 4. Navegación duplicada

### Hallazgos

| ID | Severidad | Evidencia | Problema |
|----|-----------|-----------|----------|
| **PF-N01** | **MEDIO** | `Screen.Dashboard` → redirect a Home (`NavGraph`) | Ruta fantasma; coste de composición extra |
| **PF-N02** | **MEDIO** | Legacy aún en `NavGraph`: Patients, Clinical, History, Appointments, Inventory, Sales, Chat, Reports | Dominios paralelos a Clients/Records; peso de grafo + riesgo de deep-link |
| **PF-N03** | **BAJO** | Home **y** Admin navegan a Users / Roles / Permissions | Duplicado de entrada (aceptable UX; no es leak) |
| **PF-N04** | **BAJO** | Archivos login experimentales (`ModernLoginScreen`, etc.) siguen en árbol fuente aunque fuera de `Screen` | APK / dex noise si no stripped |
| **PF-N05** | **MEDIO** | Pacientes legacy vs Clientes platform; Historial vs Expedientes | Dos modelos de datos para el mismo job de negocio |

### Plan

| Prioridad | Acción |
|-----------|--------|
| P1 | Mantener hub platform-only (ya hecho); **no re-enlazar** legacy |
| P1 | Feature-flag / build flavor: excluir composables legacy del grafo en build piloto/V1 |
| P2 | Eliminar redirect Dashboard o unificar startDestination |
| P2 | No borrar aún código legacy (riesgo); marcar `@Deprecated` / freeze |
| P3 | ProGuard/R8 shrink pantallas no referenciadas tras sacar del grafo |

---

## 5. ViewModels redundantes

### Inventario (duplicación real)

| Dominio | Clases | Severidad |
|---------|--------|-----------|
| Auth | `viewmodel.AuthViewModel`, `modules.auth.AuthViewModel`, `PersistentAuthViewModel` | **ALTO** |
| Chat | `viewmodel.ChatViewModel`, `modules.chat.ChatViewModel`, `FirebaseChatViewModel` | **ALTO** |
| Profile | `viewmodel.ProfileViewModel`, `modules.profile.ProfileViewModel` | **MEDIO** |
| Appointments | `AppointmentViewModel`, `modules…`, `AppointmentViewModelImproved`, `FirebaseAppointmentViewModel`, `SafeFirebaseAppointmentViewModel` | **ALTO** |
| Patients | `viewmodel.PatientViewModel`, `modules.patients.PatientViewModel`, `FirebasePatientViewModel` | **ALTO** |
| Inventory / Sales | dobles en `modules/` + `viewmodel/Firebase*` | **MEDIO** |
| Platform hub | `ClientViewModel`, `RecordViewModel`, `DocumentViewModel`, `UserManagementViewModel` | OK (únicos) — arreglar lifecycle |

### Plan

| Prioridad | Acción |
|-----------|--------|
| P0 | Hub: una sola cadena Auth → `PersistentAuthViewModel` |
| P1 | Inventario escrito: “VM canónica vs freeze” por dominio; no crear más `Firebase*ViewModel` |
| P1 | Rutas legacy que aún se abran: apuntar a **un** VM por dominio |
| P2 | Borrar o archivar VMs cero call sites tras grep (paso mecánico, no feature) |

---

## 6. Repositorios duplicados

### Inventario

| Dominio | Duplicados | Severidad |
|---------|------------|-----------|
| Chat | `platform.chat.data.ChatRepository`, `modules.chat.ChatRepository`, `repository.ChatRepository` | **ALTO** |
| Firebase core | `core.FirebaseRepository`, `repository.FirebaseRepository` | **MEDIO** |
| Firestore wrapper | `core…FirebaseFirestoreRepository` (object), `repository.FirebaseFirestoreRepository` (class) | **MEDIO** |
| Auth | `modules.auth.AuthRepository`, `FirebaseAuthRepository`, `AuthRepositoryImpl` | **ALTO** |
| Clientes vs Pacientes | `ClientRepository` (tenant) vs `PatientRepositoryImpl` / history `PetRepository` (root) | **ALTO** (datos + coste) |
| Expedientes | `RecordRepository` vs Clinical/History repos | **ALTO** |
| Sales / Inventory | `modules.*.data.*` + Firebase VMs directos | **MEDIO** |

### Plan

| Prioridad | Acción |
|-----------|--------|
| P0 | Hub solo habla repos **platform** tenant-scoped |
| P1 | Anotar repos no hub como `// FREEZE — do not call from hub` |
| P1 | Un solo entry point Auth/Firestore para nuevas llamadas |
| P2 | No migrar datos legacy aquí; evitar lecturas cruzadas platform+legacy en la misma pantalla |

---

## 7. Roadmap de hardening (sprints)

### H0 — Hotfix hub (1–2 días) · coste Firestore inmediato

1. Limits en `list*` + `listen*` de Clients / Records / Documents.  
2. Quitar doble carga get+listen en `bindCompany`.  
3. Tope + no `readBytes` ilimitado en upload.  
4. `viewModel()` scoped en las 6 pantallas platform.

**Éxito:** abrir Clientes/Expedientes/Documentos genera ≤ 1 query acotada (o 1 listener acotado), sin OOM en archivos &gt; tope.

### H1 — Lifecycle y singletons (3–5 días)

1. Cancel jobs en `onCleared`.  
2. Reducir uso de `getInstance` en pantallas aún alcanzables.  
3. History VM scoped en NavGraph.

### H2 — Congelar duplicados (sin borrar agresivo)

1. Flavor/grafo sin legacy composables.  
2. Lista freeze de Dashboard/CRM/Tasks/Chat platform aggregations.  
3. Deprecate Screen redirect Dashboard.

### H3 — Observabilidad

1. Log de lecturas estimadas por pantalla (debug).  
2. Revisar `usage_metrics` vs picos Firestore en consola.  
3. No añadir producto; solo métricas internas existentes.

---

## 8. Matriz prioritaria (ejecución)

| ID | Sev | Área | Acción corta |
|----|-----|------|--------------|
| PF-M01 | CRÍTICO | Memory | Cap + stream upload |
| PF-Q05/06 | ALTO | Firestore | Un solo modo carga |
| PF-Q01/03/04 | ALTO | Firestore | `.limit(N)` |
| PF-L01/02 | ALTO | Listeners | Limit + cancel lifecycle |
| PF-M02 | ALTO | Memory | `viewModel()` nav-scoped |
| PF-M03 | ALTO | Memory | Reducir singletons |
| PF-Q07 | MEDIO | Firestore | Records: clients one-shot |
| PF-N02 | MEDIO | Nav | Saciar legacy del grafo piloto |
| PF-Q12 | CRÍTICO* | Firestore | No invocar Dashboard full-scan |
| Dup VM/Repo | ALTO | Estructura | Freeze + un canon por dominio hub |

---

## 9. Fuera de alcance (explícito)

- Nuevas pantallas, CRM/Tasks/AI UI, company switcher, billing.  
- Migración masiva de datos legacy → platform.  
- Reescritura de arquitectura multi-módulo.  
- Índices nuevos salvo los requeridos por `orderBy+limit` ya modelados.

---

## 10. Criterios de aceptación del plan

1. Hub Clients/Records/Documents: **ninguna** query de lista sin `limit`.  
2. Ningún `bindCompany` hace get+listen simultáneo.  
3. Upload rechaza o streaMEA archivos sobre tope; sin `readBytes()` ilimitado.  
4. VMs platform viven en `ViewModelStore` de la ruta.  
5. Dashboard/CRM full-scan **cero** call sites desde UI.  
6. Build piloto/V1: grafo sin Patients/Clinical/Inventory/Sales/Chat legacy (o flag off).

---

*Auditoría de rendimiento. Plan de hardening únicamente — sin nuevas funcionalidades.*
