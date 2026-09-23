package com.example.nexogo.navigation

/**
 * Rutas de la app coherente V1 — solo flujo Platform activo.
 * Auth → Company → Home → (Clients | Records | Documents | Users | Roles | Permissions)
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    object Home : Screen("home")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Help : Screen("help")
    object About : Screen("about")

    object CompanyOnboarding : Screen("company_onboarding")

    /** Operación tenant */
    object Clients : Screen("clients")
    object Records : Screen("records")
    object Documents : Screen("documents")

    /** Administración tenant */
    object Users : Screen("users")
    object Roles : Screen("roles")
    object Permissions : Screen("permissions")
}
