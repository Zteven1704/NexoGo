# S0 Secure Report

**Sprint:** S0 Secure (`TOP_20_NEXT_ACTIONS.md` #1–#5)  
**Fecha:** 2026-09-19  
**Compilación:** `:app:compileDebugKotlin` — **BUILD SUCCESSFUL**  
**Restricciones cumplidas:** sin nuevas features; sin rediseño de UI (solo eliminación de secretos/auto-admin); compilación OK.

---

## Resumen ejecutivo

Cierre de seguridad mínima para piloto: Firestore/Storage deny-by-default con membership en `companies/{companyId}`, eliminación de credenciales/admin auto-creado, cold start limpio, y rules de emergencia archivadas.

| # | Acción | Estado |
|---|--------|--------|
| 1 | Reescribir `firestore.rules` | ✅ |
| 2 | Reescribir `storage.rules` (membership + deny) | ✅ |
| 3 | Eliminar `admin@nexogo.com` / `123456` / auto-admin | ✅ |
| 4 | Limpiar `MainActivity` | ✅ |
| 5 | Archivar `firestore_rules_emergency.rules` | ✅ |

---

## #1 Firestore rules

**Archivo:** `firestore.rules`

- `isSignedIn()`, `isCompanyMember(companyId)` vía `companies/{id}/memberships/{uid}` con `status == 'ACTIVE'`.
- `companies/{companyId}`: create autenticado (`id == companyId`); read/update solo miembros; delete denegado.
- Bootstrap membership (creator → propia `ACTIVE`) y `settings` create en alta de company.
- Resto tenant: `companies/{companyId}/{subcollection}/{docId}` (+ nested) solo miembros.
- `company_index`, `users` / `usuarios`, mirror `company_memberships`.
- Catálogo IAM (`permissions_catalog`, `platform_roles`, `role_permissions`): read auth, write denegado en cliente.
- `platform/audit_logs`: create auth; read denegado.
- Legacy root (patients, citas, products, sales, chat, …): staff por `usuarios.rol` — **sin catch-all**.
- Default: `match /{document=**} { allow read, write: if false; }`

**Ops:** desplegar en Firebase Console / CLI antes de probar en device.

---

## #2 Storage rules

**Archivo:** `storage.rules`

- `companies/{companyId}/{allPaths=**}`: read/write si membership `ACTIVE`.
- Paths legacy allow-list (profile, patients, medical_records, inventory, chats, sales, reports, temp, public).
- Default deny: `match /{allPaths=**} { allow read, write, delete: if false; }`

---

## #3 Credenciales y auto-admin

Eliminado / neutralizado en código Kotlin:

| Área | Cambio |
|------|--------|
| `FirebaseAuthRepository` | Sin `init` auto-create; sin privilegio por email |
| `PersistentAuthViewModel` | Sin `createDefaultAdmin` / manual |
| `AuthViewModel` | Sin admin pre-autenticado en `init` |
| `ProfileViewModel` | Sin `fixAdminRole` por email |
| `SimpleLoginScreen` | Sin LaunchedEffect create admin; sin role por email |
| `modules/auth/LoginScreen` | Sin card de usuarios/contraseñas de prueba |
| `FirebaseDiagnostics` | Sin sign-in con password hardcodeada |
| `FirebaseAutoFixer` | Sin recrear admin |
| Mock/test helpers | Emails demo `*.example.com`; password test no usable |

`admin@nexogo.com` y `"123456"` **no aparecen** en `app/src/main/java/**/*.kt`.

---

## #4 MainActivity

`MainActivity` / `NexoGoApp` ahora solo:

- tema + `CompanyProvider` + `NavGraph` (Splash)

**Eliminado del cold start:** `MockDataGenerator`, `FirebaseDiagnostics`, `FirestorePermissionTester`, `AppointmentDiagnostics`, `AppointmentDebugger`, seed de citas, writes a `test`/`diagnostics`.

---

## #5 Emergency rules archivadas

| Antes | Después |
|-------|---------|
| `firestore_rules_emergency.rules` (root, `if true`) | `archived/security/firestore_rules_emergency.rules.ARCHIVED` |
| — | `archived/security/README.md` (no desplegar) |

---

## Compilación

```
./gradlew :app:compileDebugKotlin
→ BUILD SUCCESSFUL
```

Warnings residuales solo en `FirebaseFullTestActivity` (preexistentes / no bloqueantes).

---

## Pendiente operativo (fuera de código)

1. **Publicar** `firestore.rules` y `storage.rules` en el proyecto Firebase.  
2. Verificar bootstrap: registro → `createCompany` + membership `ACTIVE` con rules desplegadas.  
3. Cuentas `admin@nexogo.com` ya existentes en Auth: rotar/eliminar manualmente en Console (no se borran solas).  
4. Siguiente sprint: **S1 Spine** (sesión company obligatoria, hub único, PermissionEngine).

---

## Archivos tocados (principales)

```
firestore.rules
storage.rules
archived/security/firestore_rules_emergency.rules.ARCHIVED
archived/security/README.md
app/.../MainActivity.kt
app/.../repository/FirebaseAuthRepository.kt
app/.../viewmodel/PersistentAuthViewModel.kt
app/.../viewmodel/AuthViewModel.kt
app/.../viewmodel/ProfileViewModel.kt
app/.../ui/screens/auth/SimpleLoginScreen.kt
app/.../modules/auth/LoginScreen.kt
app/.../core/firebase/FirebaseDiagnostics.kt
app/.../core/firebase/FirebaseAutoFixer.kt
(+ mocks/tests: emails/password placeholders)
S0_SECURE_REPORT.md
```
