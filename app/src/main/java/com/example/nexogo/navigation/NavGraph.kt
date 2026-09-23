package com.example.nexogo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nexogo.platform.clients.ui.PlatformClientsScreen
import com.example.nexogo.platform.company.onboarding.CompanyOnboardingScreen
import com.example.nexogo.platform.documents.ui.PlatformDocumentsScreen
import com.example.nexogo.platform.records.ui.PlatformRecordsScreen
import com.example.nexogo.platform.role.ui.PlatformPermissionsScreen
import com.example.nexogo.platform.role.ui.PlatformRolesScreen
import com.example.nexogo.platform.users.ui.PlatformUserManagementScreen
import com.example.nexogo.ui.screens.auth.RegisterScreen
import com.example.nexogo.ui.screens.auth.SimpleLoginScreen
import com.example.nexogo.ui.screens.auth.SessionSplashScreen
import com.example.nexogo.ui.screens.home.HomeScreen
import com.example.nexogo.ui.screens.profile.ProfileScreen
import com.example.nexogo.ui.screens.settings.AboutScreen
import com.example.nexogo.ui.screens.settings.HelpScreen
import com.example.nexogo.ui.screens.settings.PlatformSettingsScreen
import com.example.nexogo.viewmodel.PersistentAuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first

/**
 * Único grafo de navegación V1 — Platform only.
 * Post-login: Home (gate de company) o CompanyOnboarding.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SessionSplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            SimpleLoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.CompanyOnboarding.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CompanyOnboarding.route) {
            CompanyOnboardingScreen(
                onCompleted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.CompanyOnboarding.route) { inclusive = true }
                    }
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCompanyOnboarding = {
                    navController.navigate(Screen.CompanyOnboarding.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToPlatformClients = {
                    navController.navigate(Screen.Clients.route)
                },
                onNavigateToPlatformRecords = {
                    navController.navigate(Screen.Records.route)
                },
                onNavigateToPlatformDocuments = {
                    navController.navigate(Screen.Documents.route)
                },
                onNavigateToPlatformUsers = {
                    navController.navigate(Screen.Users.route)
                },
                onNavigateToPlatformRoles = {
                    navController.navigate(Screen.Roles.route)
                },
                onNavigateToPlatformPermissions = {
                    navController.navigate(Screen.Permissions.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Clients.route) {
            PlatformClientsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Records.route) {
            PlatformRecordsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Documents.route) {
            PlatformDocumentsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Users.route) {
            PlatformUserManagementScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Roles.route) {
            PlatformRolesScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Permissions.route) {
            PlatformPermissionsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            PlatformSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToHelp = { navController.navigate(Screen.Help.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.Help.route) {
            HelpScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
