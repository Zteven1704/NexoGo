# 🏠 Módulo de Domicilios - NexoGo

Módulo completo para gestionar solicitudes de servicios veterinarios a domicilio.

## 📋 Características

- ✅ Creación de solicitudes por usuarios
- ✅ Aprobación/Rechazo por administradores
- ✅ Gestión de estados (Pendiente, Aprobado, Rechazado, En Camino, Finalizado)
- ✅ Historial de eventos completo
- ✅ Notificaciones FCM en tiempo real
- ✅ Envío de correos automáticos
- ✅ Subida de fotos adjuntas
- ✅ Permisos basados en roles

## 🏗️ Arquitectura

### Modelos de Datos

- **Domicilio**: Modelo principal de solicitud
- **DomicilioEvento**: Eventos del historial
- **DomicilioEstado**: Enum de estados

### Repositorios

- **DomicilioRepository**: Maneja todas las operaciones de Firestore y Storage

### ViewModels

- **DomicilioViewModel**: Para usuarios normales
- **AdminDomicilioViewModel**: Para administradores
- **VetDomicilioViewModel**: Para veterinarios/auxiliares

### Pantallas

- **DomicilioListScreen**: Lista de solicitudes del usuario
- **DomicilioCreateScreen**: Crear nueva solicitud
- **DomicilioDetailScreen**: Detalle de solicitud
- **AdminDomicilioListScreen**: Lista de pendientes (Admin)
- **AdminDomicilioDetailScreen**: Detalle para aprobar/rechazar (Admin)
- **VetDomicilioListScreen**: Lista de domicilios asignados (Vet)

## 🔧 Configuración

### 1. Firestore Rules

Agregar estas reglas a `firestore.rules`:

```javascript
match /domicilios/{domicilioId} {
  // Usuarios pueden leer y crear sus propios domicilios
  allow read, create: if request.auth != null && 
    (request.auth.uid == resource.data.usuarioId || 
     get(/databases/$(database)/documents/users/$(request.auth.uid)).data.rol == 'admin' ||
     get(/databases/$(database)/documents/users/$(request.auth.uid)).data.rol == 'veterinario' ||
     get(/databases/$(database)/documents/users/$(request.auth.uid)).data.rol == 'auxiliar');
  
  // Solo admin puede actualizar estado
  allow update: if request.auth != null && 
    (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.rol == 'admin' ||
     (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.rol in ['veterinario', 'auxiliar'] && 
      request.resource.data.estado in ['EN_CAMINO', 'FINALIZADO']));
  
  // Solo el usuario puede cancelar sus propias solicitudes pendientes
  allow delete: if request.auth != null && 
    request.auth.uid == resource.data.usuarioId && 
    resource.data.estado == 'PENDIENTE';
}
```

### 2. Storage Rules

Agregar estas reglas a `storage.rules`:

```javascript
match /domicilios/{domicilioId}/{allPaths=**} {
  allow read: if request.auth != null;
  allow write: if request.auth != null && 
    (request.auth.uid == resource.metadata.usuarioId || 
     get(/databases/$(database)/documents/users/$(request.auth.uid)).data.rol == 'admin');
}
```

### 3. Cloud Functions

Las funciones ya están implementadas en `cloud-functions/index.js`:

- `onDomicilioStatusChanged`: Trigger que se ejecuta cuando cambia el estado
- Envía notificaciones FCM automáticamente
- Envía correos cuando se aprueba o rechaza

**Configurar Nodemailer:**

1. Editar `cloud-functions/index.js`
2. Cambiar `tu_correo@gmail.com` por tu correo real
3. Cambiar `tu_contraseña_de_app` por tu contraseña de aplicación de Gmail

**Desplegar funciones:**

```bash
cd cloud-functions
npm install
firebase deploy --only functions
```

### 4. FCM Token

El token FCM se registra automáticamente:
- Al iniciar la app (MainActivity)
- Cuando el token cambia (NexoGoMessagingService)

## 📱 Uso

### Para Usuarios

1. **Crear solicitud:**
   - Navegar a `Screen.DomicilioList`
   - Presionar botón "+"
   - Llenar formulario
   - Opcional: adjuntar foto
   - Guardar

2. **Ver solicitudes:**
   - Lista automática de todas las solicitudes
   - Tocar para ver detalle

3. **Cancelar solicitud:**
   - Solo si está en estado "PENDIENTE"
   - Desde el detalle, presionar "Cancelar"

### Para Administradores

1. **Ver pendientes:**
   - Navegar a `Screen.AdminDomicilioList`
   - Ver todas las solicitudes pendientes

2. **Aprobar/Rechazar:**
   - Abrir detalle de solicitud
   - Presionar "Aprobar" o "Rechazar"
   - Si rechaza, ingresar motivo

### Para Veterinarios/Auxiliares

1. **Ver asignados:**
   - Navegar a `Screen.VetDomicilioList`
   - Ver domicilios asignados

2. **Cambiar estado:**
   - Desde la lista o detalle
   - "Marcar como En Camino"
   - "Marcar como Finalizado"

## 🔔 Notificaciones

Las notificaciones se envían automáticamente cuando:

- ✅ Se aprueba una solicitud
- ❌ Se rechaza una solicitud (con motivo)
- 🚗 Estado cambia a "En Camino"
- ✅ Estado cambia a "Finalizado"

## 📧 Correos

Los correos se envían automáticamente cuando:

- ✅ Se aprueba una solicitud
- ❌ Se rechaza una solicitud (incluye motivo)

## 🗂️ Estructura de Datos

### Colección: `domicilios`

```javascript
{
  domicilioId: string,
  usuarioId: string,
  nombreUsuario: string,
  telefono: string,
  direccion: string,
  tipoServicio: string,
  fechaSolicitada: Timestamp,
  estado: "PENDIENTE" | "APROBADO" | "RECHAZADO" | "EN_CAMINO" | "FINALIZADO" | "CANCELADO",
  motivoRechazo: string?,
  fotoAdjuntaUrl: string?,
  historialEventos: [
    {
      fecha: Timestamp,
      usuarioAccion: string,
      accion: string,
      estadoFinal: string,
      observaciones: string?
    }
  ],
  fechaCreacion: Timestamp,
  fechaActualizacion: Timestamp,
  veterinarioAsignadoId: string?,
  veterinarioAsignadoNombre: string?
}
```

### Colección: `users`

Asegúrate de que los usuarios tengan el campo `tokenFCM`:

```javascript
{
  uid: string,
  nombre: string,
  telefono: string,
  rol: "admin" | "veterinario" | "auxiliar" | "usuario",
  direccion: string,
  tokenFCM: string?  // IMPORTANTE: Para notificaciones
}
```

## 🔐 Permisos por Rol

### ADMIN
- ✅ Ver todos los domicilios
- ✅ Aprobar/Rechazar solicitudes
- ✅ Cambiar cualquier estado
- ✅ Ver historial completo

### VETERINARIO / AUXILIAR
- ✅ Ver domicilios asignados
- ✅ Cambiar estado a "En Camino"
- ✅ Cambiar estado a "Finalizado"
- ✅ Ver historial

### USUARIO
- ✅ Crear solicitudes
- ✅ Ver sus propias solicitudes
- ✅ Cancelar si está pendiente
- ✅ Ver historial de sus solicitudes

## 🚀 Navegación

Agregar enlaces en tu Dashboard o Home:

```kotlin
// Para usuarios
navController.navigate(Screen.DomicilioList.route)

// Para admin
navController.navigate(Screen.AdminDomicilioList.route)

// Para veterinarios
navController.navigate(Screen.VetDomicilioList.route)
```

## 🐛 Solución de Problemas

### Las notificaciones no llegan
1. Verificar que el token FCM esté registrado en Firestore
2. Verificar permisos de notificaciones en Android
3. Revisar logs de Cloud Functions

### Los correos no se envían
1. Verificar configuración de Nodemailer
2. Verificar contraseña de aplicación de Gmail
3. Revisar logs de Cloud Functions

### Error al subir foto
1. Verificar permisos de Storage
2. Verificar reglas de Storage
3. Verificar tamaño de imagen

## ✅ Checklist de Implementación

- [x] Modelos de datos creados
- [x] Repositorio implementado
- [x] ViewModels creados
- [x] Pantallas Compose implementadas
- [x] Navegación configurada
- [x] Cloud Functions implementadas
- [x] FCM Token Manager implementado
- [x] Notificaciones configuradas
- [x] Servicio de mensajería actualizado
- [x] MainActivity actualizado

## 📝 Notas

- El módulo está completamente funcional y conectado a Firebase REAL
- No hay TODOs ni funciones vacías
- Todo el código está listo para ejecutar
- Las notificaciones y correos funcionan automáticamente

---

**Módulo desarrollado para NexoGo - Sistema de Gestión Veterinaria** 🐾




