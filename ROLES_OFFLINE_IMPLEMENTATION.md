# 🔐 Implementación de Control de Acceso por Roles y Modo Offline

## 📋 Resumen

Este documento describe la implementación completa de:
1. **Control de acceso por roles** (ADMIN, VET, VET_ASSISTANT, USER) por pantalla
2. **Modo offline** con Firestore (persistencia local, cola de escrituras, UI que indica estado)
3. **Firestore Rules** que protegen las colecciones por rol

## 🚀 Configuración Inicial

### 1. Habilitar Persistencia Offline

La persistencia offline se habilita automáticamente en `MainActivity.onCreate()`:

```kotlin
FirestoreConfig.enablePersistence()
```

**Ubicación**: `app/src/main/java/com/example/nexogo/core/firebase/FirestoreConfig.kt`

### 2. Iniciar Monitor de Red

El monitor de red se inicia automáticamente en `MainActivity.onCreate()`:

```kotlin
NetworkStatus.startMonitor(this)
```

**Ubicación**: `app/src/main/java/com/example/nexogo/core/network/NetworkStatus.kt`

## 📁 Archivos Generados

### Core

1. **FirestoreConfig.kt** - Configuración de persistencia offline
2. **SessionManager.kt** - Gestión de sesión y caché local de roles
3. **NetworkStatus.kt** - Monitoreo de estado de conexión

### Repository

4. **CurrentUserRepository.kt** - Repositorio para perfil del usuario actual con observables

### Navigation

5. **RoleGuard.kt** - Utilidad para verificar acceso por roles

### UI Components

6. **RoleAware.kt** - Composable para mostrar contenido según rol
7. **OfflineIndicator.kt** - Indicadores visuales de estado offline/online

## 🔐 Control de Acceso por Roles

### Uso en NavGraph

Para proteger una ruta en `NavGraph.kt`:

```kotlin
import com.example.nexogo.navigation.RoleGuard
import com.example.nexogo.core.session.SessionManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val userRole = remember { SessionManager.getRoleBlocking(context) }
    
    NavHost(navController = navController, startDestination = "home") {
        // Ruta pública
        composable("home") { HomeScreen() }
        
        // Ruta protegida solo para ADMIN
        composable("admin") {
            if (RoleGuard.canAccessRoute(userRole, listOf(UserRole.ADMIN))) {
                AdminScreen()
            } else {
                // Redirigir a home o mostrar pantalla de acceso denegado
                HomeScreen()
            }
        }
        
        // Ruta protegida para VET y ASSISTANT
        composable("vet") {
            if (RoleGuard.canAccessRoute(userRole, listOf(UserRole.VET, UserRole.VET_ASSISTANT))) {
                VetScreen()
            } else {
                HomeScreen()
            }
        }
    }
}
```

### Uso en UI con RoleAware

Para mostrar/ocultar componentes según el rol:

```kotlin
import com.example.nexogo.ui.components.RoleAware
import com.example.nexogo.core.models.UserRole

@Composable
fun MyScreen(userRole: UserRole?) {
    Column {
        // Botón solo visible para VET y ASSISTANT
        RoleAware(role = userRole, allowedRoles = listOf(UserRole.VET, UserRole.VET_ASSISTANT)) {
            Button(onClick = { /* acción solo para vets */ }) {
                Text("Acción Veterinaria")
            }
        }
        
        // Botón solo visible para ADMIN
        RoleAware(role = userRole, allowedRole = UserRole.ADMIN) {
            Button(onClick = { /* acción solo para admin */ }) {
                Text("Acción Administrativa")
            }
        }
    }
}
```

## 📡 Modo Offline

### Funcionamiento Automático

Firestore maneja automáticamente el modo offline cuando la persistencia está habilitada:

1. **Lecturas**: Usan cache cuando no hay conexión
2. **Escrituras**: Se encolan automáticamente y se sincronizan cuando vuelve la conexión
3. **Listeners en tiempo real**: Continúan funcionando con datos en cache

### Detectar Escrituras Pendientes

En tus listeners de Firestore:

```kotlin
firestore.collection("domicilios")
    .addSnapshotListener { snapshot, error ->
        snapshot?.documents?.forEach { doc ->
            val hasPendingWrites = doc.metadata.hasPendingWrites()
            if (hasPendingWrites) {
                // Mostrar indicador "Pendiente de sincronizar"
            }
        }
    }
```

### Mostrar Estado Offline en UI

```kotlin
import com.example.nexogo.ui.components.OfflineIndicator
import com.example.nexogo.core.network.NetworkStatus

@Composable
fun MyScreen() {
    Column {
        // Banner de estado offline
        OfflineIndicator()
        
        // O indicador compacto
        Row {
            OfflineIndicatorCompact()
            Text("Mi contenido")
        }
        
        // Badge de sincronización pendiente
        PendingSyncBadge(hasPendingWrites = true)
    }
}
```

## 🔒 Firestore Rules

### Aplicar Reglas

1. Ve a **Firebase Console** > **Firestore Database** > **Rules**
2. Copia el contenido de `firestore_rules_with_roles.rules`
3. Pega en el editor de reglas
4. Haz clic en **Publish**

### Estructura de las Reglas

Las reglas incluyen:
- **Helper functions** para verificar roles (custom claims o documento users)
- **Protección por colección** según el rol del usuario
- **Permisos específicos** para cada operación (read, write, create, delete)

### Custom Claims (Recomendado)

Para usar `request.auth.token.role` en las reglas, necesitas asignar custom claims:

**Opción 1: Cloud Function automática**

Crea una Cloud Function que asigne claims cuando se crea un usuario:

```javascript
// cloud-functions/index.js
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.setCustomClaimsOnUserCreate = functions.firestore
    .document('users/{uid}')
    .onCreate(async (snap, context) => {
      const user = snap.data();
      const uid = context.params.uid;
      const role = user.role || 'USER';
      
      await admin.auth().setCustomUserClaims(uid, { role: role });
      console.log(`Custom claim asignado: ${uid} -> ${role}`);
      return null;
    });

exports.setCustomClaimsOnUserUpdate = functions.firestore
    .document('users/{uid}')
    .onUpdate(async (change, context) => {
      const newData = change.after.data();
      const uid = context.params.uid;
      const role = newData.role || 'USER';
      
      await admin.auth().setCustomUserClaims(uid, { role: role });
      console.log(`Custom claim actualizado: ${uid} -> ${role}`);
      return null;
    });
```

**Opción 2: Manual desde Firebase Console**

1. Ve a **Authentication** > **Users**
2. Selecciona un usuario
3. Haz clic en **Custom Claims**
4. Agrega: `{"role": "ADMIN"}` (o VET, VET_ASSISTANT, USER)

## 🧪 Testing

### Test Offline

1. **Crear un domicilio** con la app conectada
2. **Activar modo avión** en el dispositivo
3. **Crear otro domicilio** (debe guardarse localmente)
4. **Verificar** que aparece en la lista (desde cache)
5. **Desactivar modo avión**
6. **Verificar** que el domicilio se sincroniza automáticamente

### Test de Roles

1. **Login como ADMIN**: Verificar acceso a todas las pantallas
2. **Login como VET**: Verificar acceso solo a pantallas de veterinario
3. **Login como USER**: Verificar acceso solo a pantallas de usuario
4. **Intentar navegar directamente** a rutas protegidas (debe redirigir)

### Test de Firestore Rules

1. **Login como USER**: Intentar escribir en colección de inventario (debe fallar)
2. **Login como ADMIN**: Intentar escribir en inventario (debe funcionar)
3. **Verificar logs** en Firebase Console para ver intentos de acceso denegados

## 📝 Estructura de Datos

### Documento User

```json
{
  "id": "user123",
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "role": "VET",  // ADMIN | VET | VET_ASSISTANT | USER
  "isApproved": true,
  "isActive": true
}
```

**Importante**: El campo `role` debe coincidir con el enum `UserRole` en Kotlin.

## 🔧 Troubleshooting

### Problema: "Permission denied" en Firestore

**Solución**:
1. Verificar que las reglas están aplicadas en Firebase Console
2. Verificar que el usuario tiene el campo `role` en su documento
3. Si usas custom claims, verificar que están asignados correctamente

### Problema: Datos no se sincronizan offline

**Solución**:
1. Verificar que `FirestoreConfig.enablePersistence()` se llama antes de usar Firestore
2. Verificar que no hay errores en los logs
3. Verificar que la app tiene permisos de almacenamiento

### Problema: Roles no se detectan correctamente

**Solución**:
1. Verificar que `SessionManager.fetchAndSaveProfile()` se llama después del login
2. Verificar que el documento del usuario tiene el campo `role`
3. Limpiar caché de la app si es necesario

## 📚 Referencias

- [Firestore Offline Persistence](https://firebase.google.com/docs/firestore/manage-data/enable-offline)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Custom Claims](https://firebase.google.com/docs/auth/admin/custom-claims)

## ✅ Checklist de Implementación

- [x] FirestoreConfig creado y habilitado en MainActivity
- [x] SessionManager implementado con DataStore
- [x] CurrentUserRepository con observables
- [x] RoleGuard para protección de rutas
- [x] RoleAware composable para UI condicional
- [x] NetworkStatus para monitoreo de conexión
- [x] OfflineIndicator para UI
- [x] Firestore Rules actualizadas
- [x] MainActivity actualizado con inicialización
- [x] Cloud Functions para custom claims
- [x] README con instrucciones completas
- [x] Ejemplo de pantalla protegida

## 📦 Archivos Creados

### Core
- `app/src/main/java/com/example/nexogo/core/firebase/FirestoreConfig.kt`
- `app/src/main/java/com/example/nexogo/core/session/SessionManager.kt`
- `app/src/main/java/com/example/nexogo/core/network/NetworkStatus.kt`

### Repository
- `app/src/main/java/com/example/nexogo/repository/CurrentUserRepository.kt`

### Navigation
- `app/src/main/java/com/example/nexogo/navigation/RoleGuard.kt`

### UI Components
- `app/src/main/java/com/example/nexogo/ui/components/RoleAware.kt`
- `app/src/main/java/com/example/nexogo/ui/components/OfflineIndicator.kt`

### Example
- `app/src/main/java/com/example/nexogo/ui/screens/ExampleRoleProtectedScreen.kt`

### Rules & Functions
- `firestore_rules_with_roles.rules`
- `cloud-functions/index.js` (actualizado con custom claims)

### Documentation
- `ROLES_OFFLINE_IMPLEMENTATION.md` (este archivo)

## 🎯 Próximos Pasos

1. **Aplicar Firestore Rules** en Firebase Console
2. **Configurar Cloud Function** para custom claims (opcional pero recomendado)
3. **Probar modo offline** activando/desactivando conexión
4. **Probar control de acceso** con diferentes roles
5. **Agregar indicadores offline** en pantallas críticas

---

**Nota**: Todos los archivos están implementados y listos para usar. Solo falta aplicar las reglas de Firestore y configurar custom claims si se desea usar esa funcionalidad.

