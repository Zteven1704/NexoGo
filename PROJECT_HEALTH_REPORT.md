# NexoGo Project Health Report

**Fecha:** 2026-09-19  
**Alcance:** Auditoría completa — arquitectura, dependencias, duplicados, warnings, compilación  
**Compilación:** `:app:compileDebugKotlin --rerun-tasks` — **BUILD SUCCESSFUL** (~40s)  
**Warnings Kotlin:** **344**

---

## 1. Veredicto ejecutivo

| Área | Salud | Nota |
|------|-------|------|
| Compilación | **Verde** | Build exitoso |
| Platform foundation | **Fuerte (scaffold)** | 11 módulos / ~43 archivos |
| Cutover UI → platform | **Débil** | Solo Company cableado |
| Duplicados / shadowing | **Pobre** | 3 stacks concurrentes |
| Dependencias / DI | **Parcial** | Hilt comentado; `@Inject` residual |
| Warnings | **Alto ruido** | 344 (mayoría deprecations) |
| Tests | **Mínimo** | 2 archivos de prueba |

**Estado general:** el producto vive en `ui/` + `modules/` + `viewmodel/` legacy; `platform/*` es una capa SaaS multi-tenant completa a nivel datos pero casi huérfana de navegación. La app **compila**, pero la deuda estructural es alta.

---

## 2. Arquitectura

### 2.1 Tres stacks concurrentes

```
platform/*   → SaaS multi-tenant (Company, IAM, Clients, Records, Docs, Chat, CRM, AI, …)
modules/*    → Feature modules Compose (history, sales, inventory, chat, auth, …)
ui/ + viewmodel/ + repository/ + core/  → Superficie legacy viva (NavGraph)
```

| Capa | ~Archivos `.kt` | Rol |
|------|----------------:|-----|
| `modules/` | 75 | Features Compose |
| `ui/` | 63 | Screens / components |
| `platform/` | 43 | Nueva plataforma |
| `core/` | 19 | Firebase + models |
| `viewmodel/` | 17 | VMs legacy / Firebase* |
| `model/` | 12 | Dominio vet legacy |
| `repository/` | 11 | Repos legacy |
| `navigation/` | 2 | NavGraph |
| **Total app** | **~257** | |

### 2.2 Platform inventory (completo)

| Módulo | Archivos | Integración UI |
|--------|----------:|----------------|
| company | 6 | ✅ `CompanyProvider` / Home / Auth session |
| tenant | 8 | Infra (usado por repos platform) |
| role | 6 | No |
| clients | 3 | No (Patients UI sigue viva) |
| records | 3 | No (History/Clinical sigue vivo) |
| documents | 3 | No |
| chat | 5 | No (triple chat legacy) |
| dashboard | 2 | No |
| tasks | 2 | No |
| crm | 2 | No |
| ai | 3 | No (NoOp OpenAI) |

### 2.3 Dual paths (live vs platform)

| Dominio | Live (UI) | Platform | Riesgo |
|---------|-----------|----------|--------|
| Pacientes / Clientes | Patients screens + DataStore | `Client*` | Divergencia de datos |
| Historial / Expedientes | history + clinical | `Record*` | Mismo |
| Chat | ui + modules | `platform.chat` | Triple stack |
| Auth | PersistentAuth + muchas pantallas login | Session Company only | Proliferación |
| Repos Firebase | core + repository + modules | TenantAware* | Sprawl |

### 2.4 Riesgos arquitectónicos (prioridad)

1. **Migración incompleta / tres stacks** — comportamiento ambiguo; platform puede crecer sin impacto en producto.
2. **Triple chat** — 3× `ChatRepository`, 2× `ChatListScreen`/`ChatViewModel`; NavGraph importa ambos.
3. **Patients ≠ Clients cutover** — UX aún Patient; platform Clients/Records sin rutas.
4. **Dual `UserRole`** — `model.UserRole` vs `core.models.UserRole` (valores incompatibles); bridge solo cubre core.
5. **DI a medias** — Hilt deshabilitado; `@Inject` residual (~11); singletons manuales.
6. **Firebase sprawl** — 2× `FirebaseRepository`, 3× storage managers, muchos `Firebase*ViewModel`.
7. **Tenant isolation no aplicada al path live** — guardas existen; repos legacy sin filtrar por Company.
8. **Auth/login proliferación** — múltiples LoginScreen + dual Auth VM/repo.

---

## 3. Dependencias

### 3.1 Stack declarado (`libs.versions.toml` + `app/build.gradle.kts`)

| Área | Versión / notas |
|------|-----------------|
| AGP | 8.13.0 |
| Kotlin | 2.0.21 |
| Compose BOM | 2024.09.00 |
| Firebase BOM | 33.4.0 (auth, firestore, storage, messaging, analytics) |
| Navigation Compose | 2.8.2 |
| Lifecycle / VM Compose | 2.8.7 / 2.9.4 |
| Coroutines | 1.8.1 |
| Coil | 2.7.0 |
| DataStore | 1.0.0 |
| Gson | 2.10.1 |
| iText PDF | 7.2.5 |
| CameraX | 1.4.0 |
| WorkManager | 2.9.1 |
| Accompanist permissions | 0.32.0 |
| Play Services Auth | 21.2.0 |
| Material (View) | 1.9.0 |

### 3.2 DI / Hilt

```kotlin
// app/build.gradle.kts — COMENTADO
// implementation(libs.hilt.android)
// implementation(libs.hilt.navigation.compose)
// ksp(libs.hilt.compiler)
```

- Hilt listado en version catalog pero **no activo**.
- Anotaciones `@Inject` aún presentes en repos/app → ornamentales.
- **Recomendación:** restaurar Hilt de punta a punta **o** eliminar `@Inject` / Application inject muerto.

### 3.3 OpenAI / AI

- Sin dependencia SDK OpenAI en Gradle (correcto para foundation).
- `NoOpOpenAiClient` en app; API key prevista solo en backend.

### 3.4 Documentación vs código

- ~**66** Markdown en raíz (arquitectura / reportes de módulos).
- Alineación diseño↔scaffold **buena** para platform; ejecución en producto **atrasada**.

---

## 4. Duplicados

### 4.1 Clases / enums con el mismo nombre (>1 definición)

| Nombre | × | Severidad |
|--------|--:|-----------|
| `Product` | 4 | Alta |
| `ChatRepository` | 3 | **Crítica** |
| `Sale` / `SaleItem` / `Message` / `ClinicalRecord` | 3 | Alta |
| `Patient` / `PatientViewModel` / `Conversation` / `FirebaseRepository` | 2 | Alta |
| `UserRole` (enums distintos) | 2 | **Crítica** |
| `AuthViewModel` / `ChatViewModel` / `FollowUp` / `MessageType`… | 2 | Media–Alta |

*(Conteo por nombre simple de clase; incluye platform vs legacy intencional en algunos casos — p.ej. `FollowUp` CRM vs posible legacy.)*

### 4.2 Duplicados estructurales destacados

| Área | Paths |
|------|-------|
| Chat UI | `ui/screens/chat/*` ↔ `modules/chat/*` |
| Auth | `ui/screens/auth/*` ↔ `modules/auth/*` + muchas variantes Login |
| Patients | `ui/screens/patients` ↔ `modules/patients` ↔ `platform/clients` |
| History | `modules/history` ↔ `ui/screens/clinical` ↔ `platform/records` |
| Firebase storage | `core/firebase`, `core/repository/firebase`, `firebase/` |
| Sales / Inventory / Dashboard | `ui/screens/*` ↔ `modules/*` |

### 4.3 NavGraph smell

Imports simultáneos de chat UI y `modules.chat` (alias `NewChatScreen`) → riesgo de shadowing y rutas ambiguas.

---

## 5. Warnings (compilación)

| Métrica | Valor |
|---------|------:|
| Total `w:` | **344** |
| Deprecations | ~234 |
| `Condition is always …` | ~51 |
| Delicate API | ~12 |
| Duplicate branch | ~5 |
| Errores `e:` | **0** |

### Top archivos por warnings

| Archivo | ~Warnings |
|---------|----------:|
| `core/firebase/DateFilterDiagnostics.kt` | 38 |
| `repository/FirebaseStorageRepository.kt` | 24 |
| `ui/.../MinimalAppointmentsScreen.kt` | 19 |
| `modules/history/ClinicalHistoryViewModel.kt` | 17 |
| Edit/Create Appointment screens | 12–14 c/u |
| `modules/chat/ChatRepository.kt` | 10 |
| Settings / Home Icons AutoMirrored | varios |

**Patrones dominantes:** APIs de fecha `java.util.Date` deprecadas; Icons no AutoMirrored; `menuAnchor()` viejo; condiciones siempre true/false; APIs delicadas de coroutines.

**Nota:** el código **platform/** nuevo aporta poco al ruido de warnings; el peso está en legacy appointments/history/firebase diagnostics.

---

## 6. Compilación

```bash
bash ./gradlew :app:compileDebugKotlin --no-daemon --rerun-tasks
# BUILD SUCCESSFUL in ~40s
# 17 actionable tasks
```

| Check | Resultado |
|-------|-----------|
| compileDebugKotlin | ✅ SUCCESS |
| Errores de tipo | ✅ Ninguno |
| Platform modules | ✅ Compilan |
| Tests automatizados sustanciales | ❌ Solo Example* (~2 files) |

---

## 7. Scorecard consolidado

| Dimensión | Score (1–5) | Comentario |
|-----------|:-----------:|------------|
| Compila | 5 | Verde limpio de errores |
| Diseño platform | 4 | Scaffold completo y coherente |
| Aplicación en producto | 2 | Solo Company en flujo vivo |
| Control de duplicados | 1 | Chat/Auth/Patient/Firebase |
| Higiene warnings | 2 | 344 warnings |
| DI / dependencias | 2 | Hilt mid-adoption |
| Cobertura tests | 1 | Casi nula |
| Docs arquitectura | 4 | Amplia; useful as To-Be |

**Promedio aproximado: 2.6 / 5 — “Fundación sólida, producto aún bifurcado”.**

---

## 8. Recomendaciones priorizadas

### P0 (estabilidad)

1. Elegir **un** stack de Chat y fijarlo en NavGraph; marcar el resto `@Deprecated` / no importar.
2. Unificar **`UserRole`** (un enum + bridge de migración).
3. Cerrar Hilt: **on completo** o **off + purge `@Inject`**.

### P1 (migración)

4. Cablear **Clients** y **Records** a rutas Compose (aunque sea feature-flag).
5. Congelar evolución de Patients/History salvo bugs; apuntar writers nuevos a `platform`.
6. Aplicar `companyId` en path live (citas/pacientes) o documentar ventana de riesgo multi-tenant.

### P2 (higiene)

7. Reducir warnings top-10 archivos (Date APIs, Icons AutoMirrored, diagnostics fuera de release).
8. Eliminar pantallas Login duplicadas no referenciadas.
9. Añadir tests de smoke: Company session bind, Client create, Record create, Task assign.

### P3 (producto)

10. UI Documentos / Tasks / CRM / Dashboard platform.
11. Worker backend OpenAI para `AIJob`.
12. Security Rules tenant-scoped.

---

## 9. Resumen

- **Compilación OK**; platform foundations **completas a nivel código**.
- **Salud estructural baja** por duplicación y cutover incompleto.
- Siguiente paso de mayor ROI: **una fuente de verdad por dominio (Chat + Roles + Clients)** antes de más módulos platform.

---

*Generado por auditoría automatizada + análisis de árbol de paquetes, Gradle y salida de `compileDebugKotlin`.*
