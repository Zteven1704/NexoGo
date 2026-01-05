package com.example.nexogo.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manejador de errores para operaciones de Firebase
 */
object FirebaseErrorHandler {
    
    /**
     * Ejecuta una operación de Firebase de forma segura
     * @param operation La operación a ejecutar
     * @return Result<T> con el resultado o el error capturado
     */
    suspend fun <T> safeCall(operation: suspend () -> T): Result<T> {
        return try {
            withContext(Dispatchers.IO) {
                Result.success(operation())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene un mensaje de error amigable para el usuario
     * @param exception La excepción capturada
     * @return Mensaje de error traducido
     */
    fun getErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("network") == true -> 
                "Error de conexión. Verifica tu internet."
            exception.message?.contains("invalid-email") == true -> 
                "Formato de email inválido."
            exception.message?.contains("user-not-found") == true -> 
                "Usuario no encontrado."
            exception.message?.contains("wrong-password") == true -> 
                "Contraseña incorrecta."
            exception.message?.contains("email-already-in-use") == true -> 
                "Este email ya está registrado."
            exception.message?.contains("weak-password") == true -> 
                "La contraseña es muy débil."
            exception.message?.contains("too-many-requests") == true -> 
                "Demasiados intentos. Intenta más tarde."
            exception.message?.contains("user-disabled") == true -> 
                "Esta cuenta ha sido deshabilitada."
            exception.message?.contains("operation-not-allowed") == true -> 
                "Operación no permitida."
            exception.message?.contains("requires-recent-login") == true -> 
                "Necesitas iniciar sesión nuevamente."
            exception.message?.contains("quota-exceeded") == true -> 
                "Límite de cuota excedido."
            exception.message?.contains("unavailable") == true -> 
                "Servicio temporalmente no disponible."
            exception.message?.contains("permission-denied") == true -> 
                "No tienes permisos para realizar esta acción."
            exception.message?.contains("not-found") == true -> 
                "Recurso no encontrado."
            exception.message?.contains("already-exists") == true -> 
                "El recurso ya existe."
            exception.message?.contains("failed-precondition") == true -> 
                "Condición previa fallida."
            exception.message?.contains("aborted") == true -> 
                "Operación cancelada."
            exception.message?.contains("out-of-range") == true -> 
                "Valor fuera de rango."
            exception.message?.contains("unimplemented") == true -> 
                "Función no implementada."
            exception.message?.contains("internal") == true -> 
                "Error interno del servidor."
            exception.message?.contains("unavailable") == true -> 
                "Servicio no disponible."
            exception.message?.contains("data-loss") == true -> 
                "Pérdida de datos."
            exception.message?.contains("unauthenticated") == true -> 
                "No autenticado. Inicia sesión nuevamente."
            else -> exception.message ?: "Error desconocido"
        }
    }
    
    /**
     * Verifica si el error es de red
     */
    fun isNetworkError(exception: Exception): Boolean {
        return exception.message?.contains("network") == true ||
               exception.message?.contains("unavailable") == true ||
               exception.message?.contains("timeout") == true
    }
    
    /**
     * Verifica si el error es de autenticación
     */
    fun isAuthError(exception: Exception): Boolean {
        return exception.message?.contains("user-not-found") == true ||
               exception.message?.contains("wrong-password") == true ||
               exception.message?.contains("invalid-email") == true ||
               exception.message?.contains("user-disabled") == true ||
               exception.message?.contains("unauthenticated") == true
    }
    
    /**
     * Verifica si el error es de permisos
     */
    fun isPermissionError(exception: Exception): Boolean {
        return exception.message?.contains("permission-denied") == true ||
               exception.message?.contains("operation-not-allowed") == true
    }
}
