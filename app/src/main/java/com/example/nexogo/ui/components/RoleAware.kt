package com.example.nexogo.ui.components

import androidx.compose.runtime.Composable
import com.example.nexogo.core.models.UserRole

/**
 * Composable que muestra contenido solo si el usuario tiene uno de los roles permitidos
 * Los administradores siempre pueden ver el contenido
 */
@Composable
fun RoleAware(
    role: UserRole?,
    allowedRoles: List<UserRole>,
    content: @Composable () -> Unit
) {
    if (role == null) return
    
    // Los administradores siempre pueden ver el contenido
    if (role == UserRole.ADMIN) {
        content()
        return
    }
    
    // Verificar si el rol está en la lista de roles permitidos
    if (allowedRoles.contains(role)) {
        content()
    }
}

/**
 * Composable que muestra contenido solo si el usuario tiene un rol específico
 */
@Composable
fun RoleAware(
    role: UserRole?,
    allowedRole: UserRole,
    content: @Composable () -> Unit
) {
    RoleAware(role, listOf(allowedRole), content)
}

/**
 * Versión con strings para compatibilidad
 */
@Composable
fun RoleAware(
    role: String?,
    allowedRoles: List<String>,
    content: @Composable () -> Unit
) {
    if (role == null) return
    
    // Los administradores siempre pueden ver el contenido
    if (role == "ADMIN" || role == UserRole.ADMIN.name) {
        content()
        return
    }
    
    // Verificar si el rol está en la lista de roles permitidos
    if (allowedRoles.contains(role)) {
        content()
    }
}




