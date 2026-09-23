# CURRENT_ACTIVE_FLOW

Inventario del flujo que la app **realmente ejecuta**.  
Fuentes: `MainActivity.kt`, `navigation/NavGraph.kt`, `navigation/Screen.kt` y cada composable pasado a `composable(...)`.  
Fecha: 15 sep 2026. **Sin cambios de código.**

Leyenda:

| Término | Significado |
|---|---|
| Camino vivo | Lo que corre al abrir el launcher, sin teclear rutas |
| Registrado | Hay `composable(...)` en `NavGraph` |
| Declarado | Existe en `Screen.kt` |
| Muerto | Compila y no se alcanza desde el camino vivo ni se instancia ahí |

---

## 1. Flujo real de inicio

Launcher: `MainActivity` (`AndroidManifest` → `MAIN` / `LAUNCHER`).

`Application` real: `FirebaseConfig` (no `NexoGoApplication`).

```
onCreate
  ├─ UncaughtExceptionHandler → core.firebase.FirebaseErrorHandler
  └─ setContent → NexoGoTheme → NexoGoApp()
        ├─ LaunchedEffect (cada cold start, paralelo a la UI)
        │     SafeFirestoreOperations.safeSet("test","connection")
        │     FirebaseDiagnostics.runFullDiagnostics
        │     FirestorePermissionTester.testAllCollections
        │     AppointmentDiagnostics (+ createTestAppointment si vacío)
        │     AppointmentDebugger.debug + createTestAppointmentIfNeeded
        │     si permisos OK: core.FirebaseRepository + MockDataGenerator.generateAllMockData()
        └─ NavGraph(
              startDestination = Screen.Splash.route   // "splash"
           )
```

El parámetro por defecto de `NavGraph` es `Screen.Login.route`, pero **MainActivity lo ignora** y fuerza splash.

Secuencia UI:

```
splash  (MinimalSplashScreen, delay 2 s, sin ViewModel, sin Auth)
   └─ navigate("login") popUpTo splash inclusive
         login  (SimpleLoginScreen)
            ├─ Register → "register"
            └─ éxito → "home" popUpTo login inclusive
                  HomeScreen
```

No hay restauración de sesión en el splash: siempre login, aunque Firebase Auth tenga usuario.

---

## 2. Pantallas efectivamente registradas

Hay **50** `composable(...)` en `NavGraph.kt`.  
`Screen.kt` declara **48** rutas; **5** de ellas no tienen `composable`. Algunas rutas extra se registran concatenando `/{id}` y no coinciden 1:1 con el `route` del sealed class.

### 2.1 Tabla completa NavGraph → composable

| # | Ruta registrada | `Screen` | Composable instanciado | Archivo |
|---|---|---|---|---|
| 1 | `splash` | Splash | `MinimalSplashScreen` | `ui/screens/auth/MinimalSplashScreen.kt` |
| 2 | `login` | Login | `SimpleLoginScreen` | `ui/screens/auth/SimpleLoginScreen.kt` |
| 3 | `modern_login` | ModernLogin | `ModernLoginScreen` | `ui/screens/auth/ModernLoginScreen.kt` |
| 4 | `ultra_simple_login` | UltraSimpleLogin | `UltraSimpleLoginScreen` | `ui/screens/auth/UltraSimpleLoginScreen.kt` |
| 5 | `minimal_login` | MinimalLogin | `MinimalLoginScreen` | `ui/screens/auth/MinimalLoginScreen.kt` |
| 6 | `register` | Register | `RegisterScreen` | `ui/screens/auth/RegisterScreen.kt` |
| 7 | `home` | Home | `HomeScreen` | `ui/screens/home/HomeScreen.kt` |
| 8 | `dashboard` | Dashboard | `MinimalDashboardScreen` | `ui/screens/dashboard/MinimalDashboardScreen.kt` |
| 9 | `profile` | Profile | `ProfileScreen` | `ui/screens/profile/ProfileScreen.kt` |
| 10 | `appointments` | Appointments | `BeautifulAppointmentsScreen` | `ui/screens/appointments/BeautifulAppointmentsScreen.kt` |
| 11 | `create_appointment` | CreateAppointment | `CreateAppointmentScreen` | `ui/screens/appointments/CreateAppointmentScreen.kt` |
| 12 | `edit_appointment/{appointmentId}` | EditAppointment | `BeautifulEditAppointmentScreen` | `ui/screens/appointments/BeautifulEditAppointmentScreen.kt` |
| 13 | `patients` | Patients | `PatientsScreen` | `ui/screens/patients/PatientsScreen.kt` |
| 14 | `create_edit_patient/{patientId}` | (extra) | `CreateEditPatientScreen` | `ui/screens/patients/CreateEditPatientScreen.kt` |
| 15 | `create_edit_patient` | CreateEditPatient | `CreateEditPatientScreen` | igual |
| 16 | `clinical_records` | ClinicalRecords | `ClinicalHistoryScreen` | `modules/history/ClinicalHistoryScreen.kt` |
| 17 | `create_edit_clinical_record/{recordId}` | (extra) | `CreateEditClinicalRecordScreen` | `ui/screens/clinical/CreateEditClinicalRecordScreen.kt` |
| 18 | `create_edit_clinical_record` | CreateEditClinicalRecord | igual | igual |
| 19 | `view_clinical_record/{recordId}` | ViewClinicalRecord + sufijo | `ViewClinicalRecordScreen` | `ui/screens/clinical/ViewClinicalRecordScreen.kt` |
| 20 | `history_list` | HistoryList | `SimpleHistoryListScreen` | `modules/history/ui/SimpleHistoryListScreen.kt` |
| 21 | `history_detail/{recordId}` | HistoryDetail | `HistoryDetailScreen` | `modules/history/ui/HistoryDetailScreen.kt` |
| 22 | `history_edit?recordId={recordId}` | HistoryEdit | `SimpleHistoryEditScreen` | `modules/history/ui/SimpleHistoryEditScreen.kt` |
| 23 | `history_pdf_preview/{recordId}` | HistoryPdfPreview | `HistoryPdfPreview` | `modules/history/ui/HistoryPdfPreview.kt` |
| 24 | `inventory` | Inventory | `InventoryMainScreen` | `modules/inventory/ui/InventoryMainScreen.kt` |
| 25 | `create_edit_product?productId={productId}` | CreateEditProduct | `InventoryEditScreen` | `modules/inventory/ui/InventoryEditScreen.kt` |
| 26 | `category_management` | CategoryManagement | `CategoryManagementScreen` | `modules/config/ui/CategoryManagementScreen.kt` |
| 27 | `sales` | Sales | `SalesMainScreen` | `modules/sales/ui/SalesMainScreen.kt` |
| 28 | `create_edit_sale?saleId={saleId}` | CreateEditSale | `SalesEditScreen` | `modules/sales/ui/SalesEditScreen.kt` |
| 29 | `services_management` | ServicesManagement | `ServicesManagementScreen` | `modules/sales/ui/ServicesManagementScreen.kt` |
| 30 | `medical_records` | MedicalRecords | `MedicalRecordsScreen` | `ui/screens/medical/MedicalRecordsScreen.kt` |
| 31 | `chat_list` | ChatList | `ChatListScreen` | ver nota chat |
| 32 | `new_chat` | NewChat | `NewChatScreen(...)` con `onNavigateToChat` | firma de `ui/screens/chat/NewChatScreen.kt` |
| 33 | `chat_conversation/{chatId}` | ChatConversation | `NewChatScreen(conversationId, ...)` | firma de `modules/chat/ChatScreen.kt` |
| 34 | `chatbot_config` | ChatbotConfig | `ChatbotConfigScreen` | `ui/screens/chat/ChatbotConfigScreen.kt` |
| 35 | `settings` | Settings | `SimpleSettingsScreen` | `ui/screens/settings/SimpleSettingsScreen.kt` |
| 36 | `storage_management` | StorageManagement | `StorageManagementScreen` | `ui/screens/settings/StorageManagementScreen.kt` |
| 37 | `product_categories` | ProductCategories | `ProductCategoriesScreen` | `ui/screens/settings/ProductCategoriesScreen.kt` |
| 38 | `sale_detail/{saleId}` | SaleDetail + sufijo | `ui.screens.sales.SalesScreen` | `ui/screens/sales/SalesScreen.kt` |
| 39 | `reports` | Reports | `Text("Pantalla en desarrollo")` | inline en NavGraph |
| 40–45 | `reports_main`, `sales_reports`, `appointments_reports`, `patients_reports`, `inventory_reports`, `clinical_reports` | *Reports | igual, placeholder | inline |
| 46 | `help` | Help | `HelpScreen` | `ui/screens/settings/HelpScreen.kt` |
| 47 | `about` | About | `AboutScreen` | `ui/screens/settings/AboutScreen.kt` |
| 48 | `admin` | Admin | `AdminScreen` | `ui/screens/admin/AdminScreen.kt` |
| 49 | `user_approval` | UserApproval | `UserApprovalScreen` | `ui/screens/admin/UserApprovalScreen.kt` |
| 50 | `firebase_test` | FirebaseTest | `FirebaseTestScreen` | `FirebaseFullTestActivity.kt` |

**Nota chat:** `NavGraph` importa `ui.screens.chat.ChatListScreen` y luego `modules.chat.ChatListScreen` (mismo nombre). Las tres lambdas coinciden con ambas. También importa `ui.screens.chat.NewChatScreen` y `modules.chat.ChatScreen as NewChatScreen`. `ChatConversationScreen` se importa y **no se llama**.

### 2.2 En `Screen.kt` sin `composable`

`chat`, `admin_dashboard`, `user_management`, `professional_dashboard`, `patient_dashboard`.

`ViewClinicalRecord.route` es `"view_clinical_record"`; lo registrado es `"view_clinical_record/{recordId}"`.  
`SaleDetail.route` es `"sale_detail"`; lo registrado es `"sale_detail/{saleId}"`.

---

## 3. Login actualmente utilizado

| Pieza | Valor |
|---|---|
| Splash vivo | `MinimalSplashScreen` |
| Login vivo | **`SimpleLoginScreen` en `"login"`** |
| Registro vivo | `ui.screens.auth.RegisterScreen` |
| Post-login | **`HomeScreen` (`"home"`)** — nunca `dashboard` en este camino |

Mecánica del login vivo:

- No usa `viewModel()` / Hilt.
- `remember { FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()) }`.
- Puede crear `admin@nexogo.com` / `123456` (pantalla + `init` del repo).
- Tras éxito: `AppDataStore` + `PersistentAuthViewModel.getInstance(context)` y `onNavigateToHome()`.

Registrados pero **no usados** al arrancar: `ModernLoginScreen`, `UltraSimpleLoginScreen`, `MinimalLoginScreen` (van a `"dashboard"`).

No registrados: `LoginScreen`, `GoogleSignInScreen`, `modules/auth/LoginScreen`, `SplashScreen`, `UltraSimpleSplashScreen`.

---

## 4. ViewModels realmente instanciados

Instanciación = default param, `remember { }`, `getInstance`, `viewModel()` o constructor en el lambda de NavGraph, desde un composable **registrado**.

### 4.1 Camino vivo (splash → login → home → hijos de Home)

| ViewModel | Dónde | Cómo |
|---|---|---|
| `PersistentAuthViewModel` | `SimpleLoginScreen` (éxito), `HomeScreen`, `AdminScreen`, `UserApprovalScreen` | `getInstance(context)` |
| `ProfileViewModel` (`viewmodel`) | `ProfileScreen` | `viewModel()` |
| `AppointmentViewModelImproved` | `BeautifulAppointmentsScreen`, `BeautifulEditAppointmentScreen` | `remember { }` |
| `FirebaseAppointmentViewModel` | `CreateAppointmentScreen` | default constructor |
| `PatientViewModel` (`viewmodel`) | pacientes + create/edit + create/edit/view clinical | `getInstance(context)` |
| `ClinicalHistoryViewModel` | `ClinicalHistoryScreen` | `viewModel()` |
| `ClinicalRecordViewModel` | `CreateEditClinicalRecordScreen`, `ViewClinicalRecordScreen` | `getInstance` |
| `modules.inventory.viewmodel.InventoryViewModel` | `InventoryMainScreen`, `InventoryEditScreen` | `remember` |
| `modules.sales.viewmodel.SalesViewModel` | `SalesEditScreen`, `ServicesManagementScreen` | `remember` |
| — | **`SalesMainScreen` no instancia ViewModel** | listas vacías en Compose |
| `AuthViewModel` (`viewmodel`) | `MedicalRecordsScreen` | `getInstance()` |
| `ChatViewModel` (`viewmodel`) | `ChatbotConfigScreen`; `ui` ChatList / NewChat **si** gana el import | `getInstance` |
| `modules.chat.ChatViewModel` | modules ChatList / ChatScreen **si** gana el import | `viewModel()` |
| `SettingsViewModel` | `StorageManagementScreen` | `getInstance` |
| `ProductCategoryViewModel` | `ProductCategoriesScreen` | `getInstance` |
| `FirebaseSalesViewModel` | `ui.screens.sales.SalesScreen` (`sale_detail/{saleId}`) | default — Home **no navega** a esta ruta |
| `HistoryViewModel` | HistoryDetail y PdfPreview **en el lambda de NavGraph** | `HistoryViewModel(HistoryRepository(...))` — no sale de Home |

Sin ViewModel: splash, login/register (repo + coroutine), `SimpleSettingsScreen`, Help, About, `MinimalDashboardScreen`, `CategoryManagementScreen` (repo en Compose), `FirebaseTestScreen`, placeholders de reportes, `SimpleHistoryListScreen` / `SimpleHistoryEditScreen` (repo en Compose).

Logins alternativos: `AuthViewModel` vía `viewModel()` en Modern / UltraSimple.

### 4.2 Existentes y no instanciados por pantallas registradas vivas

`viewmodel.AppointmentViewModel`, `SafeFirebaseAppointmentViewModel`, `FirebasePatientViewModel`, `FirebaseInventoryViewModel`, `FirebaseChatViewModel`, `FirebaseMedicalRecordViewModel`, `FirebaseSettingsViewModel`, `modules.auth.AuthViewModel`, `modules.patients.PatientViewModel`, `modules.appointments.AppointmentViewModel`, `modules.profile.ProfileViewModel`, `modules.dashboard.DashboardViewModel`, `modules.config.ConfigViewModel`, `modules.inventory.InventoryViewModel` (paquete raíz, no `viewmodel/`), `modules.sales.SalesViewModel` (`modules/sales/SalesViewModel.kt`).

---

## 5. Repositorios realmente utilizados

### 5.1 En el camino vivo

| Repo / acceso | Quién |
|---|---|
| `repository.FirebaseAuthRepository` | login, registro, `PersistentAuthViewModel`, `UserApprovalScreen` |
| `repository.FirebaseRepository` | `AppointmentViewModelImproved`, `FirebaseAppointmentViewModel` → colección **`citas`** |
| `core.FirebaseRepository` | MainActivity mock; `ClinicalHistoryViewModel`; inventario; `CategoryManagementScreen`; `SalesRepository` en edit/servicios; history list/edit |
| `modules.inventory.data.InventoryRepository` | pantallas inventario |
| `modules.sales.data.SalesRepository` | `SalesEditScreen`, `ServicesManagementScreen` (**no** el listado `SalesMainScreen`) |
| `repository.ChatRepository` y/o `modules.chat.ChatRepository` | según import de chat |
| Auth / Firestore / Storage `getInstance()` | `ProfileViewModel` (sin repo) |
| `AppDataStore` | pacientes, clinical record VM, settings, categorías, persistencia de sesión |

### 5.2 Solo fuera de Home (rutas history_detail / pdf)

`modules.history.repo.HistoryRepository` + `core.FirebaseRepository` (construidos en NavGraph).

### 5.3 No utilizados (Hilt off o sin llamadas desde UI registrada viva)

`FirebaseFirestoreRepository`, `FirebaseStorageRepository`, `FirebaseMessagingRepository`, `AuthRepositoryImpl`, `PatientRepositoryImpl`, `ProductRepositoryImpl`, `UserRepositoryImpl`, `LocalDataRepositoryImpl`, `modules.auth.AuthRepository`, `modules.history.repo.PetRepository` (sí en `HistoryEditScreen`, que NavGraph **no** compone; usa `SimpleHistoryEditScreen`).

FCM: `NexoGoMessagingService` en manifiesto; no es navegación.

---

## 6. Código muerto no referenciado (por el flujo activo)

### 6.1 Importado en NavGraph y nunca compuesto

`LoginScreen`, `SplashScreen`, `UltraSimpleSplashScreen`, `AppointmentsScreen`, `SimpleAppointmentsScreen`, `SafeAppointmentsScreen`, `MinimalAppointmentsScreen`, `TestAppointmentsScreen`, `AppointmentsScreenImproved`, `ui.screens.chat.ChatScreen`, `ChatConversationScreen`, `HistoryListScreen`, `HistoryEditScreen`, `UltraSimpleHistoryScreen`, `modules.sales.SalesScreen` (alias `NewSalesScreen`), `ConfigScreen`, `modules.dashboard.DashboardScreen`, `SimpleDashboardScreen`, `SettingsScreen`.

### 6.2 Registrados pero inalcanzables desde splash → SimpleLogin → Home

`modern_login`, `ultra_simple_login`, `minimal_login`, `dashboard`, `history_*`, `view_clinical_record/{recordId}` (Home no pasa navegación a ver; `ClinicalHistoryScreen` solo tiene `onNavigateToCreateRecord`), `sale_detail/{saleId}` (`SalesMainScreen` no navega ahí), `reports_main` y reportes hijos (Home solo abre `reports`).

### 6.3 Composables en disco no usados por NavGraph

Entre otros: `ui/screens/auth/LoginScreen.kt`, `GoogleSignInScreen.kt`, `modules/auth/*`, `modules/appointments/AppointmentScreen.kt`, `modules/patients/PatientScreen.kt`, `modules/profile/ProfileScreen.kt`, `modules/inventory/InventoryScreen.kt`, `InventoryListScreen.kt`, `ui/screens/inventory/InventoryScreen.kt`, `ui/screens/clinical/ClinicalRecordsScreen.kt`, `EditAppointmentScreen.kt`, `EditAppointmentScreenById.kt`, `ui/components/SafeSettingsScreen.kt`.

### 6.4 Otros

- `NexoGoApplication.kt` — no está en el manifiesto.
- Repos `@Inject` — Hilt comentado en Gradle.
- `SalesMainScreen.kt` está **registrado** pero es UI demo (lista vacía); el repo de ventas solo vive en create/edit/servicios.

---

## 7. Árbol completo de navegación

### 7.1 Camino vivo

```
MainActivity
└─ NavGraph startDestination = "splash"
   └─ splash  MinimalSplashScreen
      └─ login  SimpleLoginScreen
         ├─ register  RegisterScreen ──► login | home
         └─ home  HomeScreen
            ├─ profile
            ├─ appointments
            │     ├─ create_appointment
            │     └─ edit_appointment/{appointmentId}
            ├─ patients
            │     ├─ create_edit_patient
            │     └─ create_edit_patient/{patientId}
            ├─ clinical_records  ClinicalHistoryScreen
            │     ├─ create_edit_clinical_record
            │     └─ create_edit_clinical_record/{recordId}
            │     (view_clinical_record/{id} registrado, sin navigate desde aquí)
            ├─ inventory
            │     ├─ create_edit_product?productId=
            │     └─ category_management
            ├─ medical_records
            ├─ chat_list
            │     ├─ new_chat ──► chat_conversation/{chatId}   [createRoute OK]
            │     └─ chat_conversation/{chatId}/$id            [BUG: no matchea la ruta]
            ├─ sales
            │     ├─ create_edit_sale?saleId=
            │     └─ services_management
            ├─ reports                         placeholder
            ├─ settings
            │     ├─ storage_management
            │     ├─ product_categories
            │     ├─ chatbot_config
            │     ├─ help
            │     ├─ about
            │     └─ profile
            ├─ admin
            │     └─ user_approval
            ├─ firebase_test
            └─ logout ──► login
```

### 7.2 Árbol huérfano (registrado, no sale del splash vivo)

```
modern_login | ultra_simple_login | minimal_login
   └─ dashboard  MinimalDashboardScreen
         ├─ profile
         ├─ appointments  (mismo árbol de citas)
         ├─ patients
         ├─ history_list  SimpleHistoryListScreen
         │     ├─ history_edit?recordId=
         │     └─ (delete TODO, no repo en NavGraph)
         ├─ inventory / sales / chat_list / settings
         └─ logout ──► login
                history_detail/{id} ──► history_edit (sin id) | history_pdf_preview/{id}
```

Nadie en el código de NavGraph hace `navigate(HistoryDetail.createRoute(...))` desde `SimpleHistoryListScreen` (solo create/edit). `history_detail` queda registrado y prácticamente huérfano.

### 7.3 Ramas placeholder / detalle de venta

```
reports ──► Text en desarrollo
reports_main, sales_reports, appointments_reports,
patients_reports, inventory_reports, clinical_reports
   └── mismo placeholder (sin botones desde Home salvo "reports")

sale_detail/{saleId} ──► ui SalesScreen
   (no hay navigate desde SalesMainScreen)
```

### 7.4 Declarado y sin grafo

```
Screen.Chat
Screen.AdminDashboard
Screen.UserManagement
Screen.ProfessionalDashboard
Screen.PatientDashboard
```

---

## Conclusión operativa

El producto que el usuario ve al instalar/abrir es:

**MinimalSplash → SimpleLogin → Home**, con citas Beautiful + `repository.FirebaseRepository`, inventario/ventas `modules` + `core.FirebaseRepository` (listado de ventas aún stub), historial **lista Firestore / alta DataStore**, pacientes **solo DataStore**, y un segundo grafo de logins/dashboard/history que no se dispara al arrancar.

Cualquier limpieza debe preservar esa columna y tratar el resto como candidato a borrar o a no tocar.
