package com.example.nexogo.navigation

import com.example.nexogo.core.models.UserRole

/**
 * Utilidad para verificar si un usuario puede acceder a una ruta según su rol
 */
object RoleGuard {
    /**
     * Verifica si el usuario puede acceder a una ruta según los roles requeridos
     * 
     * @param userRole Rol del usuario actual (puede ser null si no está autenticado)
     * @param requiredRoles Lista de roles que pueden acceder a la ruta
     * @return true si el usuario puede acceder, false en caso contrario
     */
    fun canAccessRoute(userRole: UserRole?, requiredRoles: List<UserRole>): Boolean {
        if (userRole == null) return false
        
        // Los administradores pueden acceder a todo
        if (userRole == UserRole.ADMIN) return true
        
        // Verificar si el rol del usuario está en la lista de roles permitidos
        return requiredRoles.contains(userRole)
    }
    
    /**
     * Verifica si el usuario puede acceder usando strings (para compatibilidad)
     */
    fun canAccessRoute(userRole: String?, requiredRoles: List<String>): Boolean {
        if (userRole == null) return false
        
        // Los administradores pueden acceder a todo
        if (userRole == "ADMIN" || userRole == UserRole.ADMIN.name) return true
        
        // Verificar si el rol del usuario está en la lista de roles permitidos
        return requiredRoles.contains(userRole)
    }
    
    /**
     * Verifica si el usuario tiene un rol específico
     */
    fun hasRole(userRole: UserRole?, role: UserRole): Boolean {
        return userRole == role || userRole == UserRole.ADMIN
    }
    
    /**
     * Verifica si el usuario tiene alguno de los roles especificados
     */
    fun hasAnyRole(userRole: UserRole?, roles: List<UserRole>): Boolean {
        if (userRole == null) return false
        if (userRole == UserRole.ADMIN) return true
        return roles.contains(userRole)
    }
}




