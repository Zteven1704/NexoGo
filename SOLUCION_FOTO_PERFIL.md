# 🔧 Solución: Error al Subir Foto de Perfil

## 🚨 Problema
Error al intentar cambiar la foto de perfil: "Error al subir la foto"

## ✅ Solución Implementada

### 1. Helper Centralizado Creado

Se creó `ProfileImageUploader.kt` que:
- ✅ Usa la ruta correcta: `users/{userId}/profile/{fileName}`
- ✅ Verifica permisos antes de subir
- ✅ Proporciona mensajes de error descriptivos
- ✅ Maneja errores de red, permisos y cancelación

### 2. Reglas de Storage Actualizadas

Las reglas ahora permiten:
- ✅ `users/{userId}/profile/{fileName}` (ruta principal)
- ✅ `users/{userId}/profile_pics/{fileName}` (ruta alternativa)
- ✅ `profile_images/{fileName}` (ruta alternativa)

### 3. ViewModels Actualizados

Todos los ViewModels ahora usan el helper centralizado:
- ✅ `ProfileViewModel.kt` (modules/profile)
- ✅ `ProfileViewModel.kt` (viewmodel)

## 📋 Pasos para Aplicar

### 1. Aplicar Reglas de Storage

1. Ve a **Firebase Console** > **Storage** > **Rules**
2. Copia el contenido actualizado de `storage.rules`
3. Pega y haz clic en **Publish**

### 2. Verificar Permisos

Asegúrate de que:
- El usuario esté autenticado
- El `userId` coincida con el usuario autenticado
- Las reglas de Storage estén aplicadas

### 3. Probar

1. Abre la pantalla de perfil
2. Selecciona una foto
3. La subida debería funcionar correctamente

## 🔍 Diagnóstico de Errores

Si aún hay errores, verifica:

### Error: "Permission denied"
- **Causa**: Las reglas de Storage no están aplicadas o son incorrectas
- **Solución**: Aplicar las reglas actualizadas en Firebase Console

### Error: "Network error"
- **Causa**: Sin conexión a internet
- **Solución**: Verificar conexión (la app funciona offline pero la subida requiere conexión)

### Error: "Usuario no autenticado"
- **Causa**: El usuario no está logueado
- **Solución**: Hacer login nuevamente

## 📝 Ruta de Almacenamiento

Las fotos de perfil se guardan en:
```
users/{userId}/profile/profile_{userId}_{timestamp}_{uuid}.jpg
```

Esta ruta está protegida por las reglas de Storage y solo el usuario propietario puede subir/eliminar su foto.

## ✅ Verificación

Después de aplicar los cambios:
1. ✅ Las reglas de Storage están actualizadas
2. ✅ El código usa el helper centralizado
3. ✅ Los mensajes de error son descriptivos
4. ✅ La subida funciona correctamente

---

**Nota**: Si el error persiste, revisa los logs en Logcat buscando "ProfileImageUploader" para ver el error específico.




