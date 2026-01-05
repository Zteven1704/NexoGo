# 🚀 Guía de Configuración Firebase para NexoGo

## 📋 Resumen

Esta guía te ayudará a configurar Firebase Authentication y Firestore para la app NexoGo con todos los datos de prueba necesarios.

## 🛠️ Pasos de Configuración

### 1. Configurar Firebase Console

#### 1.1 Crear Proyecto Firebase
1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Haz clic en "Crear un proyecto"
3. Nombre del proyecto: `NexoGo`
4. Habilita Google Analytics (opcional)
5. Selecciona la cuenta de Google Analytics

#### 1.2 Configurar Authentication
1. En el menú lateral, ve a "Authentication"
2. Haz clic en "Comenzar"
3. Ve a la pestaña "Sign-in method"
4. Habilita "Correo electrónico/contraseña"
5. Habilita "Google" (opcional)

#### 1.3 Configurar Firestore Database
1. En el menú lateral, ve a "Firestore Database"
2. Haz clic en "Crear base de datos"
3. Selecciona "Comenzar en modo de prueba" (inicialmente)
4. Selecciona una ubicación (preferiblemente cercana a tu ubicación)

#### 1.4 Configurar Storage
1. En el menú lateral, ve a "Storage"
2. Haz clic en "Comenzar"
3. Acepta las reglas de seguridad por defecto
4. Selecciona la misma ubicación que Firestore

### 2. Configurar la App Android

#### 2.1 Descargar google-services.json
1. En Firebase Console, ve a "Configuración del proyecto" (ícono de engranaje)
2. En la pestaña "General", busca "Tus aplicaciones"
3. Haz clic en "Agregar app" y selecciona Android
4. **Package name**: `com.example.nexogo`
5. **App nickname**: NexoGo Android
6. Descarga `google-services.json`
7. Coloca el archivo en `app/google-services.json`

#### 2.2 Configurar SHA-1 (Opcional para Google Sign-In)
1. En Android Studio, abre la terminal
2. Ejecuta: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
3. Copia el SHA-1 fingerprint
4. En Firebase Console, ve a "Configuración del proyecto" > "Tus aplicaciones" > "NexoGo Android"
5. Agrega el SHA-1 fingerprint

### 3. Crear Usuarios de Prueba

#### 3.1 Crear usuarios en Authentication
1. En Firebase Console, ve a "Authentication" > "Users"
2. Haz clic en "Agregar usuario" y crea los siguientes usuarios:

| Email | Password | UID (se genera automáticamente) |
|-------|----------|----------------------------------|
| admin@nexogo.com | 123456 | (copiar después de crear) |
| vet@nexogo.com | 123456 | (copiar después de crear) |
| assistant@nexogo.com | 123456 | (copiar después de crear) |
| patient@nexogo.com | 123456 | (copiar después de crear) |

#### 3.2 Configurar Custom Claims (Opcional)
Para cada usuario creado:
1. Haz clic en el UID del usuario
2. Ve a la pestaña "Custom Claims"
3. Agrega el JSON correspondiente:

**Admin:**
```json
{
  "role": "admin",
  "permissions": ["read", "write", "delete", "manage_users", "manage_config"]
}
```

**Veterinario:**
```json
{
  "role": "vet",
  "permissions": ["read", "write", "manage_patients", "manage_appointments", "manage_inventory", "manage_sales", "chat"]
}
```

**Asistente:**
```json
{
  "role": "assistant",
  "permissions": ["read", "write", "manage_patients", "manage_appointments", "manage_inventory", "manage_sales", "chat"]
}
```

**Paciente:**
```json
{
  "role": "patient",
  "permissions": ["read_own_data", "write_own_data", "chat", "view_own_appointments"]
}
```

### 4. Crear Datos Mock en Firestore

#### 4.1 Colección `users`
Crea documentos con los UIDs de los usuarios:

**admin@nexogo.com:**
```json
{
  "uid": "UID_DEL_ADMIN",
  "name": "Dr. Admin",
  "email": "admin@nexogo.com",
  "role": "admin",
  "phone": "555-0001",
  "isApproved": true,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**vet@nexogo.com:**
```json
{
  "uid": "UID_DEL_VET",
  "name": "Dr. María González",
  "email": "vet@nexogo.com",
  "role": "vet",
  "phone": "555-0002",
  "isApproved": true,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**assistant@nexogo.com:**
```json
{
  "uid": "UID_DEL_ASSISTANT",
  "name": "Ana López",
  "email": "assistant@nexogo.com",
  "role": "assistant",
  "phone": "555-0003",
  "isApproved": true,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**patient@nexogo.com:**
```json
{
  "uid": "UID_DEL_PATIENT",
  "name": "Carlos Ruiz",
  "email": "patient@nexogo.com",
  "role": "patient",
  "phone": "555-0004",
  "isApproved": true,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### 4.2 Colección `patients`
Crea los siguientes documentos:

**pet_001:**
```json
{
  "patientId": "pet_001",
  "ownerId": "UID_DEL_PATIENT",
  "name": "Max",
  "species": "Perro",
  "breed": "Golden Retriever",
  "age": 3,
  "sex": "Macho",
  "weight": 25.5,
  "reproductiveStatus": "Castrado",
  "vaccinationHistory": [
    {
      "vaccine": "Rabia",
      "date": "2024-01-15",
      "nextDue": "2025-01-15"
    },
    {
      "vaccine": "Pentavalente",
      "date": "2024-02-10",
      "nextDue": "2025-02-10"
    }
  ],
  "desparasitacionHistory": [
    {
      "product": "NexGard",
      "date": "2024-03-01",
      "nextDue": "2024-04-01"
    }
  ],
  "medicalHistory": [
    {
      "date": "2024-01-15",
      "diagnosis": "Vacunación rutinaria",
      "treatment": "Vacuna antirrábica"
    }
  ],
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**pet_002:**
```json
{
  "patientId": "pet_002",
  "ownerId": "UID_DEL_PATIENT",
  "name": "Luna",
  "species": "Gato",
  "breed": "Persa",
  "age": 2,
  "sex": "Hembra",
  "weight": 4.2,
  "reproductiveStatus": "Esterilizada",
  "vaccinationHistory": [
    {
      "vaccine": "Trivalente",
      "date": "2024-01-20",
      "nextDue": "2025-01-20"
    }
  ],
  "desparasitacionHistory": [
    {
      "product": "Bravecto",
      "date": "2024-03-05",
      "nextDue": "2024-06-05"
    }
  ],
  "medicalHistory": [
    {
      "date": "2024-01-20",
      "diagnosis": "Vacunación anual",
      "treatment": "Trivalente felina"
    }
  ],
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### 4.3 Colección `appointments`
Crea los siguientes documentos:

**apt_001:**
```json
{
  "appointmentId": "apt_001",
  "patientId": "pet_001",
  "vetId": "UID_DEL_VET",
  "date": "2024-12-20T10:00:00Z",
  "status": "SCHEDULED",
  "notes": "Consulta de rutina - vacunación anual",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**apt_002:**
```json
{
  "appointmentId": "apt_002",
  "patientId": "pet_002",
  "vetId": "UID_DEL_VET",
  "date": "2024-12-19T14:00:00Z",
  "status": "COMPLETED",
  "notes": "Revisión post-esterilización",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### 4.4 Colección `inventory`
Crea los siguientes documentos:

**prod_001:**
```json
{
  "productId": "prod_001",
  "name": "NexGard Spectra",
  "description": "Antiparasitario para perros - 3 comprimidos",
  "quantity": 15,
  "unitPrice": 45.99,
  "totalPrice": 689.85,
  "imageUrl": "https://example.com/nexgard.jpg",
  "category": "Antiparasitarios",
  "minStock": 5,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**prod_002:**
```json
{
  "productId": "prod_002",
  "name": "Vacuna Antirrábica",
  "description": "Vacuna antirrábica para perros y gatos",
  "quantity": 25,
  "unitPrice": 15.00,
  "totalPrice": 375.00,
  "imageUrl": "https://example.com/rabia.jpg",
  "category": "Vacunas",
  "minStock": 10,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### 4.5 Colección `sales`
Crea los siguientes documentos:

**sale_001:**
```json
{
  "saleId": "sale_001",
  "patientId": "pet_001",
  "serviceType": "medical",
  "amount": 45.99,
  "date": "2024-12-18T10:00:00Z",
  "notes": "Venta de NexGard Spectra",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### 4.6 Colección `chat`
Crea los siguientes documentos:

**chat_001:**
```json
{
  "chatId": "chat_001",
  "participants": ["UID_DEL_PATIENT", "UID_DEL_VET"],
  "messages": [
    {
      "senderUid": "UID_DEL_PATIENT",
      "message": "Hola doctor, mi perro Max no ha comido desde ayer",
      "timestamp": "2024-12-18T09:00:00Z",
      "attachments": []
    },
    {
      "senderUid": "UID_DEL_VET",
      "message": "Hola Carlos, es importante que traigas a Max para una revisión. ¿Tiene otros síntomas?",
      "timestamp": "2024-12-18T09:30:00Z",
      "attachments": []
    }
  ],
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### 4.7 Colección `configuration`
Crea el siguiente documento:

**config_001:**
```json
{
  "configId": "config_001",
  "language": "es",
  "contactPhone": "+52 555 123 4567",
  "contactWhatsApp": "+52 555 123 4567",
  "logoUrl": "https://example.com/nexogo_logo.png",
  "clinicName": "NexoGo Veterinaria",
  "address": "Av. Principal 123, Ciudad, CP 12345",
  "businessHours": "Lunes a Viernes: 8:00 - 18:00, Sábados: 8:00 - 14:00",
  "emergencyPhone": "+52 555 999 8888",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

### 5. Configurar Reglas de Seguridad

#### 5.1 Reglas de Firestore
En Firebase Console, ve a "Firestore Database" > "Reglas" y reemplaza con:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Función para verificar si el usuario está autenticado
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Función para verificar el rol del usuario
    function hasRole(role) {
      return isAuthenticated() && 
             exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == role;
    }
    
    // Función para verificar si es admin
    function isAdmin() {
      return hasRole('admin');
    }
    
    // Función para verificar si es veterinario o asistente
    function isVetOrAssistant() {
      return hasRole('vet') || hasRole('assistant');
    }
    
    // Función para verificar si es paciente
    function isPatient() {
      return hasRole('patient');
    }
    
    // Función para verificar si el usuario es el propietario del recurso
    function isOwner(resource) {
      return isAuthenticated() && resource.data.ownerId == request.auth.uid;
    }
    
    // Reglas para la colección de usuarios
    match /users/{userId} {
      allow read: if isAuthenticated() && (userId == request.auth.uid || isAdmin());
      allow write: if isAuthenticated() && userId == request.auth.uid;
      allow create: if isAuthenticated() && userId == request.auth.uid;
    }
    
    // Reglas para la colección de pacientes
    match /patients/{patientId} {
      allow read: if isAuthenticated() && (isVetOrAssistant() || isAdmin() || isOwner(resource));
      allow write: if isAuthenticated() && (isVetOrAssistant() || isAdmin());
      allow create: if isAuthenticated() && (isVetOrAssistant() || isAdmin());
    }
    
    // Reglas para la colección de citas
    match /appointments/{appointmentId} {
      allow read: if isAuthenticated() && (isVetOrAssistant() || isAdmin() || isOwner(resource));
      allow write: if isAuthenticated() && (isVetOrAssistant() || isAdmin());
      allow create: if isAuthenticated() && (isVetOrAssistant() || isAdmin());
    }
    
    // Reglas para la colección de inventario
    match /inventory/{productId} {
      allow read, write: if isAuthenticated() && (isVetOrAssistant() || isAdmin());
    }
    
    // Reglas para la colección de ventas
    match /sales/{saleId} {
      allow read: if isAuthenticated() && (isVetOrAssistant() || isAdmin() || isOwner(resource));
      allow write: if isAuthenticated() && (isVetOrAssistant() || isAdmin());
      allow create: if isAuthenticated() && (isVetOrAssistant() || isAdmin());
    }
    
    // Reglas para la colección de chat
    match /chat/{chatId} {
      allow read, write: if isAuthenticated() && 
        (isAdmin() || request.auth.uid in resource.data.participants);
      allow create: if isAuthenticated();
    }
    
    // Reglas para la colección de configuración
    match /configuration/{configId} {
      allow read, write: if isAuthenticated() && isAdmin();
    }
  }
}
```

### 6. Verificar Configuración

#### 6.1 Probar la App
1. Compila y ejecuta la app en un dispositivo o emulador
2. Intenta iniciar sesión con los usuarios de prueba
3. Verifica que cada rol ve solo lo que debe ver

#### 6.2 Verificar en Firebase Console
1. Ve a "Authentication" y verifica que los usuarios estén creados
2. Ve a "Firestore Database" y verifica que las colecciones tengan datos
3. Ve a "Storage" y verifica que esté configurado

## 🎉 ¡Listo!

Tu configuración de Firebase para NexoGo está completa con:

- ✅ 4 usuarios de prueba con diferentes roles
- ✅ Datos mock realistas en todas las colecciones
- ✅ Reglas de seguridad configuradas
- ✅ Sincronización en tiempo real
- ✅ Chat funcional
- ✅ Gestión completa de citas, pacientes, inventario y ventas

## 📱 Testing

La app está lista para probar con:
- ✅ Login con diferentes roles
- ✅ Navegación según permisos
- ✅ Datos mock realistas
- ✅ Sincronización en tiempo real
- ✅ Chat funcional
- ✅ Gestión de citas
- ✅ Inventario y ventas

¡NexoGo está listo para usar! 🚀

