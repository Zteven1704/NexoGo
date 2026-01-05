# 🔧 CREAR ÍNDICE DE FIRESTORE PARA DOMICILIOS

## 🚨 PROBLEMA
Error: `FAILED_PRECONDITION: The query requires an index`

## ✅ SOLUCIÓN RÁPIDA (RECOMENDADA)

### **Opción 1: Usar el enlace directo de Firebase**

1. **Copia el enlace** que aparece en el error:
   ```
   https://console.firebase.google.com/v1/r/project/nexogo-82003/firestore/indexes?create_composite=...
   ```

2. **Pégalo en tu navegador** y presiona Enter

3. **Firebase creará el índice automáticamente**

4. **Espera unos minutos** mientras se crea el índice (puede tardar 2-5 minutos)

5. **Reinicia la aplicación** y prueba nuevamente

---

## ✅ SOLUCIÓN ALTERNATIVA: Desplegar con Firebase CLI

### **Paso 1: Instalar Firebase CLI** (si no lo tienes)

```bash
npm install -g firebase-tools
```

### **Paso 2: Autenticarse**

```bash
firebase login
```

### **Paso 3: Desplegar índices**

```bash
firebase deploy --only firestore:indexes
```

---

## 📋 ÍNDICE NECESARIO

El índice que se necesita es:

- **Colección**: `domicilios`
- **Campos**:
  - `usuarioId` (Ascendente)
  - `fechaCreacion` (Descendente)

Este índice ya está definido en el archivo `firestore.indexes.json` del proyecto.

---

## ⏱️ TIEMPO DE CREACIÓN

Los índices de Firestore pueden tardar:
- **Índices simples**: 1-2 minutos
- **Índices compuestos**: 2-5 minutos
- **Índices grandes**: 5-10 minutos

Puedes verificar el estado del índice en:
**Firebase Console** > **Firestore Database** > **Indexes**

El estado aparecerá como:
- 🟡 **Building** (en construcción)
- 🟢 **Enabled** (listo para usar)

---

## 🔍 VERIFICAR QUE FUNCIONA

Después de crear el índice:

1. **Espera a que el estado sea "Enabled"**
2. **Reinicia la aplicación**
3. **Intenta cargar la lista de domicilios**
4. **El error debería desaparecer**

---

## 📝 NOTA

Si el enlace no funciona o expiró, puedes crear el índice manualmente:

1. Ve a **Firebase Console** > **Firestore Database** > **Indexes**
2. Haz clic en **Create Index**
3. Colección: `domicilios`
4. Agrega campos:
   - Campo: `usuarioId`, Orden: `Ascending`
   - Campo: `fechaCreacion`, Orden: `Descending`
5. Haz clic en **Create**




