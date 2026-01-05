# 📱 Configuración de Emulador para NexoGo

## ✅ **Requisitos del Emulador**

Para que Firebase funcione correctamente, el emulador debe tener:

### **1. Google Play Services**
- ✅ **Imagen con Google Play Store** (no AOSP)
- ✅ **Google Play Services** instalado y actualizado
- ✅ **Google Play Store** funcional

### **2. Especificaciones Recomendadas**
- **API Level**: 28 o superior (Android 9.0+)
- **RAM**: 4GB o más
- **Almacenamiento**: 8GB o más
- **Arquitectura**: x86_64 (recomendado para mejor rendimiento)

### **3. Configuración del Emulador**

#### **Crear AVD con Google Play Services:**
1. Abrir **Android Studio**
2. Ir a **Tools** → **AVD Manager**
3. Hacer clic en **Create Virtual Device**
4. Seleccionar **Phone** → **Pixel 4** (o similar)
5. **IMPORTANTE**: Seleccionar una imagen con **Google Play Store**
   - ✅ **API 34** (Android 14) con Google Play
   - ✅ **API 33** (Android 13) con Google Play
   - ✅ **API 32** (Android 12) con Google Play
6. Configurar **Advanced Settings**:
   - **RAM**: 4096 MB
   - **Internal Storage**: 8000 MB
   - **SD Card**: 1000 MB
7. Hacer clic en **Finish**

#### **Configuración Adicional:**
1. **Abrir el emulador**
2. **Configurar Google Play Store**:
   - Iniciar sesión con cuenta de Google
   - Actualizar Google Play Services
3. **Verificar conexión a internet**
4. **Instalar la app NexoGo**

### **4. Verificación de Google Play Services**

#### **Comprobar en el emulador:**
1. Abrir **Settings** → **Apps** → **Google Play Services**
2. Verificar que esté **instalado y actualizado**
3. Verificar **permisos** necesarios

#### **Comprobar en la app:**
```kotlin
// En Logcat, buscar:
"🔥 Firebase inicializado correctamente"
"✅ Firestore: Documento creado con ID: [ID]"
"✅ Auth: Servicio de autenticación disponible"
"✅ Storage: Referencia al storage obtenida correctamente"
```

### **5. Solución de Problemas Comunes**

#### **Error: "Google Play Services not available"**
- ✅ Usar imagen con Google Play Store
- ✅ Actualizar Google Play Services
- ✅ Reiniciar el emulador

#### **Error: "Firebase connection failed"**
- ✅ Verificar conexión a internet
- ✅ Verificar google-services.json
- ✅ Verificar applicationId coincidente

#### **Error: "Authentication failed"**
- ✅ Verificar Google Play Services
- ✅ Verificar configuración de Firebase Console
- ✅ Verificar SHA-1 fingerprint

### **6. Configuración de Red**

#### **Para desarrollo local:**
- ✅ **Conexión a internet** estable
- ✅ **Firewall** configurado para permitir conexiones Firebase
- ✅ **Proxy** configurado si es necesario

#### **Para testing:**
- ✅ **Red WiFi** estable
- ✅ **Datos móviles** disponibles
- ✅ **VPN** desactivado si causa problemas

### **7. Verificación Final**

#### **Logs esperados en Logcat:**
```
🔥 Firebase inicializado correctamente
📊 Probando conexión con Firestore...
✅ Firestore: Documento creado con ID: [ID]
🔐 Probando conexión con Firebase Auth...
✅ Auth: Servicio de autenticación disponible
💾 Probando conexión con Firebase Storage...
✅ Storage: Referencia al storage obtenida correctamente
✅ Test de conexión Firebase completado exitosamente
```

#### **Verificación en Firebase Console:**
1. Ir a **Firebase Console** → **Firestore Database**
2. Verificar que aparezca la colección **"test"**
3. Verificar que el documento tenga **"status": "connected"**

### **8. Configuración Recomendada**

#### **Para Desarrollo:**
- **Emulador**: Pixel 4 con API 34 + Google Play
- **RAM**: 4GB
- **Almacenamiento**: 8GB
- **Red**: WiFi estable

#### **Para Testing:**
- **Emulador**: Pixel 5 con API 33 + Google Play
- **RAM**: 6GB
- **Almacenamiento**: 12GB
- **Red**: Conexión estable

### **9. Comandos Útiles**

#### **Verificar Google Play Services:**
```bash
# En el emulador, abrir terminal
adb shell pm list packages | grep google
```

#### **Verificar Firebase:**
```bash
# En Logcat
adb logcat | grep FirebaseConnectionTest
```

#### **Limpiar cache:**
```bash
# Limpiar cache del emulador
adb shell pm clear com.google.android.gms
```

## 🎯 **Resultado Esperado**

Con esta configuración, NexoGo debería:
- ✅ **Conectarse a Firebase** sin errores
- ✅ **Autenticar usuarios** correctamente
- ✅ **Sincronizar datos** en tiempo real
- ✅ **Subir archivos** a Storage
- ✅ **Recibir notificaciones** push

## 📞 **Soporte**

Si encuentras problemas:
1. **Verificar logs** en Logcat
2. **Revisar configuración** del emulador
3. **Verificar Google Play Services**
4. **Consultar documentación** de Firebase

**¡NexoGo está listo para funcionar con Firebase!** 🔥

