# NexoGo V1 Readiness Report

**Rol:** CTO Audit  
**Fecha:** 2026-09-19  
**Alcance:** Arquitectura · Seguridad · Escalabilidad · Deuda técnica · Duplicados · Rendimiento · UX  
**Código:** No modificado (solo evaluación)  
**Referencias cruzadas:** `PROJECT_HEALTH_REPORT.md`, `UNIFIED_NAVIGATION_PLAN.md`, `MASTER_ARCHITECTURE.md`, reportes `*_REPORT.md` de foundations

---

## Veredicto ejecutivo

| Pregunta | Respuesta |
|----------|-----------|
| ¿Listo para V1 SaaS multi-tenant en producción? | **No** |
| ¿Listo como MVP clínica vet (legacy) en demo controlada? | **Parcial** — usable con riesgo de seguridad alto |
| ¿Foundation de plataforma compilable y coherente? | **Sí** — scaffold fuerte, producto no cutover |

### Clasificación global

| Dimensión | Estado |
|-----------|--------|
| Arquitectura (modelo datos platform) | **listo** (foundation) |
| Arquitectura (producto vivo / cutover) | **crítico** |
| Seguridad | **crítico** |
| Escalabilidad | **requiere corrección** |
| Deuda técnica | **requiere corrección** |
| Duplicados | **crítico** |
| Rendimiento | **requiere corrección** |
| UX / navegación unificada | **crítico** |

**Decisión de ship:** no lanzar V1 Platform. Congelar crecimiento de foundations sin cutover; priorizar seguridad server-side + un hub + un módulo platform en UI.

---

## Escala de clasificación

| Etiqueta | Significado |
|----------|-------------|
| **listo** | Cumple para V1 o es foundation sólida usable sin bloqueo |
| **requiere corrección** | Debe corregirse antes o en paralelo al soft-launch; no es bloqueo absoluto si el alcance se reduce |
| **crítico** | Bloquea V1 producción / multi-tenant / datos de clientes reales |

---

## 1. Inventario Platform (estado por módulo)

| Módulo | Entregable | Integración producto | Clasificación V1 |
|--------|------------|----------------------|------------------|
| **company** | Repo, sesión, `CompanyProvider` | Wired en `MainActivity` / auth / Home (nombre) | **listo** (sesión) · UI admin **requiere corrección** |
| **tenant** | `TenantContext`, paths, `TenantIsolationGuard` | Solo client-side en repos platform | **listo** (modelo) · server **crítico** |
| **role** + **Permission Engine** | Catalog, bundles, checker, engine 6×6 | **0 call sites** en UI/nav/repos legacy | **requiere corrección** |
| **clients** | Repo + ViewModel | Sin rutas NavGraph | **requiere corrección** |
| **records** | Repo + ViewModel | Sin UI; history/clinical legacy | **requiere corrección** |
| **documents** | Repo + storage paths | Sin UI | **requiere corrección** |
| **chat** (platform) | Repo + push NoOp | UI usa `modules/chat` / root paths | **crítico** (split datos) |
| **dashboard** (platform) | Repo métricas | UI Dashboard = legacy | **requiere corrección** |
| **tasks** | Repo | Sin UI / sin enforce permisos | **requiere corrección** |
| **crm** | Repo | Sin UI | **requiere corrección** |
| **ai** | Repo + `NoOpOpenAiClient` | Sin UI; sin API key en client | **listo** (seguro stub) |
| **audit** | Logger + repo + viewer | Solo `login`/`logout` | **requiere corrección** |
| **notifications** | Repo + NoOp push/email | Sin wire a dominio | **requiere corrección** |
| **billing** | Solo `BILLING_ARCHITECTURE.md` | Sin código | **requiere corrección** (V1 comercial) |

Única UI bajo `platform/`: `platform/company/ui/CompanyProvider.kt`.

---

## 2. Arquitectura

### Hallazgos

| ID | Hallazgo | Evidencia | Clasificación |
|----|----------|-----------|---------------|
| A1 | Tres stacks concurrentes: `platform/*` (SaaS), `modules/*` (features), `ui/`+`viewmodel/`+`repository/`+`core/` (legacy vivo) | Tree app ~257 `.kt`; solo Company cruza stacks | **crítico** |
| A2 | Sesión multi-empresa cableada sin bloquear auth legacy | `CompanyProvider`, `CompanySessionManager.ensureSessionForUser` (fail-open) | **listo** / fail-open **requiere corrección** |
| A3 | Dual entry post-login: Home vs Dashboard según flujo de login | `NavGraph.kt`, `UNIFIED_NAVIGATION_PLAN.md` | **crítico** |
| A4 | Home muestra company name pero módulos filtran por `UserRole` vet, no Permission Engine | `HomeScreen.kt` | **requiere corrección** |
| A5 | Módulos platform data-only (Clients, Records, Docs, Tasks, CRM, AI, Chat platform) fuera de navegación | Sin rutas en NavGraph hacia `platform.*` | **crítico** |
| A6 | Diseño de paths tenant coherente | `companies/{companyId}/{collection}` vía `TenantCollections` / `TenantFirestore` | **listo** |
| A7 | Package aún `com.example.nexogo` | Manifest / namespace | **requiere corrección** |

### Lectura CTO

La arquitectura **de datos** de Platform es correcta y compilable. La arquitectura **de producto** sigue siendo la app veterinaria legacy. V1 Platform no existe como experiencia; existe como biblioteca interna.

---

## 3. Seguridad

| ID | Hallazgo | Evidencia | Clasificación |
|----|----------|-----------|---------------|
| S1 | Catch-all Firestore: cualquier usuario autenticado lee/escribe **todo** el proyecto (incluye futuro `companies/*`) | `firestore.rules` L156–159 `match /{document=**} { allow read, write: if request.auth != null; }` | **crítico** |
| S2 | Rules de emergencia `if true` en repo | `firestore_rules_emergency.rules` | **crítico** |
| S3 | **Cero** reglas para `companies/{companyId}/…` | Grep `companies` en `firestore.rules` → vacío | **crítico** |
| S4 | Isolation solo en Kotlin; `TenantIsolationGuard` no cubre todos los repos IAM/company | Guard en repos tenant-aware; `RoleRepository` / paths globales sin membership server | **crítico** |
| S5 | Credenciales admin hardcodeadas en cliente | `admin@nexogo.com` / `123456` en `FirebaseAuthRepository`, `SimpleLoginScreen`, `FirebaseDiagnostics`, `FirebaseAutoFixer` | **crítico** |
| S6 | Cold start ejecuta diagnostics + mock data + testers | `MainActivity` → `MockDataGenerator`, `FirebaseDiagnostics`, `FirestorePermissionTester` | **crítico** |
| S7 | Storage sin paths `companies/{id}/…`; deny-all final (seguro pero bloquea platform uploads) | `storage.rules` L114–116 deny; sin match tenant | **requiere corrección** |
| S8 | Permission Engine / Checker no enforced en producto | Sin usos fuera de `platform/role` | **requiere corrección** |
| S9 | Audit parcial (auth only); IP vacía | `PersistentAuthViewModel` + `EmptyAuditIpProvider` | **requiere corrección** |
| S10 | OpenAI NoOp; sin API key en app | `OpenAiClient.kt` / `AIRepository` default NoOp | **listo** |
| S11 | Push/email foundation NoOp; FCM legacy incompleto | `NoOpPushDispatcher`, `NexoGoMessagingService` | **requiere corrección** |

### Lectura CTO

**Bloqueo absoluto de producción.** Con el catch-all actual, multi-tenant es cosmética: cualquier cuenta autenticada puede cruzar tenants a nivel Firestore. Corregir rules es prerequisito #1, antes de cablear más UI platform.

---

## 4. Escalabilidad

| ID | Hallazgo | Evidencia | Clasificación |
|----|----------|-----------|---------------|
| E1 | Modelo jerárquico por company escalable en diseño | `TenantCollections`, storage paths platform | **listo** |
| E2 | No hay `firestore.indexes.json` en repo | Glob indexes → 0 | **crítico** (queries compuestas) |
| E3 | Agregaciones / listados con full scan client-side | `DashboardRepository` count*; search en docs/clients con filter in-memory | **requiere corrección** |
| E4 | Canales push/email NoOp → no hay fan-out multi-tenant | `NotificationDispatchers` | **requiere corrección** |
| E5 | Billing / entitlements solo diseño | `BILLING_ARCHITECTURE.md` | **requiere corrección** (SaaS) |
| E6 | Cloud Functions mínimas / no alineadas a platform events | `cloud-functions/` legacy-oriented | **requiere corrección** |

---

## 5. Deuda técnica

| ID | Hallazgo | Evidencia | Clasificación |
|----|----------|-----------|---------------|
| D1 | Foundations incompletas sin cutover (IAM, Audit, Notifications, AI, Nav) | Reportes + 0/1 wire | **requiere corrección** |
| D2 | Doc sprawl: ~18 `*_REPORT.md` + decenas de `.md` arquitectura en root | Repo root | **requiere corrección** |
| D3 | Auth triplicado | `PersistentAuthViewModel`, `AuthViewModel`, `modules/auth` | **requiere corrección** |
| D4 | DI incompleto (Hilt residual / singletons) | `PROJECT_HEALTH_REPORT.md` | **requiere corrección** |
| D5 | ~344 warnings Kotlin reportados | Health report | **requiere corrección** |
| D6 | Tests mínimos (~2) | Health report | **requiere corrección** |
| D7 | `gradle.properties` / JDK notes frágiles | Config | **requiere corrección** |
| D8 | Compilación verde | `:app:compileDebugKotlin` BUILD SUCCESSFUL | **listo** |

---

## 6. Duplicados

| Dominio | Copias | Clasificación |
|---------|--------|---------------|
| Chat | `platform/chat` + `modules/chat` + `repository/ChatRepository` | **crítico** |
| Storage managers | `firebase/`, `core/firebase/`, `core/repository/firebase/` | **requiere corrección** |
| Roles | `UserRole` legacy vs `BaseRoleCodes` + bridges no usados en UI | **requiere corrección** |
| Messaging | FCM legacy + platform NoOp notifications | **requiere corrección** |
| Firestore access | Múltiples `Firebase*Repository` / managers | **requiere corrección** |
| Login / hubs | Login / Modern / Ultra / Minimal → Home o Dashboard | **crítico** |
| Clinical / history | `ui/clinical` vs `modules/history` vs `platform/records` | **requiere corrección** |
| Patients vs Clients | UI Patients vs `platform/clients` | **requiere corrección** |

**Riesgo principal:** divergencia de datos (dos chats / dos modelos de cliente) y ambigüedad de “fuente de verdad”.

---

## 7. Rendimiento

| ID | Hallazgo | Clasificación |
|----|----------|---------------|
| P1 | Build OK; sin harness de performance runtime | **listo** (compile) / **requiere corrección** (ops) |
| P2 | Queries unbounded / agregaciones full-collection en dashboard y búsquedas | **crítico** a escala tenant media |
| P3 | Snapshot listeners (chat platform + legacy) — riesgo de leaks si UI no dispose | **requiere corrección** |
| P4 | Search documentos/clientes: load-all + filter memoria | **requiere corrección** |

---

## 8. UX

| ID | Hallazgo | Clasificación |
|----|----------|---------------|
| U1 | Plan de navegación unificada existe; no implementado | **crítico** |
| U2 | `CompanyProvider` wired; nombre de empresa visible | **listo** |
| U3 | Sin company switcher; company no gobierna mapa de módulos | **requiere corrección** |
| U4 | Platform features invisibles al usuario final | **crítico** |
| U5 | Dual hub Home/Dashboard confunde onboarding | **requiere corrección** |
| U6 | Drawer/acciones por rol vet, no por Permission Engine | **requiere corrección** |

---

## 9. Scorecard V1 (resumen)

```
Arquitectura datos platform ..... listo
Arquitectura producto ........... crítico
Seguridad server-side ........... crítico
IAM en path de producto ......... requiere corrección
Escalabilidad / indexes ......... crítico → requiere corrección
Deuda / duplicados .............. crítico
Rendimiento queries ............. requiere corrección
UX unificada .................... crítico
AI key hygiene .................. listo
Compilación ..................... listo
```

**Conteo aproximado de hallazgos clasificados en este informe**

| Clasificación | Cantidad (ítems tabulados) |
|---------------|----------------------------|
| listo | ~10 |
| requiere corrección | ~35 |
| crítico | ~18 |

---

## 10. Criterio mínimo para declarar “V1 Platform Ready”

No negociable (todos **crítico** hoy):

1. **Firestore + Storage rules** deny-by-default; membership en `companies/{companyId}`; eliminar catch-all y emergency `if true`.
2. **Quitar** credenciales hardcodeadas, mock generators y diagnostics del path de release / `MainActivity`.
3. **Un solo hub** post-login + gating con `PermissionChecker` / `PermissionEngine`.
4. **Cutover visible:** al menos un módulo platform en UI (recomendado: Clients o Records) leyendo solo paths tenant.
5. **`firestore.indexes.json`** + límites en listados/agregaciones.
6. **Congelar** un stack por dominio (elegir chat platform vs modules; archivar el otro).

Recomendado antes de clientes de pago:

7. Audit en mutaciones de dominio + IP real (backend).  
8. Notifications push vía Cloud Function (reemplazar NoOp).  
9. Entitlements billing mínimos (plan FREE/STARTER flags).  
10. Suite de tests de isolation tenant (unit + rules emulator).

---

## 11. Roadmap CTO sugerido (orden)

| Fase | Foco | Objetivo |
|------|------|----------|
| **P0 — Secure** | Rules + secretos + MainActivity limpio | Dejar de sangrar datos |
| **P1 — One spine** | Unified nav + Permission Engine en drawer | Una app, un modelo de acceso |
| **P2 — First cutover** | Clients **o** Records en UI tenant | Platform deja de ser biblioteca muerta |
| **P3 — Kill duplicates** | Un chat, un storage manager, un auth VM | Una fuente de verdad |
| **P4 — Scale basics** | Indexes, pagination, dashboard aggregations | Tenant con datos reales |
| **P5 — Ops** | Audit full, push, billing entitlements | Operación SaaS |

---

## 12. Lo más listo vs lo más peligroso hoy

| Más listo | Más peligroso |
|-----------|---------------|
| Company session + `TenantContext` paths | Catch-all `firestore.rules` |
| Permission Engine / catalog (código) | Isolation solo en cliente |
| AI NoOp sin key en device | Admin `123456` + mock en cold start |
| Compilación verde de foundations | Triple chat / dual Home–Dashboard |

---

## 13. Conclusión

NexoGo Platform tiene una **fundación de dominio multi-tenant bien orientada** (company, tenant, IAM, módulos de negocio, audit/notifications stubs) y **compila**.  

Como **producto V1 multi-tenant**, está **no listo**: la superficie viva es legacy, la seguridad server-side es inaceptable, los permisos no gobiernan la UX, y los duplicados amenazan la integridad de datos.

**Clasificación final V1 Platform:** **crítico** — no ship a producción.  
**Clasificación demo interna legacy (datos no sensibles):** **requiere corrección** — solo con rules endurecidas y sin credentials/mock en build de demo.

---

*Informe generado sin modificaciones de código. Próximo paso recomendado: P0 Secure (rules + limpieza MainActivity/credenciales).*
