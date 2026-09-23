# LEGACY CLEANUP PLAN — NexoGo

**Rol:** Software Refactoring Specialist  
**Fecha:** 2026-09-23  
**Contexto:** Tras `V1_INTEGRATION_REPORT.md`, el NavGraph solo expone Platform. El árbol fuente aún contiene stacks veterinarios y auth duplicados.  
**Regla:** No tocar Company / Roles / Tenant / Clients / Records / Documents / Users activos. Compilar siempre.

---

## 1. Inventario

### 1.1 Código muerto (no alcanzable desde NavGraph V1)

| Área | Evidencia |
|------|-----------|
| Rutas legacy | Ausentes de `Screen.kt` / `NavGraph.kt` |
| Auth experimental | 6+ pantallas login/splash sin call sites |
| Settings legacy | `SettingsScreen`, `SimpleSettingsScreen`, Storage, ProductCategories |
| UI vet | `ui/screens/{appointments,patients,clinical,chat,inventory,sales,dashboard,medical,admin}` |
| `modules/*` vet | appointments, auth, chat, config, dashboard, history, inventory, patients, profile, sales |

### 1.2 Pantallas duplicadas

| Dominio | Variantes |
|---------|-----------|
| Login | `SimpleLoginScreen` (activo) vs Minimal/Modern/UltraSimple/`LoginScreen` / `modules.auth.LoginScreen` |
| Splash | `SessionSplashScreen` (activo) vs Minimal/UltraSimple/`SplashScreen` |
| Register | `ui/.../RegisterScreen` (activo) vs `modules.auth.RegisterScreen` |
| Settings | `PlatformSettingsScreen` (activo) vs Settings/SimpleSettings |
| Profile | `ui/.../ProfileScreen` vs `modules.profile.ProfileScreen` |
| Chat / Citas / Inventario / Ventas | UI + modules paralelos |

### 1.3 Logins duplicados

| Mantener | Eliminar (seguro) |
|----------|-------------------|
| `SimpleLoginScreen` + `SessionSplashScreen` + `RegisterScreen` (ui) | Minimal/Modern/UltraSimple login+splash, `SplashScreen`, `LoginScreen` (ui), `modules/auth/*` |

### 1.4 Repositorios duplicados

| Mantener (Platform / auth vivo) | Legacy / duplicado |
|---------------------------------|--------------------|
| `FirebaseAuthRepository`, platform `*Repository`, `Usage`/`Audit`/`Company`/`Role`/`Client`/`Record`/`Document`/`UserManagement` | `modules/*/data/*`, `repository/ChatRepository`, doble `FirebaseRepository` (core vs repository) — **defer** consolidar FirebaseRepository (alto acoplamiento) |

### 1.5 ViewModels duplicados

| Activos | Legacy / duplicados |
|---------|---------------------|
| `PersistentAuthViewModel`, platform VMs, `ProfileViewModel` (viewmodel) | `AuthViewModel`×2, `ChatViewModel`×2, `Appointment*`×N, `Patient*`×2, `Sales*`×2, `Inventory*`×2, `Firebase*`, `SafeFirebase*`, `ClinicalRecord*`, `SettingsViewModel`, `ProductCategoryViewModel`, modules dashboard/history/config |

### 1.6 Módulos veterinarios obsoletos (respecto a Platform C/E/D)

Pacientes, citas, historial clínico legacy, inventario, ventas/servicios, chat vet, dashboard vet, approval admin legacy.

---

## 2. Fases

| Fase | Contenido | Riesgo |
|------|-----------|--------|
| **S0 Seguro** | Auth/settings huérfanos + `modules/auth` + clusters UI/modules sin import desde superficie viva; VMs/repos solo usados por ellos; fix `FirebaseTestData` | Bajo |
| **S1 Diferido** | Unificar `FirebaseRepository` (core vs repository); borrar `repository/ChatRepository` tras S0; ProGuard/R8 | Medio |
| **S2 Diferido** | Archive carpeta `archived/legacy/` en git history; shrink APK | Bajo ops |

---

## 3. Criterio “seguro”

Eliminar solo si:

1. Cero imports desde NavGraph / platform / SessionSplash / SimpleLogin / Register / Home / Platform* / Profile activo.  
2. Tras borrar, el grafo de dependencias del set eliminado es cerrado (o se elimina junto).  
3. `:app:compileDebugKotlin` pasa.

---

## 4. Ejecución S0 (este ciclo)

**Estado:** completado — ver `LEGACY_CLEANUP_REPORT.md`.  
**Compile:** BUILD SUCCESSFUL.

