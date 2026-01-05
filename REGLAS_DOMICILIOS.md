# 🔧 REGLAS DE FIRESTORE Y STORAGE PARA DOMICILIOS

## 🚨 PROBLEMA
Error: `failed _precondition` al crear o gestionar domicilios.

## ✅ SOLUCIÓN

### 1. Actualizar Reglas de Firestore

Ve a **Firebase Console** > **Firestore Database** > **Rules** y reemplaza las reglas de `domicilios` con:

```javascript
// Reglas para domicilios
match /domicilios/{domicilioId} {
  // Crear: cualquier usuario autenticado puede crear domicilios
  allow create: if request.auth != null;
  
  // Leer: el usuario propietario, admin, veterinario o auxiliar
  allow read: if request.auth != null && (
    resource.data.usuarioId == request.auth.uid ||
    exists(/databases/$(database)/documents/usuarios/$(request.auth.uid)) &&
    get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.role in ['ADMIN', 'VET', 'VET_ASSISTANT']
  );
  
  // Actualizar: admin puede aprobar/rechazar, vet/auxiliar puede cambiar estado
  allow update: if request.auth != null && (
    // Admin puede hacer cualquier cambio
    (exists(/databases/$(database)/documents/usuarios/$(request.auth.uid)) &&
     get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.role == 'ADMIN') ||
    // Vet/Auxiliar solo puede cambiar a EN_CAMINO o FINALIZADO
    (exists(/databases/$(database)/documents/usuarios/$(request.auth.uid)) &&
     get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.role in ['VET', 'VET_ASSISTANT'] &&
     request.resource.data.estado in ['EN_CAMINO', 'FINALIZADO']) ||
    // Usuario puede cancelar su propia solicitud pendiente
    (resource.data.usuarioId == request.auth.uid &&
     resource.data.estado == 'PENDIENTE' &&
     request.resource.data.estado == 'CANCELADO')
  );
  
  // Eliminar: solo admin o el usuario propietario si está pendiente
  allow delete: if request.auth != null && (
    (exists(/databases/$(database)/documents/usuarios/$(request.auth.uid)) &&
     get(/databases/$(database)/documents/usuarios/$(request.auth.uid)).data.role == 'ADMIN') ||
    (resource.data.usuarioId == request.auth.uid && resource.data.estado == 'PENDIENTE')
  );
}
```

### 2. Actualizar Reglas de Storage

Ve a **Firebase Console** > **Storage** > **Rules** y agrega:

```javascript
// Reglas para fotos de domicilios
match /domicilios/{domicilioId}/{fileName} {
  // Lectura: el usuario propietario del domicilio, admin, veterinario o auxiliar
  allow read: if request.auth != null && (
    // Verificar que el usuario tenga acceso al domicilio
    exists(/databases/(default)/documents/domicilios/$(domicilioId)) &&
    (
      get(/databases/(default)/documents/domicilios/$(domicilioId)).data.usuarioId == request.auth.uid ||
      (exists(/databases/(default)/documents/usuarios/$(request.auth.uid)) &&
       get(/databases/(default)/documents/usuarios/$(request.auth.uid)).data.role in ['ADMIN', 'VET', 'VET_ASSISTANT'])
    )
  );
  
  // Escritura: solo al crear (subir foto al crear domicilio)
  allow write: if request.auth != null;
  
  // Eliminación: admin o usuario propietario
  allow delete: if request.auth != null && (
    (exists(/databases/(default)/documents/usuarios/$(request.auth.uid)) &&
     get(/databases/(default)/documents/usuarios/$(request.auth.uid)).data.role == 'ADMIN') ||
    (exists(/databases/(default)/documents/domicilios/$(domicilioId)) &&
     get(/databases/(default)/documents/domicilios/$(domicilioId)).data.usuarioId == request.auth.uid)
  );
}
```

### 3. Crear Índices Compuestos (si es necesario)

Si ves un error sobre índices faltantes, Firebase te dará un enlace para crearlos automáticamente. O puedes crearlos manualmente:

1. Ve a **Firestore Database** > **Indexes**
2. Haz clic en **Create Index**
3. Colección: `domicilios`
4. Campos:
   - `usuarioId` (Ascending)
   - `fechaCreacion` (Descending)
5. Haz clic en **Create**

### 4. Verificar

Después de aplicar las reglas:

1. **Reinicia la aplicación**
2. **Intenta crear un domicilio** nuevamente
3. **Verifica que no aparezcan errores** de permisos

## 📝 NOTAS

- Las reglas usan la colección `usuarios` (no `users`)
- Los roles deben ser: `ADMIN`, `VET`, `VET_ASSISTANT` (en mayúsculas)
- Los estados deben coincidir con el enum: `PENDIENTE`, `APROBADO`, `RECHAZADO`, `EN_CAMINO`, `FINALIZADO`, `CANCELADO`




