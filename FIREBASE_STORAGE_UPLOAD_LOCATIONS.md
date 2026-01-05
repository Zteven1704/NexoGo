# 🔍 Ubicaciones de Carga a Firebase Storage - NexoGo

## 📊 **Resumen de Archivos que Suben a Firebase Storage**

He identificado **todos los lugares** donde se suben archivos a Firebase Storage en el proyecto NexoGo. Aquí está el análisis completo:

---

## 🗂️ **1. FirebaseFullTestActivity.kt**

### **Línea 220-225:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/FirebaseFullTestActivity.kt
// Líneas: 220-225

val uploadTask = storageRef.putBytes(testContent.toByteArray())

uploadTask.addOnSuccessListener { taskSnapshot ->
    // Obtener URL de descarga
    storageRef.downloadUrl.addOnSuccessListener { uri ->
        Log.d(TAG, "✅ Storage: Archivo subido correctamente: $uri")
```

### **Línea 553-558:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/FirebaseFullTestActivity.kt
// Líneas: 553-558

val uploadTask = storageRef.putBytes(testContent.toByteArray())

uploadTask.addOnSuccessListener { taskSnapshot ->
    // Obtener URL de descarga
    storageRef.downloadUrl.addOnSuccessListener { uri ->
        Log.d("FirebaseFullTest", "✅ Storage: Archivo subido correctamente: $uri")
```

**📝 Nota:** Estas son **pruebas de conexión** que suben archivos de texto de prueba.

---

## 🗂️ **2. FirebaseRepository.kt**

### **Línea 352:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/repository/FirebaseRepository.kt
// Línea: 352

suspend fun uploadFile(fileUri: Uri, path: String): Result<String> {
    return try {
        val storageRef = storage.reference.child(path)
        val uploadTask = storageRef.putFile(fileUri).await()
        val downloadUrl = storageRef.downloadUrl.await()
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**📝 Nota:** Función **genérica** para subir cualquier archivo.

---

## 🗂️ **3. FirebaseStorageManager.kt**

### **Línea 25 - Imagen de Perfil:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/firebase/FirebaseStorageManager.kt
// Línea: 25

suspend fun uploadUserProfileImage(userId: String, imageUri: Uri): Result<String> {
    return try {
        val fileName = "profile_${userId}_${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("users/$userId/profile/$fileName")
        
        val uploadTask = ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await()
        
        Log.d("StorageManager", "✅ Imagen de perfil subida: $downloadUrl")
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### **Línea 44 - Archivo de Prueba:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/firebase/FirebaseStorageManager.kt
// Línea: 44

suspend fun uploadTestFile(content: String): Result<String> {
    return try {
        val fileName = "test_${System.currentTimeMillis()}.txt"
        val ref = storage.reference.child("test/$fileName")
        
        val uploadTask = ref.putBytes(content.toByteArray()).await()
        val downloadUrl = ref.downloadUrl.await()
        
        Log.d("StorageManager", "✅ Archivo de prueba subido: $downloadUrl")
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### **Línea 63 - Imagen de Producto:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/firebase/FirebaseStorageManager.kt
// Línea: 63

suspend fun uploadProductImage(productId: String, imageUri: Uri): Result<String> {
    return try {
        val fileName = "product_${productId}_${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("products/$productId/images/$fileName")
        
        val uploadTask = ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await()
        
        Log.d("StorageManager", "✅ Imagen de producto subida: $downloadUrl")
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### **Línea 83 - Archivo Médico:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/firebase/FirebaseStorageManager.kt
// Línea: 83

suspend fun uploadMedicalFile(recordId: String, fileUri: Uri, fileName: String): Result<String> {
    return try {
        val fileExtension = fileName.substringAfterLast(".", "")
        val newFileName = "medical_${recordId}_${UUID.randomUUID()}.$fileExtension"
        val ref = storage.reference.child("medical_records/$recordId/files/$newFileName")
        
        val uploadTask = ref.putFile(fileUri).await()
        val downloadUrl = ref.downloadUrl.await()
        
        Log.d("StorageManager", "✅ Archivo médico subido: $downloadUrl")
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## 🗂️ **4. FirebaseStorageRepository.kt**

### **Línea 29 - Imagen de Perfil:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/repository/FirebaseStorageRepository.kt
// Línea: 29

suspend fun uploadUserProfileImage(userId: String, imageUri: Uri): Result<String> {
    return try {
        val fileName = "profile_${userId}_${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("users/$userId/profile/$fileName")
        
        val uploadTask = ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await()
        
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### **Línea 46 - Imagen de Producto:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/repository/FirebaseStorageRepository.kt
// Línea: 46

suspend fun uploadProductImage(productId: String, imageUri: Uri): Result<String> {
    return try {
        val fileName = "product_${productId}_${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("products/$productId/images/$fileName")
        
        val uploadTask = ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await()
        
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### **Línea 63 - Imagen de Mascota:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/repository/FirebaseStorageRepository.kt
// Línea: 63

suspend fun uploadPetImage(petId: String, imageUri: Uri): Result<String> {
    return try {
        val fileName = "pet_${petId}_${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("pets/$petId/images/$fileName")
        
        val uploadTask = ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await()
        
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### **Línea 81 - Archivo Médico:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/repository/FirebaseStorageRepository.kt
// Línea: 81

suspend fun uploadMedicalFile(recordId: String, fileUri: Uri, fileName: String): Result<String> {
    return try {
        val fileExtension = fileName.substringAfterLast(".", "")
        val newFileName = "medical_${recordId}_${UUID.randomUUID()}.$fileExtension"
        val ref = storage.reference.child("medical_records/$recordId/files/$newFileName")
        
        val uploadTask = ref.putFile(fileUri).await()
        val downloadUrl = ref.downloadUrl.await()
        
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### **Línea 99 - Archivo de Chat:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/repository/FirebaseStorageRepository.kt
// Línea: 99

suspend fun uploadChatFile(chatId: String, fileUri: Uri, fileName: String, fileType: String): Result<String> {
    return try {
        val fileExtension = fileName.substringAfterLast(".", "")
        val newFileName = "chat_${chatId}_${UUID.randomUUID()}.$fileExtension"
        val ref = storage.reference.child("chat/$chatId/files/$fileType/$newFileName")
        
        val uploadTask = ref.putFile(fileUri).await()
        val downloadUrl = ref.downloadUrl.await()
        
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### **Línea 116 - PDF de Factura:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/repository/FirebaseStorageRepository.kt
// Línea: 116

suspend fun uploadInvoicePDF(saleId: String, pdfUri: Uri): Result<String> {
    return try {
        val fileName = "invoice_${saleId}_${UUID.randomUUID()}.pdf"
        val ref = storage.reference.child("invoices/$saleId/$fileName")
        
        val uploadTask = ref.putFile(pdfUri).await()
        val downloadUrl = ref.downloadUrl.await()
        
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### **Línea 133 - Imagen de Rayos X:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/repository/FirebaseStorageRepository.kt
// Línea: 133

suspend fun uploadXRayImage(recordId: String, imageUri: Uri): Result<String> {
    return try {
        val fileName = "xray_${recordId}_${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("medical_records/$recordId/xrays/$fileName")
        
        val uploadTask = ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await()
        
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### **Línea 150 - Recibo de Pago:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/repository/FirebaseStorageRepository.kt
// Línea: 150

suspend fun uploadPaymentReceipt(paymentId: String, imageUri: Uri): Result<String> {
    return try {
        val fileName = "receipt_${paymentId}_${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("payments/$paymentId/receipts/$fileName")
        
        val uploadTask = ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await()
        
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## 🗂️ **5. ChatRepository.kt**

### **Línea 186:**
```kotlin
// Archivo: app/src/main/java/com/example/nexogo/repository/ChatRepository.kt
// Línea: 186

private suspend fun uploadFileToStorage(uri: Uri, fileName: String): String? {
    return try {
        val fileRef = storage.reference
            .child(STORAGE_CHAT_FILES)
            .child("${System.currentTimeMillis()}_$fileName")
        
        val uploadTask = fileRef.putFile(uri).await()
        fileRef.downloadUrl.await().toString()
    } catch (e: Exception) {
        null
    }
}
```

---

## 🔍 **Análisis del Error 404**

### **Posibles Causas del Error 404:**

1. **❌ Configuración de Storage Rules**
   - Las reglas de Firebase Storage pueden estar bloqueando las subidas
   - Verificar en Firebase Console → Storage → Rules

2. **❌ Autenticación Requerida**
   - El usuario debe estar autenticado para subir archivos
   - Verificar que `FirebaseAuth.getInstance().currentUser` no sea null

3. **❌ Permisos de Internet**
   - Verificar que la app tenga permisos de internet
   - Verificar conexión a internet estable

4. **❌ Configuración del Bucket**
   - Verificar que el bucket de Storage esté configurado correctamente
   - Verificar que `google-services.json` tenga la configuración correcta

5. **❌ Referencias de Storage**
   - Verificar que `FirebaseStorage.getInstance()` esté inicializado
   - Verificar que las rutas de Storage sean válidas

### **Rutas de Storage Identificadas:**

- `test/` - Archivos de prueba
- `users/{userId}/profile/` - Imágenes de perfil
- `products/{productId}/images/` - Imágenes de productos
- `pets/{petId}/images/` - Imágenes de mascotas
- `medical_records/{recordId}/files/` - Archivos médicos
- `medical_records/{recordId}/xrays/` - Rayos X
- `chat/{chatId}/files/{fileType}/` - Archivos de chat
- `invoices/{saleId}/` - PDFs de facturas
- `payments/{paymentId}/receipts/` - Recibos de pago

---

## 🛠️ **Recomendaciones para Corregir el Error 404**

### **1. Verificar Storage Rules:**
```javascript
// En Firebase Console → Storage → Rules
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### **2. Verificar Autenticación:**
```kotlin
// Antes de subir archivos, verificar autenticación
val user = FirebaseAuth.getInstance().currentUser
if (user == null) {
    Log.e("Storage", "❌ Usuario no autenticado")
    return
}
```

### **3. Verificar Configuración:**
```kotlin
// Verificar que Storage esté inicializado
val storage = FirebaseStorage.getInstance()
Log.d("Storage", "✅ Storage inicializado: ${storage != null}")
```

### **4. Agregar Logs de Debug:**
```kotlin
// Agregar logs detallados para debugging
Log.d("Storage", "🔍 Subiendo archivo a: $path")
Log.d("Storage", "🔍 URI del archivo: $fileUri")
Log.d("Storage", "🔍 Usuario autenticado: ${FirebaseAuth.getInstance().currentUser != null}")
```

---

## 📋 **Checklist de Verificación**

### **Configuración Básica:**
- [ ] Firebase Storage habilitado en Firebase Console
- [ ] Storage Rules configuradas correctamente
- [ ] Usuario autenticado antes de subir archivos
- [ ] Permisos de internet en AndroidManifest.xml
- [ ] google-services.json configurado correctamente

### **Debugging:**
- [ ] Logs de autenticación antes de subir
- [ ] Logs de la ruta de Storage
- [ ] Logs de errores detallados
- [ ] Verificar conexión a internet
- [ ] Verificar que el archivo existe y es accesible

---

## 🎯 **Conclusión**

**Se han identificado 16 ubicaciones** donde se suben archivos a Firebase Storage en el proyecto NexoGo. El error 404 puede estar relacionado con:

1. **Configuración de Storage Rules** (más probable)
2. **Falta de autenticación** del usuario
3. **Configuración incorrecta** del bucket de Storage
4. **Problemas de permisos** o conectividad

**Recomendación:** Revisar primero las Storage Rules en Firebase Console y verificar que el usuario esté autenticado antes de intentar subir archivos.

