# 🔧 Crear Índices de Firestore para Domicilios

Firestore requiere índices compuestos para las consultas del módulo de domicilios.

## ⚠️ SOLUCIÓN RÁPIDA

**El error proporciona un enlace directo. Haz clic en el enlace del Logcat para crear el índice automáticamente:**

El enlace está en el error:
```
https://console.firebase.google.com/v1/r/project/nexogo-82003/firestore/indexes?create_composite=...
```

## 📋 Índices Requeridos

### 1. Índice para consulta por usuario (URGENTE - Este es el que falta)
- **Colección**: `domicilios`
- **Campos**:
  - `usuarioId` (ASCENDING)
  - `fechaCreacion` (DESCENDING)

### 2. Índice para consulta de pendientes
- **Colección**: `domicilios`
- **Campos**:
  - `estado` (ASCENDING)
  - `fechaCreacion` (DESCENDING)

### 3. Índice para consulta de asignados
- **Colección**: `domicilios`
- **Campos**:
  - `veterinarioAsignadoId` (ASCENDING)
  - `estado` (ASCENDING)
  - `fechaSolicitada` (ASCENDING)

## 🚀 Opción 1: Usar el enlace directo del error (MÁS RÁPIDO)

1. **Copia el enlace completo del error en Logcat**
2. **Pégalo en tu navegador**
3. **Haz clic en "Crear índice"**
4. **Espera 2-5 minutos** mientras se crea el índice

## 🚀 Opción 2: Crear manualmente en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto: **nexogo-82003**
3. Ve a **Firestore Database** > **Índices**
4. Haz clic en **Crear índice**
5. Configura:
   - **Colección ID**: `domicilios`
   - **Campo 1**: `usuarioId` (Ascendente)
   - **Campo 2**: `fechaCreacion` (Descendente)
6. Haz clic en **Crear**

## 🚀 Opción 3: Usar el archivo firestore.indexes.json

El archivo `firestore.indexes.json` ya está creado en la raíz del proyecto. Para desplegarlo:

```bash
# Si tienes Firebase CLI instalado
firebase deploy --only firestore:indexes

# O usar el script PowerShell
.\APLICAR_INDICES_FIRESTORE.ps1
```

## ⚠️ Nota Importante

- Los índices pueden tardar **2-5 minutos** en crearse
- Mientras se crea, verás el estado "Building" en Firebase Console
- Una vez que esté "Enabled", las consultas funcionarán
- **No necesitas reiniciar la app**, funcionará automáticamente

## ✅ Verificación

Después de crear los índices:
1. Ve a Firebase Console > Firestore > Índices
2. Verifica que el índice esté en estado "Enabled" (verde)
3. Navega al módulo de domicilios en la app
4. Verifica que no aparezcan más errores de índices en Logcat

## 🔍 Si el error persiste

Si después de crear el índice sigue apareciendo el error:
1. Verifica que el nombre del campo sea exactamente `usuarioId` (no `usuario_id`)
2. Verifica que el orden de los campos sea correcto
3. Espera unos minutos más (puede tardar hasta 10 minutos)
4. Reinicia la app completamente

