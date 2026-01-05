# 🔥 APLICAR REGLAS DE FIRESTORE - SOLUCIÓN PERMISSION_DENIED

## 🚨 PROBLEMA
Error: `PERMISSION_DENIED: Missing or insufficient permissions` al intentar agendar citas.

## ✅ SOLUCIÓN

### **Opción 1: Aplicar desde Firebase Console (RECOMENDADO)**

1. **Abrir Firebase Console**
   - Ve a: https://console.firebase.google.com/
   - Selecciona tu proyecto NexoGo

2. **Ir a Firestore Database**
   - En el menú lateral, haz clic en "Firestore Database"
   - Ve a la pestaña "Rules"

3. **Copiar y pegar las reglas**
   - Copia todo el contenido del archivo `firestore.rules`
   - Pega en el editor de reglas
   - Haz clic en "Publish"

### **Opción 2: Instalar Firebase CLI**

```bash
# Instalar Node.js primero desde: https://nodejs.org/
# Luego instalar Firebase CLI
npm install -g firebase-tools

# Autenticarse
firebase login

# Aplicar reglas
firebase deploy --only firestore:rules
```

### **Opción 3: Reglas temporales más permisivas**

Si necesitas una solución rápida, puedes usar estas reglas temporales:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Reglas temporales - PERMITIR TODO A USUARIOS AUTENTICADOS
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 🔧 REGLAS ACTUALIZADAS

Las reglas han sido actualizadas para:
- ✅ Usar roles correctos: `ADMIN`, `VET`, `ASSISTANT` (en lugar de `ADMINISTRATOR`, `VETERINARIAN`)
- ✅ Usar colecciones en español: `usuarios`, `citas`, `inventario`
- ✅ Permitir acceso a usuarios autenticados para debug

## 🎯 DESPUÉS DE APLICAR

1. **Reinicia la aplicación** en el dispositivo
2. **Intenta agendar una cita** nuevamente
3. **Verifica que no aparezcan errores** de permisos

## 📱 VERIFICACIÓN

Si sigues teniendo problemas:
1. Verifica que el usuario esté autenticado
2. Revisa la consola de Firebase para ver los logs
3. Asegúrate de que las reglas se aplicaron correctamente

## ⚠️ IMPORTANTE

Estas reglas son **temporales para debug**. En producción, deberías usar reglas más restrictivas basadas en roles específicos.

