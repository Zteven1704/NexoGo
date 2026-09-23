# LEGACY CLEANUP REPORT — NexoGo

**Rol:** Software Refactoring Specialist  
**Fecha:** 2026-09-23  
**Plan:** `LEGACY_CLEANUP_PLAN.md`  
**Compilación:** `:app:compileDebugKotlin` — **BUILD SUCCESSFUL**  
**Fase ejecutada:** **S0 Seguro** únicamente

---

## 1. Resumen

Se eliminó el stack veterinario y auth/settings duplicados que ya no estaban en el NavGraph V1. La superficie viva (Platform + auth canónico + Profile/Settings) compila y se mantiene.

| Métrica | Antes (aprox.) | Después S0 |
|---------|----------------|------------|
| `modules/` | 10 submódulos vet | **Eliminado** |
| Logins/splash experimentales | 6+ | **0** (quedan SessionSplash + SimpleLogin + Register) |
| UI screens vet | appointments/patients/clinical/chat/inventory/sales/dashboard/medical/admin | **Eliminados** |
| ViewModels legacy | Appointment/Chat/Patient/Clinical/Auth/Firebase*/Safe*/Settings/ProductCategory | **Eliminados** |
| `repository/ChatRepository` | Duplicado | **Eliminado** |

---

## 2. Ejecutado (S0)

### 2.1 Logins / splash duplicados

Eliminados:

- `MinimalLoginScreen`, `ModernLoginScreen`, `UltraSimpleLoginScreen`
- `MinimalSplashScreen`, `UltraSimpleSplashScreen`, `SplashScreen`
- `ui/screens/auth/LoginScreen`
- Carpeta `modules/auth/` (Login/Register/AuthViewModel/AuthRepository)

**Conservados:** `SessionSplashScreen`, `SimpleLoginScreen`, `RegisterScreen` (ui).

### 2.2 Settings / componentes huérfanos

Eliminados:

- `SettingsScreen`, `SimpleSettingsScreen`, `StorageManagementScreen`, `ProductCategoriesScreen`
- `SafeSettingsScreen`, `AppointmentCard`, `BeautifulAppointmentCard`, `AppointmentList`, `ChatFilePickerDialog`
- `SettingsViewModel`, `ProductCategoryViewModel`

**Conservado:** `PlatformSettingsScreen`, `HelpScreen`, `AboutScreen`.

**Ajuste:** `AppSettings` + `LocalProductCategory` en `model/AppSettings.kt` para que `AppDataStore` no dependa de ViewModels borrados.

### 2.3 Módulos veterinarios obsoletos

Eliminado `modules/` completo:

`appointments`, `auth`, `chat`, `config`, `dashboard`, `history`, `inventory`, `patients`, `profile`, `sales`.

### 2.4 Pantallas UI vet (fuera de nav)

Eliminados directorios:

`ui/screens/{appointments,patients,clinical,chat,inventory,sales,dashboard,medical,admin}`.

### 2.5 ViewModels / repos

Eliminados ViewModels: `Appointment*`, `Chat*`, `Patient*`, `ClinicalRecord*`, `AuthViewModel`, `FirebaseAppointment/Inventory/Sales`, `SafeFirebaseAppointment`.

Eliminado: `repository/ChatRepository.kt`.

### 2.6 Test data

`FirebaseTestData`: removidos imports y `createTestServices()` (dependían de `modules.sales`).

---

## 3. No ejecutado (diferido — S1/S2)

| Ítem | Motivo |
|------|--------|
| Unificar `core.FirebaseRepository` vs `repository.FirebaseRepository` | Alto acoplamiento (decenas de refs); riesgo de ruptura |
| Borrar `model` vet (Appointment, Product, Sale, …) | Aún usados por `AppDataStore` / `FirebaseTestData` / diagnostics |
| Borrar `FirebaseFullTestActivity` / ConnectionTest | Útil debug; ya `exported=false` |
| Archive git `archived/legacy/` | Ops; no necesario para compile |
| R8 shrink agresivo | Release plan, no cleanup de fuente |

---

## 4. Superficie viva post-cleanup

```
MainActivity → SessionSplash → Login | Home
Home → clients | records | documents | users | roles | permissions
     → profile | settings → help | about
platform/{company,tenant,role,clients,records,documents,users,audit,usage}
viewmodel/{PersistentAuthViewModel, ProfileViewModel}
```

---

## 5. Verificación

| Check | Resultado |
|-------|-----------|
| Compile | **BUILD SUCCESSFUL** |
| NavGraph V1 | Intact |
| Company / Roles / Tenant | Intact |
| Clients / Records / Documents | Intact |

---

## 6. Conclusión

**S0 completado:** dead code y duplicados seguros fuera; app más coherente con Platform. Consolidación de `FirebaseRepository` y modelos vet residuales quedan en el plan diferido.
