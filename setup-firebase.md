# 🚀 Configuración Completa de Firebase para NexoGo

## 📋 Resumen

Este script configura automáticamente Firebase Firestore y Authentication para la app NexoGo con:

- ✅ 8 usuarios de prueba con diferentes roles
- ✅ Datos mock realistas en todas las colecciones
- ✅ Reglas de seguridad configuradas
- ✅ Custom Claims para control de acceso
- ✅ Sincronización en tiempo real
- ✅ Validación automática

## 🛠️ Pasos de Configuración

### 1. Configurar Firebase Console

1. **Crear proyecto Firebase:**
   - Ve a [Firebase Console](https://console.firebase.google.com)
   - Crea un nuevo proyecto llamado "NexoGo"
   - Habilita Authentication (Email/Password y Google Sign-In)
   - Habilita Firestore Database
   - Habilita Storage

2. **Configurar Authentication:**
   - Ve a Authentication > Sign-in method
   - Habilita "Email/Password"
   - Habilita "Google" (opcional)
   - Agrega SHA-1 y SHA-256 de tu keystore

3. **Configurar Firestore:**
   - Ve a Firestore Database
   - Crea base de datos en modo de prueba (inicialmente)
   - Copia las reglas de `firestore.rules`

### 2. Configurar la App Android

1. **Descargar google-services.json:**
   - Ve a Project Settings > General
   - Descarga `google-services.json`
   - Colócalo en `app/google-services.json`

2. **Ejecutar la app:**
   - La app creará automáticamente todos los datos de prueba
   - Revisa los logs para confirmar la creación

### 3. Configurar Custom Claims (Opcional)

#### Opción A: Cloud Functions (Recomendado)

1. **Instalar Firebase CLI:**
   ```bash
   npm install -g firebase-tools
   ```

2. **Inicializar proyecto:**
   ```bash
   firebase init functions
   ```

3. **Copiar archivos:**
   - Copia `cloud-functions/index.js` a `functions/index.js`
   - Copia `cloud-functions/package.json` a `functions/package.json`

4. **Desplegar:**
   ```bash
   firebase deploy --only functions
   ```

5. **Ejecutar desde la app:**
   - Usa la función `setupTestUsersClaims` desde la app

#### Opción B: Script Manual

1. **Instalar Firebase Admin SDK:**
   ```bash
   npm install firebase-admin
   ```

2. **Crear script setup-claims.js:**
   ```javascript
   const admin = require('firebase-admin');
   
   // Inicializar con service account
   const serviceAccount = require('./path/to/serviceAccountKey.json');
   admin.initializeApp({
     credential: admin.credential.cert(serviceAccount)
   });
   
   // Configurar claims
   async function setupClaims() {
     const users = [
       { uid: 'UID_DEL_ADMIN', role: 'admin' },
       { uid: 'UID_DEL_VET', role: 'vet' },
       { uid: 'UID_DEL_ASSISTANT', role: 'assistant' },
       { uid: 'UID_DEL_PATIENT', role: 'patient' }
     ];
     
     for (const user of users) {
       const claims = {
         role: user.role,
         permissions: getPermissionsForRole(user.role)
       };
       
       await admin.auth().setCustomUserClaims(user.uid, claims);
       console.log(`Claims configurados para ${user.uid} con rol ${user.role}`);
     }
   }
   
   function getPermissionsForRole(role) {
     switch (role) {
       case 'admin': return ['read', 'write', 'delete', 'manage_users', 'manage_config'];
       case 'vet': return ['read', 'write', 'manage_patients', 'manage_appointments', 'manage_inventory', 'manage_sales', 'chat'];
       case 'assistant': return ['read', 'write', 'manage_patients', 'manage_appointments', 'manage_inventory', 'manage_sales', 'chat'];
       case 'patient': return ['read_own_data', 'write_own_data', 'chat', 'view_own_appointments'];
       default: return [];
     }
   }
   
   setupClaims().then(() => {
     console.log('Claims configurados exitosamente');
     process.exit(0);
   }).catch(error => {
     console.error('Error:', error);
     process.exit(1);
   });
   ```

3. **Ejecutar:**
   ```bash
   node setup-claims.js
   ```

## 👥 Usuarios de Prueba Creados

| Email | Password | Rol | Permisos |
|-------|----------|-----|----------|
| admin@nexogo.com | 123456 | admin | Acceso completo |
| vet@nexogo.com | 123456 | vet | Gestión de pacientes, citas, inventario |
| assistant@nexogo.com | 123456 | assistant | Gestión de pacientes, citas, inventario |
| patient@nexogo.com | 123456 | patient | Solo sus datos y chat |
| vet2@nexogo.com | 123456 | vet | Gestión de pacientes, citas, inventario |
| assistant2@nexogo.com | 123456 | assistant | Gestión de pacientes, citas, inventario |
| patient2@nexogo.com | 123456 | patient | Solo sus datos y chat |
| patient3@nexogo.com | 123456 | patient | Solo sus datos y chat |

## 📊 Datos Mock Creados

### 🐕 Pacientes (3)
- **Max** - Golden Retriever, 3 años, macho, castrado
- **Luna** - Persa, 2 años, hembra, esterilizada  
- **Rocky** - Pastor Alemán, 5 años, macho, intacto

### 📅 Citas (5)
- 3 programadas para los próximos días
- 1 completada (ayer)
- 1 cancelada

### 📦 Inventario (5)
- NexGard Spectra (antiparasitario)
- Bravecto (antiparasitario gatos)
- Vacuna Antirrábica
- Pentavalente (vacuna múltiple)
- Alimento Premium 15kg

### 💰 Ventas (5)
- Servicios médicos y productos
- Diferentes montos y tipos
- Asociadas a pacientes

### 💬 Chats (2)
- Conversaciones entre pacientes y veterinarios
- Mensajes con texto e imágenes
- Timestamps realistas

### ⚙️ Configuración (1)
- Idioma: español
- Teléfonos de contacto
- Logo y datos de la clínica

## 🔒 Reglas de Seguridad

Las reglas de Firestore están configuradas para:

- **Admin**: Acceso completo a todas las colecciones
- **Vet/Assistant**: Acceso a pacientes, citas, inventario, ventas, chat
- **Patient**: Solo acceso a sus propios datos y chat

## 🧪 Testing

### Verificar Conexión
1. Ejecuta la app
2. Revisa los logs para confirmar la creación de datos
3. Verifica en Firebase Console que aparecen las colecciones

### Probar Roles
1. Inicia sesión con diferentes usuarios
2. Verifica que cada rol ve solo lo que debe ver
3. Prueba la sincronización en tiempo real

### Probar Funcionalidades
1. **Chat**: Envía mensajes entre roles
2. **Citas**: Crea, edita, cancela citas
3. **Pacientes**: Gestiona información de mascotas
4. **Inventario**: Agrega, edita productos
5. **Ventas**: Registra ventas y servicios

## 📱 Logs de Verificación

La app generará logs detallados:

```
🌱 Iniciando seeding de datos de prueba...
👥 Creando usuarios de prueba...
✅ Usuario creado: admin@nexogo.com (admin)
✅ Usuario creado: vet@nexogo.com (vet)
...
🐕 Creando pacientes...
✅ 3 pacientes creados
📅 Creando citas...
✅ 5 citas creadas
...
🔄 Verificando sincronización...
👥 Usuarios en Firestore: 8
🐕 Pacientes en Firestore: 3
📅 Citas en Firestore: 5
...
✅ Seeding completado exitosamente!
```

## 🚨 Solución de Problemas

### Error: "Firebase no está inicializado"
- Verifica que `google-services.json` esté en la ubicación correcta
- Revisa que el package name coincida

### Error: "Permission denied"
- Verifica las reglas de Firestore
- Asegúrate de que los usuarios estén autenticados

### Error: "Custom claims not found"
- Ejecuta el script de configuración de claims
- Verifica que el usuario tenga el rol correcto

## 🎉 ¡Listo!

NexoGo está completamente configurado con:

- ✅ 8 usuarios de prueba
- ✅ Datos mock realistas
- ✅ Reglas de seguridad
- ✅ Sincronización en tiempo real
- ✅ Chat funcional
- ✅ Gestión completa de citas, pacientes, inventario y ventas

¡La app está lista para usar y probar! 🚀

