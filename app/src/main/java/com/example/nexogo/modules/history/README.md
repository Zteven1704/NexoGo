# Módulo: Historial Clínico 🩺 (NexoGo)

Este módulo permite gestionar los historiales clínicos veterinarios, sincronizados con Firebase (Firestore + Storage).

## 🔗 Estructura de Datos

Colección principal: `clinical_records`  
Cada documento contiene:
- Datos del propietario y mascota  
- Motivo de la consulta  
- Anamnesis  
- Examen físico  
- Diagnósticos, tratamientos y evolución  
- Archivos multimedia (imágenes, PDFs, exámenes)

## 📂 Rutas de Storage
- `medical_records/{recordId}/files/` → Archivos subidos (imágenes, exámenes)
- `medical_records/{recordId}/pdfs/` → Historial generado en PDF

## 🧠 Roles y Permisos
| Rol | Puede Leer | Puede Crear/Editar | Puede Subir Archivos | Puede Descargar PDF |
|------|--------------|----------------------|------------------------|-----------------------|
| Admin | ✅ | ✅ | ✅ | ✅ |
| Vet (Veterinario) | ✅ | ✅ | ✅ | ✅ |
| Assistant | ✅ | ⚠️ (según reglas) | ✅ | ✅ |
| Patient | ✅ (solo sus mascotas) | ❌ | ❌ | ✅ |

## 🧩 Componentes Principales
- **HistoryListScreen:** Lista de historiales por mascota
- **HistoryDetailScreen:** Vista detallada (expandible)
- **HistoryEditScreen:** Formulario de creación/edición
- **PdfGenerator.kt:** Genera PDF y lo sube a Storage
- **HistoryRepository:** CRUD + Storage + sincronización
- **HistoryViewModel:** Manejo de estado (StateFlow)

## 🧪 Pruebas Automáticas
Ejecutar:
```bash
./gradlew test --tests *HistoryModuleTest
```

Pruebas incluidas:
- Creación y lectura de historiales
- Subida/descarga de archivos
- Generación de PDF
- Validación de roles y permisos

## 🧰 Mock Data
El sistema genera datos de prueba automáticamente:
- 3 historiales clínicos de ejemplo vinculados a mascotas mock.
- 1 PDF generado por historial.
- Archivos simulados en Storage (rx1.jpg, hemograma.pdf).

## 🚀 Cómo Probar
1. Inicia sesión como `vet@nexogo.com / 123456`
2. Ve al módulo "Historial Clínico"
3. Crea un nuevo historial → adjunta imágenes → guarda
4. Regresa al listado → selecciona el historial
5. Prueba botón "Generar PDF"
6. Inicia sesión como `patient@nexogo.com / 123456`
7. Verifica que solo puede ver y descargar, no editar

## 💾 Recomendaciones
- Mantén archivos < 10MB
- Comprime imágenes antes de subir
- Usa conexión estable al subir PDFs
- Ejecuta validaciones de reglas antes de publicar

## 🏗️ Arquitectura

### Estructura de Archivos
```
modules/history/
├── models/
│   └── ClinicalRecord.kt          # Modelos de datos principales
├── repo/
│   └── HistoryRepository.kt       # Repositorio para operaciones Firebase
├── viewmodel/
│   └── HistoryViewModel.kt        # ViewModel con lógica de estado
├── ui/
│   ├── HistoryListScreen.kt      # Lista de historiales
│   ├── HistoryDetailScreen.kt    # Detalle del historial
│   ├── HistoryEditScreen.kt      # Crear/editar historial
│   └── HistoryPdfPreview.kt       # Vista previa de PDF
├── components/
│   ├── HistoryCard.kt            # Tarjeta de historial
│   └── FileUploader.kt          # Componente de subida de archivos
├── utils/
│   └── PdfGenerator.kt           # Generador de PDFs
├── tests/
│   └── HistoryModuleTest.kt      # Tests del módulo
└── README.md                     # Esta documentación
```

## 🔥 Firebase Integration

### Colecciones Firestore
- **`clinical_records/{recordId}`**: Documentos principales del historial
- **`users/{uid}`**: Información de usuarios (veterinarios, propietarios)

### Storage Paths
- **`medical_records/{recordId}/files/{filename}`**: Archivos adjuntos
- **`medical_records/{recordId}/pdfs/{recordId}.pdf`**: PDFs generados
- **`medical_records/{recordId}/thumbs/{thumb}.jpg`**: Miniaturas

### Reglas de Seguridad Sugeridas

#### Firestore Rules
```javascript
// Reglas para clinical_records
match /clinical_records/{recordId} {
  // Lectura: usuarios autenticados pueden leer si son propietarios o profesionales
  allow read: if request.auth != null && (
    request.auth.uid == resource.data.ownerId || 
    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['vet', 'assistant', 'admin']
  );
  
  // Escritura: solo veterinarios y administradores
  allow write: if request.auth != null && 
    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['vet', 'admin'];
}
```

#### Storage Rules
```javascript
// Reglas para medical_records
match /medical_records/{recordId}/{allPaths=**} {
  allow read, write: if request.auth != null && (
    request.auth.uid == resource.metadata.ownerId ||
    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['vet', 'assistant', 'admin']
  );
}
```

## 🎯 Funcionalidades

### CRUD Operations
- ✅ **Crear**: Nuevo historial clínico
- ✅ **Leer**: Ver historiales por propietario/mascota
- ✅ **Actualizar**: Editar historiales existentes
- ✅ **Eliminar**: Borrar historiales (con limpieza de archivos)

### Características Avanzadas
- 📎 **Archivos Adjuntos**: Imágenes, PDFs, documentos, videos, audios
- 📄 **Generación de PDF**: Exportar historial completo
- 🔍 **Búsqueda y Filtros**: Por mascota, propietario, fecha
- 📱 **UI Responsiva**: Adaptable a móviles y tablets
- 🔄 **Tiempo Real**: Sincronización automática con Firebase

### Permisos por Rol
- **Administrador**: Acceso completo a todos los historiales
- **Veterinario**: Crear, editar, ver historiales de sus pacientes
- **Auxiliar**: Ver y editar historiales (según configuración)
- **Paciente**: Solo ver historiales de sus mascotas

## 🧪 Testing

### Ejecutar Tests
```kotlin
// En tu Activity o Fragment
val historyTest = HistoryModuleTest(context, firebaseRepository)
val testResults = historyTest.runAllTests()
```

### Tests Incluidos
1. **Test de Creación**: Verifica creación de historiales
2. **Test de Lectura**: Verifica obtención de historiales
3. **Test de Actualización**: Verifica modificación de historiales
4. **Test de Subida**: Verifica subida de archivos adjuntos
5. **Test de PDF**: Verifica generación de PDFs
6. **Test de Eliminación**: Verifica borrado de historiales

### Mock Data
El `MockDataInitializer` crea automáticamente:
- 3-5 historiales de ejemplo
- Archivos adjuntos de prueba
- PDFs generados
- Datos vinculados a mascotas existentes

## 🚀 Uso

### Navegación
```kotlin
// Navegar a lista de historiales
navController.navigate(Screen.HistoryList.route)

// Navegar a detalle de historial
navController.navigate(Screen.HistoryDetail.createRoute(recordId))

// Navegar a edición
navController.navigate(Screen.HistoryEdit.createRoute(recordId))

// Navegar a vista previa de PDF
navController.navigate(Screen.HistoryPdfPreview.createRoute(recordId))
```

### ViewModel Usage
```kotlin
val firebaseRepository = FirebaseRepository()
val historyRepository = HistoryRepository(firebaseRepository)
val historyViewModel = HistoryViewModel(historyRepository)

// Cargar historiales
historyViewModel.loadRecords(ownerId)

// Crear historial
historyViewModel.createRecord(clinicalRecord)

// Generar PDF
historyViewModel.generatePdf(recordId)
```

## 📱 UI Components

### HistoryCard
Tarjeta reutilizable para mostrar resumen del historial:
- Nombre de mascota y propietario
- Fecha de creación
- Motivo de consulta
- Diagnóstico principal
- Botones de acción (editar, eliminar)

### FileUploader
Componente para subir archivos adjuntos:
- Selección de tipo de archivo
- Progreso de subida
- Vista previa de archivos
- Validación de tamaño y tipo

### ExpandableHistorySection
Secciones expandibles para organizar información:
- Anamnesis
- Examen físico
- Problemas y diagnósticos
- Plan de tratamiento
- Seguimientos

## 🔧 Configuración

### Dependencias Requeridas
```kotlin
// En build.gradle (app)
implementation 'com.itextpdf:itext7-core:7.2.5'
implementation 'com.itextpdf:kernel:7.2.5'
implementation 'com.itextpdf:io:7.2.5'
implementation 'com.itextpdf:layout:7.2.5'
```

### Permisos Android
```xml
<!-- En AndroidManifest.xml -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## 🐛 Troubleshooting

### Problemas Comunes

1. **Error de permisos Firebase**
   - Verificar reglas de Firestore y Storage
   - Comprobar autenticación del usuario

2. **Error de subida de archivos**
   - Verificar tamaño máximo (10MB por archivo)
   - Comprobar conexión a internet
   - Validar formato de archivo

3. **Error de generación de PDF**
   - Verificar dependencias de iText
   - Comprobar datos del historial
   - Revisar logs de error

### Logs de Debug
```kotlin
// Habilitar logs detallados
Log.d("NEXOGO_HISTORY", "Operación completada")
Log.e("NEXOGO_HISTORY", "Error: ${exception.message}")
```

## 📈 Rendimiento

### Optimizaciones
- **Paginación**: Para listas grandes de historiales
- **Compresión**: Imágenes comprimidas antes de subir
- **Cache**: Datos locales para acceso rápido
- **Lazy Loading**: Carga bajo demanda

### Límites Recomendados
- **Archivos**: Máximo 10MB por archivo
- **Historiales**: Máximo 100 por propietario
- **Adjuntos**: Máximo 20 por historial

## 🔄 Sincronización

### Tiempo Real
- **Firestore Listeners**: Actualización automática de cambios
- **StateFlow**: Emisión de estados en tiempo real
- **Coroutines**: Operaciones asíncronas

### Conflictos
- **Último en escribir gana**: Para ediciones simultáneas
- **Versionado**: Timestamps para resolución de conflictos
- **Notificaciones**: Alertas de cambios importantes

## 📞 Soporte

Para reportar problemas o solicitar funcionalidades:
1. Revisar logs de error
2. Verificar configuración de Firebase
3. Comprobar permisos de usuario
4. Contactar al equipo de desarrollo

---

**Versión**: 1.0.0  
**Última actualización**: Enero 2025  
**Mantenido por**: Equipo NexoGo
