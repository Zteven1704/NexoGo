# 🐾 NexoGo - Sistema de Gestión Veterinaria

Sistema completo de gestión veterinaria desarrollado en Kotlin + Jetpack Compose con Firebase.

## 🚀 Características

- **Autenticación**: Email/Password con roles (Admin, Vet, Assistant, Patient)
- **Gestión de Perfiles**: Edición de perfil con subida de imágenes
- **Citas**: Calendario interactivo con gestión completa de citas
- **Pacientes**: CRUD de mascotas con historial médico
- **Inventario**: Gestión de productos y servicios
- **Ventas**: Registro de ventas con facturación PDF
- **Chat**: Comunicación en tiempo real entre usuarios
- **Configuración**: Ajustes del sistema y categorías

## 🏗️ Arquitectura

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Arquitectura**: MVVM + Repository
- **Navegación**: Jetpack Navigation
- **Backend**: Firebase (Auth, Firestore, Storage)
- **Coroutines**: Para operaciones asíncronas

## 📱 Módulos

### 1. **Autenticación** (`modules.auth`)
- Login/Registro con Email/Password
- Gestión de roles y aprobaciones
- Validación de permisos

### 2. **Perfil** (`modules.profile`)
- Edición de datos personales
- Subida de foto de perfil
- Gestión de configuración

### 3. **Citas** (`modules.appointments`)
- Calendario mensual interactivo
- CRUD de citas
- Estados de citas (Programada, Confirmada, Completada, Cancelada)
- Filtrado por fecha

### 4. **Pacientes** (`modules.patients`)
- Registro de mascotas
- Subida de fotos
- Búsqueda y filtrado
- Historial médico

### 5. **Historial Clínico** (`modules.history`)
- Registro detallado de consultas
- Examen físico completo
- Diagnósticos y tratamientos
- Exportación a PDF

### 6. **Inventario** (`modules.inventory`)
- Gestión de productos
- Control de stock
- Alertas de stock bajo
- Categorización

### 7. **Ventas** (`modules.sales`)
- Registro de ventas
- Facturación automática
- Integración con inventario
- Reportes financieros

### 8. **Chat** (`modules.chat`)
- Comunicación en tiempo real
- Envío de multimedia
- Notificaciones
- Historial de conversaciones

### 9. **Configuración** (`modules.config`)
- Ajustes del sistema
- Gestión de categorías
- Configuración de notificaciones

## 🔧 Configuración

### 1. **Firebase Setup**

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Crea un nuevo proyecto llamado "NexoGo"
3. Agrega una app Android con package name: `com.example.nexogo`
4. Descarga `google-services.json` y colócalo en `/app/`

### 2. **Configuración de Firebase**

#### **Authentication**
- Habilita Email/Password en Firebase Console
- Ve a Authentication > Sign-in method
- Activa "Email/Password"

#### **Firestore**
- Crea una base de datos Firestore
- Configura las reglas de seguridad (ver `firestore.rules`)

#### **Storage**
- Habilita Firebase Storage
- Configura las reglas de seguridad (ver `storage.rules`)

### 3. **Reglas de Seguridad**

#### **Firestore Rules**
```javascript
// Copia el contenido de firestore.rules a Firebase Console > Firestore > Rules
```

#### **Storage Rules**
```javascript
// Copia el contenido de storage.rules a Firebase Console > Storage > Rules
```

## 🧪 Testing

### **Usuarios de Prueba**

| Email | Contraseña | Rol |
|-------|------------|-----|
| admin@nexogo.com | 123456 | Administrador |
| vet@nexogo.com | 123456 | Veterinario |
| assistant@nexogo.com | 123456 | Asistente |
| patient@nexogo.com | 123456 | Paciente |

### **Test de Validación Firebase**

```kotlin
// Ejecutar test completo de Firebase
val test = FirebaseFullValidationTest(context)
val result = test.runFullValidation()

if (result.success) {
    Log.d("NEXOGO_TEST", "✅ Todos los tests pasaron: ${result.passedTests}/${result.totalTests}")
} else {
    Log.e("NEXOGO_TEST", "❌ Algunos tests fallaron")
}
```

### **Mock Data**

```kotlin
// Generar datos de prueba
val mockData = MockDataGenerator(FirebaseRepository())
mockData.generateAllMockData()
```

## 📱 Uso

### **1. Iniciar Sesión**
- Usa los usuarios de prueba o crea uno nuevo
- Los roles Vet/Assistant requieren aprobación del Admin

### **2. Gestión de Citas**
- Ve a "Citas" desde el menú principal
- Haz clic en "Cargar Citas" para ver datos de prueba
- Selecciona fechas en el calendario
- Crea nuevas citas con el botón "+"

### **3. Gestión de Pacientes**
- Ve a "Pacientes" desde el menú principal
- Haz clic en "Cargar Pacientes" para ver datos de prueba
- Crea nuevos pacientes con el botón "+"
- Busca pacientes con la barra de búsqueda

### **4. Chat**
- Ve a "Chat" desde el menú principal
- Inicia conversaciones con otros usuarios
- Envía mensajes de texto y multimedia

## 🔍 Logs y Debugging

### **Tags de Log**
- `NEXOGO_AUTH`: Autenticación
- `NEXOGO_PROFILE`: Perfil de usuario
- `NEXOGO_APPT`: Citas
- `NEXOGO_PATIENTS`: Pacientes
- `NEXOGO_STORAGE`: Almacenamiento
- `NEXOGO_CHAT`: Chat
- `NEXOGO_TEST`: Tests de validación

### **Verificar Conexión Firebase**
```kotlin
// En Logcat, busca estos mensajes:
// ✅ Login exitoso: [UID]
// ✅ Documento creado en Firestore: [collection]/[document]
// ✅ Archivo subido a Storage: [path]
```

## 🚨 Solución de Problemas

### **Error de Autenticación**
1. Verifica que `google-services.json` esté en `/app/`
2. Confirma que Email/Password esté habilitado en Firebase
3. Revisa los logs con tag `NEXOGO_AUTH`

### **Error de Firestore**
1. Verifica las reglas de seguridad
2. Confirma que la base de datos esté creada
3. Revisa los logs con tag `NEXOGO_FIRESTORE`

### **Error de Storage**
1. Verifica las reglas de Storage
2. Confirma que Storage esté habilitado
3. Revisa los logs con tag `NEXOGO_STORAGE`

### **App se Cierra**
1. Revisa los logs de error en Logcat
2. Verifica que todas las dependencias estén sincronizadas
3. Confirma que Firebase esté inicializado correctamente

## 📋 Checklist de Implementación

- [x] Firebase Authentication configurado
- [x] Firebase Firestore configurado
- [x] Firebase Storage configurado
- [x] Reglas de seguridad implementadas
- [x] Módulos de autenticación implementados
- [x] Módulos de perfil implementados
- [x] Módulos de citas implementados
- [x] Módulos de pacientes implementados
- [x] Test de validación implementado
- [x] Mock data implementado
- [x] Documentación completa

## 🔄 Sincronización en Tiempo Real

- **Firestore**: Listeners automáticos para actualizaciones
- **Storage**: URLs de descarga actualizadas automáticamente
- **Chat**: Mensajes en tiempo real
- **Citas**: Calendario actualizado automáticamente

## 📊 Monitoreo

### **Métricas Importantes**
- Usuarios activos
- Citas programadas
- Pacientes registrados
- Ventas realizadas
- Mensajes enviados

### **Alertas**
- Stock bajo de productos
- Citas pendientes de confirmación
- Mensajes no leídos
- Errores de sincronización

## 🚀 Próximas Mejoras

- [ ] Notificaciones push (FCM)
- [ ] Reportes avanzados
- [ ] Integración con pagos
- [ ] Modo offline
- [ ] Sincronización automática
- [ ] Auditoría de logs
- [ ] Roles más granulares
- [ ] API REST

## 📞 Soporte

Para soporte técnico o reportar bugs:
1. Revisa los logs en Logcat
2. Verifica la configuración de Firebase
3. Confirma que todas las dependencias estén actualizadas
4. Revisa la documentación de Firebase

---

**NexoGo** - Sistema de Gestión Veterinaria Completo 🐾