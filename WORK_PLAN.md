# Ruta de trabajo NexoGo

Plan operativo a partir de `CURRENT_ACTIVE_FLOW.md` y la auditoría.  
Regla de oro: **el camino vivo es intocable hasta que esté listo cada corte.**

```
splash → SimpleLoginScreen → HomeScreen
```

Todo lo que no cuelgue de esa columna se trata como candidato a borrar o a no abrir PRs.

---

## Principio de corte

Un PR = un tema. No mezclar “borrar logins” con “reglas Firestore”.

Después de cada fase: `bash ./gradlew :app:compileDebugKotlin` y humo manual del camino vivo (login, home, citas, pacientes, historial, inventario).

---

## Fase 0 — Congelar el flujo (día 1, sin features)

**Objetivo:** que nadie trabaje sobre pantallas muertas.

1. Imprimir / fijar `CURRENT_ACTIVE_FLOW.md` como contrato.
2. En `NavGraph.kt`, no añadir rutas nuevas.
3. Lista blanca de archivos “producción ahora”:

| Área | Archivos vivos |
|---|---|
| Arranque | `MainActivity.kt`, `FirebaseConfig.kt` |
| Nav | `NavGraph.kt`, `Screen.kt` |
| Auth | `MinimalSplashScreen.kt`, `SimpleLoginScreen.kt`, `RegisterScreen.kt`, `FirebaseAuthRepository.kt`, `PersistentAuthViewModel.kt` |
| Home | `HomeScreen.kt` |
| Citas | `BeautifulAppointmentsScreen.kt`, `BeautifulEditAppointmentScreen.kt`, `CreateAppointmentScreen.kt`, `AppointmentViewModelImproved.kt`, `FirebaseAppointmentViewModel.kt`, `repository/FirebaseRepository.kt` |
| Pacientes | `PatientsScreen.kt`, `CreateEditPatientScreen.kt`, `viewmodel/PatientViewModel.kt` |
| Historial | `ClinicalHistoryScreen.kt`, `ClinicalHistoryViewModel.kt`, `CreateEditClinicalRecordScreen.kt`, `ClinicalRecordViewModel.kt` |
| Inventario | `InventoryMainScreen.kt`, `InventoryEditScreen.kt`, `InventoryRepository.kt`, `modules/inventory/viewmodel/InventoryViewModel.kt` |
| Ventas | `SalesMainScreen.kt` (stub), `SalesEditScreen.kt`, `ServicesManagementScreen.kt`, `SalesRepository.kt` |
| Resto Home | profile, settings simple, admin, chat según NavGraph |

Si un archivo no está aquí ni es dependencia directa, **no se “mejora”: se borra o se deja para fase 2.**

---

## Fase 1 — Seguridad (antes de seguir desarrollando)

Sin esto, cualquier feature nueva escribe datos clínicos en un Firestore abierto.

Orden interno:

### 1.1 Apagar el prototipo en runtime

En `MainActivity` / `NexoGoApp`:

- Quitar `MockDataGenerator`, `FirebaseDiagnostics`, `FirestorePermissionTester`, `AppointmentDiagnostics`, `AppointmentDebugger` del `LaunchedEffect`.
- Dejar Firebase init en `FirebaseConfig` solamente.

En `FirebaseAuthRepository` y `SimpleLoginScreen`:

- Quitar creación de `admin@nexogo.com` / `123456`.
- Crear el admin **una vez** en Firebase Console / script fuera de la app.

### 1.2 Superficie de ataque

- `FirebaseFullTestActivity`: `exported=false` y quitar de Home / NavGraph en release (o `debugImplementation` / flavor).
- Quitar ruta `firebase_test` del camino vivo.
- `usesCleartextTraffic=false` salvo que un endpoint lo exija.
- Recortar `println` / `Log` con email, uid, nombres.

### 1.3 Firestore / Storage (bloquea producción)

1. Unificar **un** vocabulario de roles (`ADMIN`, `VET`, `VET_ASSISTANT`, `USER` **o** el de `model.UserRole`, no ambos).
2. Custom Claims (o documento `users/{uid}` leído en rules **sin** catch-all).
3. Reescribir `firestore.rules`: **borrar** `match /{document=**} { allow read, write: if request.auth != null }` y `firestore_rules_emergency.rules` (`if true`).
4. Colecciones reales del flujo vivo: `usuarios`/`users`, `citas`, `clinical_records`, inventario, ventas, chats.
5. Publicar reglas en Console y probar con un user no-admin (debe fallar writes ajenos).
6. Cloud Functions: `context.auth` + rol admin; no dejar placeholders de Gmail en repo.

**Criterio de salida:** un usuario PATIENT/USER no puede leer/escribir citas ni historiales de otros desde el SDK.

---

## Fase 2 — Limpiar código muerto (NavGraph primero)

Borrar de **afuera hacia adentro**: primero dejar de referenciar, luego borrar archivos.

### 2.1 NavGraph: dejar de importar lo no compuesto

Quitar imports muertos (§6.1 de `CURRENT_ACTIVE_FLOW.md`).

### 2.2 Quitar rutas huérfanas del grafo

En el **mismo PR o el siguiente**, eliminar `composable` de:

- `modern_login`, `ultra_simple_login`, `minimal_login`, `dashboard`
- `history_list`, `history_detail`, `history_edit`, `history_pdf_preview` (Home usa `clinical_records`, no este stack)
- placeholders `reports_*` extra si Home solo usa `reports`
- `sale_detail/{saleId}` si `SalesMainScreen` no navega ahí
- objetos `Screen` sin composable: `chat`, `admin_dashboard`, `user_management`, `professional_dashboard`, `patient_dashboard`

Dejar **una** ruta `reports` placeholder hasta fase 5.

### 2.3 Borrar pantallas / VMs duplicados

Oleada sugerida (un PR por dominio):

1. **Auth:** `LoginScreen`, `Modern*`, `UltraSimple*`, `Minimal*`, `GoogleSignInScreen`, `SplashScreen`, `UltraSimpleSplashScreen`, `modules/auth/*`.
2. **Citas:** `Safe/Simple/Minimal/Test/Improved` screens, `EditAppointmentScreen`, `EditAppointmentScreenById`, `modules/appointments/AppointmentScreen`, `SafeFirebaseAppointmentViewModel`, `viewmodel/AppointmentViewModel`.
3. **Dashboards:** `MinimalDashboardScreen`, `SimpleDashboardScreen`, `modules/dashboard/*`.
4. **Historial paralelo:** `SimpleHistory*`, `UltraSimpleHistory*`, `HistoryList/Edit/Detail` no cableados, `ClinicalRecordsScreen` (ui) si NavGraph ya usa `ClinicalHistoryScreen`.
5. **Inventario/ventas/chat/profile** módulos no registrados.
6. **Hilt muerto:** `*RepositoryImpl` `@Inject` no usados, o reactivar Hilt en una fase posterior (no ahora).
7. `NexoGoApplication.kt` si sigue sin manifiesto.

**Criterio de salida:** `NavGraph` solo importa composables que instancia; `./gradlew :app:compileDebugKotlin` verde; camino vivo igual.

---

## Fase 3 — Unificar duplicados **del camino vivo**

No unificar “el mejor módulo” en abstracto: unificar lo que Home ya usa.

| Dominio | Decisión (contrato actual) | Trabajo |
|---|---|---|
| Auth | `SimpleLogin` + `FirebaseAuthRepository` + `PersistentAuthViewModel` | Un `User` / un `UserRole`. Matar `viewmodel.AuthViewModel` del resto o delegarlo al persistente. |
| Citas | Beautiful* + `AppointmentViewModelImproved` | Crear cita debe usar **el mismo** VM que listar/editar (hoy `FirebaseAppointmentViewModel` es otro). Un solo `FirebaseRepository` de citas. |
| Pacientes | UI `ui/screens/patients` | Hoy DataStore. Elegir: Firestore (`core.FirebaseRepository`) **o** local. No los dos. |
| Historial | Lista = Firestore (`ClinicalHistoryViewModel`); alta = DataStore (`ClinicalRecordViewModel`) | **Un** modelo + un repo. Cablear create/edit al mismo stack que la lista. Quitar `view_clinical_record` o navegarlo de verdad. |
| Inventario | `modules/inventory/ui` + `core.FirebaseRepository` | Borrar `ui/screens/inventory` y el otro `InventoryViewModel`. |
| Ventas | `SalesEdit` / servicios sí tienen repo; **listado es stub** | Implementar listado con el mismo `SalesViewModel` de edit. |
| Chat | Imports conflictivos en NavGraph | Una lista + una conversación + un `ChatViewModel` + un repo. Arreglar navigate a `createRoute`. |
| Profile | `ui/screens/profile` | Borrar `modules/profile`. |
| FirebaseRepository | dos clases mismo nombre | Quedar **una**: citas (`citas`) + genérico, o adaptar citas a `core`. |

**Criterio de salida:** un login, un VM por feature viva, un repo Firebase, un enum de roles.

---

## Fase 4 — Hacer el MVP usable (sobre el código ya único)

Orden de producto (clínica):

1. Auth: splash que **si hay sesión** vaya a Home; logout coherente.
2. Pacientes CRUD real en el backend elegido.
3. Citas: listar / crear / editar / estados con el mismo VM.
4. Historial: lista + alta + ver; adjuntos o recortar el TODO de cámara.
5. Inventario: listar / crear / editar / borrar (hoy hay TODOs de delete).
6. Ventas: listado real + `createdBy` = uid de Auth (quitar `"current_user"`).
7. Admin: aprobación de usuarios con rules (no solo UI).

Aplazar: reportes, PDF iText, chatbot, Google Sign-In, backup/restore.

---

## Fase 5 — Ingeniería de release

- Package `com.example.nexogo` → id propio.
- Compose BOM / Firebase BOM / DataStore al día (PR separado).
- R8 + signing; flavor `debug` (test activity) vs `release`.
- Crashlytics.
- Node 20 + Functions autenticadas, o borrar functions no usadas.

---

## Por dónde **empezar mañana** (secuencia concreta)

| Orden | PR | Qué tocar | Qué no tocar |
|---|---|---|---|
| **A** | Seguridad runtime | `MainActivity.kt`, `FirebaseAuthRepository.kt`, `SimpleLoginScreen.kt`, manifiesto test activity | NavGraph grande, features |
| **B** | Rules | `firestore.rules`, borrar emergency `if true`, Console | UI |
| **C** | NavGraph diet | Quitar composables huérfanos + imports | Lógica de Home |
| **D** | Borrar auth/citas/dashboard muertos | archivos de §6 | Beautiful*, SimpleLogin, Home |
| **E** | Un VM de citas | CreateAppointment → Improved | pacientes |
| **F** | Un stack historial | create/edit → mismo repo que la lista | reportes |
| **G** | Pacientes a un backend | PatientViewModel + repo | chat |
| **H** | SalesMainScreen de verdad | mismo VM que Edit | PDF |

No empezar por “actualizar Compose BOM” ni por “implementar reportes”: no desbloquean el MVP y ensanchan el diff sobre código que luego se borra.

---

## Definition of done (MVP interno)

- [ ] App abre: splash → login → home (o home si hay sesión).
- [ ] No hay mock ni admin `123456` en cliente.
- [ ] Rules niegan a un user no privilegiado.
- [ ] NavGraph sin rutas de login/dashboard/history paralelas.
- [ ] Pacientes, citas, historial e inventario CRUD sobre **un** backend.
- [ ] Listado de ventas no está vacío por `remember { emptyList() }`.
- [ ] Compile debug verde.

Cuando A+B estén mergeados, el equipo puede paralelizar D (borrados) con E–H (unificación de vivos).
