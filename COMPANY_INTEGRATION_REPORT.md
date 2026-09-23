# Company Integration Report

**Fecha:** 2026-09-18  
**Alcance:** Integración mínima de Company en Auth → Session → UI (Home)  
**Compilación:** `:app:compileDebugKotlin` — **BUILD SUCCESSFUL**

---

## Objetivo

Completar la integración mínima multi-tenant de Company sin romper el flujo legacy de autenticación ni modificar módulos legacy (pacientes, historial, etc.).

---

## Componentes implementados

| # | Componente | Ubicación | Estado |
|---|------------|-----------|--------|
| 1 | **CompanyModel** | `platform/company/model/CompanyModels.kt` (`typealias CompanyModel = Company`) | ✅ |
| 2 | **CompanyRepository** | `platform/company/data/CompanyRepository.kt` | ✅ |
| 3 | **CompanyPaths** | `platform/company/data/CompanyPaths.kt` | ✅ |
| 4 | **CompanySessionManager** | `platform/company/session/CompanySessionManager.kt` | ✅ |
| 5 | **CompanyProvider** | `platform/company/ui/CompanyProvider.kt` | ✅ |

### Detalle

- **CompanyModel / Company:** documento raíz del tenant (`companies/{id}`), settings, membership, index, roles/planes/packs.
- **CompanyPaths:** rutas Firestore (`COMPANIES`, `SETTINGS`, memberships, espejo `users/{uid}/companies`).
- **CompanyRepository:** create/get/update company, settings, membership, list index / user refs; escritura en espejo de membresía.
- **CompanySessionManager:** `ensureSessionForUser` (restore last → membership → create default), prefs locales, `clearSession`; fallos **no bloquean** auth.
- **CompanyProvider:** CompositionLocals `LocalActiveCompany`, `LocalCompanySession`, `LocalCompanySessionManager`; envuelve la app en `MainActivity`.

---

## Integración

### Auth / PersistentAuthViewModel

- Instancia de `CompanySessionManager` en el ViewModel.
- `bindCompanySession(user)` (fail-open) tras:
  - carga Firebase / DataStore al iniciar
  - recuperación post-error
  - `createDefaultAdmin()`
  - `setCurrentUser()`
  - `refreshUserFromDataStore()`
- `logout()` llama `companySessionManager.clearSession()`.

### MainActivity

```kotlin
CompanyProvider {
    NexoGoApp()
}
```

### HomeScreen

- Lee `LocalActiveCompany.current`.
- Muestra `companyName` en TopAppBar (subtítulo) y en el drawer header.
- Si no hay company (sesión vacía / Firestore offline), la UI legacy sigue igual.

---

## Flujo runtime

```
Login / restore user
  → PersistentAuthViewModel.bindCompanySession
  → CompanySessionManager.ensureSessionForUser
      → listUserCompanyRefs / last prefs / createCompany
  → StateFlow session
  → CompanyProvider CompositionLocals
  → HomeScreen muestra nombre de empresa
```

---

## Reglas respetadas

| Regla | Cumplimiento |
|-------|--------------|
| No romper compilación | ✅ BUILD SUCCESSFUL |
| No modificar módulos legacy | ✅ Solo `platform/company` + puntos de integración Auth/Home/MainActivity |
| No eliminar código existente | ✅ Solo adiciones / llamadas nuevas |
| Compatibilidad con flujo actual | ✅ Bind fail-open; UI sin company = comportamiento previo |

---

## Archivos tocados (integración)

**Nuevos (platform):**

- `app/src/main/java/com/example/nexogo/platform/company/model/CompanyModels.kt`
- `app/src/main/java/com/example/nexogo/platform/company/data/CompanyPaths.kt`
- `app/src/main/java/com/example/nexogo/platform/company/data/CompanyRepository.kt`
- `app/src/main/java/com/example/nexogo/platform/company/session/CompanySessionState.kt`
- `app/src/main/java/com/example/nexogo/platform/company/session/CompanySessionManager.kt`
- `app/src/main/java/com/example/nexogo/platform/company/ui/CompanyProvider.kt`

**Modificados (cableado):**

- `MainActivity.kt` — `CompanyProvider`
- `PersistentAuthViewModel.kt` — bind / clear session
- `HomeScreen.kt` — display `LocalActiveCompany`

---

## Compilación

```bash
bash ./gradlew :app:compileDebugKotlin --no-daemon
# BUILD SUCCESSFUL
```

Warnings existentes (Icons deprecated en HomeScreen) — no introducidos por Company.

---

## Fuera de alcance (siguiente)

- Switcher multi-company en UI
- Claims / IAM por company en rutas legacy
- Migración Patients → Clients bajo `companies/{id}`
- Reglas Firestore tenant-scoped
- Onboarding explícito de empresa (hoy: create default automático)

---

## Veredicto

**Integración mínima de Company completa y compilando.** Auth enlaza sesión de tenant de forma no bloqueante; Home refleja la empresa activa cuando existe.
