package com.example.nexogo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nexogo.ui.screens.auth.LoginScreen
import com.example.nexogo.ui.screens.auth.SimpleLoginScreen
import com.example.nexogo.ui.screens.auth.ModernLoginScreen
import com.example.nexogo.ui.screens.auth.UltraSimpleSplashScreen
import com.example.nexogo.ui.screens.auth.UltraSimpleLoginScreen
import com.example.nexogo.ui.screens.auth.MinimalSplashScreen
import com.example.nexogo.ui.screens.auth.MinimalLoginScreen
import com.example.nexogo.ui.screens.dashboard.MinimalDashboardScreen
import com.example.nexogo.ui.screens.auth.SplashScreen
import com.example.nexogo.ui.screens.auth.RegisterScreen
import com.example.nexogo.ui.screens.home.HomeScreen
import com.example.nexogo.ui.screens.profile.ProfileScreen
import com.example.nexogo.ui.screens.appointments.AppointmentsScreen
import com.example.nexogo.ui.screens.appointments.SimpleAppointmentsScreen
import com.example.nexogo.ui.screens.appointments.SafeAppointmentsScreen
import com.example.nexogo.ui.screens.appointments.MinimalAppointmentsScreen
import com.example.nexogo.ui.screens.appointments.TestAppointmentsScreen
import com.example.nexogo.ui.screens.appointments.AppointmentsScreenImproved
import com.example.nexogo.ui.screens.appointments.BeautifulAppointmentsScreen
import com.example.nexogo.ui.screens.appointments.CreateAppointmentScreen
import com.example.nexogo.ui.screens.appointments.BeautifulEditAppointmentScreen
import com.example.nexogo.ui.screens.patients.PatientsScreen
import com.example.nexogo.ui.screens.patients.CreateEditPatientScreen
import com.example.nexogo.ui.screens.clinical.ClinicalRecordsScreen
import com.example.nexogo.ui.screens.clinical.CreateEditClinicalRecordScreen
import com.example.nexogo.ui.screens.clinical.ViewClinicalRecordScreen
import com.example.nexogo.modules.inventory.ui.InventoryMainScreen
import com.example.nexogo.modules.inventory.ui.InventoryEditScreen
import com.example.nexogo.modules.config.ui.CategoryManagementScreen
import com.example.nexogo.modules.sales.ui.SalesMainScreen
import com.example.nexogo.modules.sales.ui.SalesEditScreen
import com.example.nexogo.modules.sales.ui.ServicesManagementScreen
import com.example.nexogo.ui.screens.medical.MedicalRecordsScreen
import com.example.nexogo.ui.screens.chat.ChatListScreen
import com.example.nexogo.ui.screens.chat.ChatScreen
import com.example.nexogo.ui.screens.chat.NewChatScreen
import com.example.nexogo.ui.screens.chat.ChatConversationScreen
import com.example.nexogo.ui.screens.chat.ChatbotConfigScreen
import com.example.nexogo.modules.chat.ChatScreen as NewChatScreen
import com.example.nexogo.modules.chat.ChatListScreen
import com.example.nexogo.modules.history.ClinicalHistoryScreen
import com.example.nexogo.modules.history.ui.HistoryListScreen
import com.example.nexogo.modules.history.ui.HistoryDetailScreen
import com.example.nexogo.modules.history.ui.HistoryEditScreen
import com.example.nexogo.modules.history.ui.SimpleHistoryListScreen
import com.example.nexogo.modules.history.ui.SimpleHistoryEditScreen
import com.example.nexogo.modules.history.ui.UltraSimpleHistoryScreen
import com.example.nexogo.modules.history.ui.HistoryPdfPreview
import com.example.nexogo.modules.history.repo.HistoryRepository
import com.example.nexogo.modules.history.viewmodel.HistoryViewModel
import com.example.nexogo.core.FirebaseRepository
// import com.example.nexogo.modules.inventory.InventoryScreen as NewInventoryScreen
import com.example.nexogo.modules.sales.SalesScreen as NewSalesScreen
import com.example.nexogo.modules.config.ConfigScreen
import com.example.nexogo.modules.dashboard.DashboardScreen
import com.example.nexogo.ui.screens.dashboard.SimpleDashboardScreen
import com.example.nexogo.ui.screens.settings.SettingsScreen
import com.example.nexogo.ui.screens.settings.SimpleSettingsScreen
import com.example.nexogo.ui.screens.settings.StorageManagementScreen
import com.example.nexogo.ui.screens.settings.ProductCategoriesScreen
import com.example.nexogo.ui.screens.sales.SalesScreen
// import com.example.nexogo.ui.screens.sales.CreateEditSaleScreen
// import com.example.nexogo.ui.screens.sales.ReportsScreen
// import com.example.nexogo.ui.screens.reports.ReportsMainScreen
// import com.example.nexogo.ui.screens.reports.SalesReportsScreen
import com.example.nexogo.ui.screens.settings.HelpScreen
import com.example.nexogo.ui.screens.settings.AboutScreen
import com.example.nexogo.ui.screens.admin.AdminScreen
import com.example.nexogo.ui.screens.admin.UserApprovalScreen
import com.example.nexogo.FirebaseTestScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            MinimalSplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Authentication screens
        composable(Screen.Login.route) {
            SimpleLoginScreen(
                onNavigateToRegister = {
                    println("DEBUG: NavGraph - Navegando a registro")
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ModernLogin.route) {
            ModernLoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.ModernLogin.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        composable(Screen.UltraSimpleLogin.route) {
            UltraSimpleLoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.UltraSimpleLogin.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.MinimalLogin.route) {
            MinimalLoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.MinimalLogin.route) { inclusive = true }
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
                }
            )
        }
        
                // Main app screens
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToProfile = {
                            navController.navigate(Screen.Profile.route)
                        },
                        onNavigateToAppointments = {
                            navController.navigate(Screen.Appointments.route)
                        },
                        onNavigateToPatients = {
                            navController.navigate(Screen.Patients.route)
                        },
                        onNavigateToClinicalRecords = {
                            navController.navigate(Screen.ClinicalRecords.route)
                        },
                        onNavigateToInventory = {
                            navController.navigate(Screen.Inventory.route)
                        },
                        onNavigateToMedicalRecords = {
                            navController.navigate(Screen.MedicalRecords.route)
                        },
                        onNavigateToChat = {
                            navController.navigate(Screen.ChatList.route)
                        },
                        onNavigateToSales = {
                            navController.navigate(Screen.Sales.route)
                        },
                        onNavigateToReports = {
                            navController.navigate(Screen.Reports.route)
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route)
                        },
                        onNavigateToAdmin = {
                            navController.navigate(Screen.Admin.route)
                        },
                        onNavigateToFirebaseTest = {
                            navController.navigate(Screen.FirebaseTest.route)
                        },
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }
                
                composable(Screen.Dashboard.route) {
                    MinimalDashboardScreen(
                        onNavigateToProfile = {
                            navController.navigate(Screen.Profile.route)
                        },
                        onNavigateToAppointments = {
                            navController.navigate(Screen.Appointments.route)
                        },
                        onNavigateToPatients = {
                            navController.navigate(Screen.Patients.route)
                        },
                        onNavigateToClinicalHistory = {
                            navController.navigate(Screen.HistoryList.route)
                        },
                        onNavigateToInventory = {
                            navController.navigate(Screen.Inventory.route)
                        },
                        onNavigateToSales = {
                            navController.navigate(Screen.Sales.route)
                        },
                        onNavigateToChat = {
                            navController.navigate(Screen.ChatList.route)
                        },
                        onNavigateToConfig = {
                            navController.navigate(Screen.Settings.route)
                        },
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
                composable(Screen.Appointments.route) {
                    BeautifulAppointmentsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToCreateAppointment = {
                            navController.navigate(Screen.CreateAppointment.route)
                        },
                        onNavigateToEditAppointment = { appointmentId ->
                            android.util.Log.d("NEXOGO_NAV", "🚀 Navegando a editar cita: $appointmentId")
                            navController.navigate(Screen.EditAppointment.createRoute(appointmentId))
                        }
                    )
                }
        
                composable(Screen.CreateAppointment.route) {
                    CreateAppointmentScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable(Screen.EditAppointment.route) { backStackEntry ->
                    val appointmentId = backStackEntry.arguments?.getString("appointmentId")
                    BeautifulEditAppointmentScreen(
                        appointmentId = appointmentId ?: "",
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.Patients.route) {
                    PatientsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToCreatePatient = {
                            navController.navigate(Screen.CreateEditPatient.route)
                        },
                        onNavigateToEditPatient = { patientId ->
                            navController.navigate("${Screen.CreateEditPatient.route}/$patientId")
                        }
                    )
                }

                composable("${Screen.CreateEditPatient.route}/{patientId}") { backStackEntry ->
                    val patientId = backStackEntry.arguments?.getString("patientId")
                    CreateEditPatientScreen(
                        patientId = patientId,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.CreateEditPatient.route) {
                    CreateEditPatientScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                
                // Clinical Records routes
                composable(Screen.ClinicalRecords.route) {
                    ClinicalHistoryScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCreateRecord = { navController.navigate(Screen.CreateEditClinicalRecord.route) }
                    )
                }
                
                composable(Screen.CreateEditClinicalRecord.route + "/{recordId}") { backStackEntry ->
                    val recordId = backStackEntry.arguments?.getString("recordId")
                    val isEditing = recordId != null
                    CreateEditClinicalRecordScreen(
                        recordId = recordId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.CreateEditClinicalRecord.route) {
                    CreateEditClinicalRecordScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.ViewClinicalRecord.route + "/{recordId}") { backStackEntry ->
                    val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
                    ViewClinicalRecordScreen(
                        recordId = recordId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEdit = { recordId -> 
                            navController.navigate("${Screen.CreateEditClinicalRecord.route}/$recordId")
                        }
                    )
                }
                
                // New History Module routes - Restored with simple list
                composable(Screen.HistoryList.route) {
                    android.util.Log.d("NEXOGO_DEBUG", "Navegando a HistoryList.route")
                    SimpleHistoryListScreen(
                        onNavigateBack = {
                            android.util.Log.d("NEXOGO_DEBUG", "Navegando de vuelta desde HistoryList")
                            navController.popBackStack()
                        },
                        onNavigateToCreate = {
                            android.util.Log.d("NEXOGO_DEBUG", "Navegando a crear historial")
                            navController.navigate(Screen.HistoryEdit.route)
                        },
                        onNavigateToEdit = { recordId ->
                            android.util.Log.d("NEXOGO_DEBUG", "Navegando a editar historial: $recordId")
                            navController.navigate("${Screen.HistoryEdit.route}?recordId=$recordId")
                        },
                        onDeleteRecord = { recordId ->
                            android.util.Log.d("NEXOGO_DEBUG", "Eliminando historial: $recordId")
                            // TODO: Implementar eliminación
                        }
                    )
                }
                
                composable(Screen.HistoryDetail.route) { backStackEntry ->
                    val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
                    val firebaseRepository = FirebaseRepository()
                    val historyRepository = HistoryRepository(firebaseRepository)
                    val historyViewModel = HistoryViewModel(historyRepository)
                    
                    HistoryDetailScreen(
                        recordId = recordId,
                        viewModel = historyViewModel,
                        onNavigateToEdit = { recordId ->
                            navController.navigate(Screen.HistoryEdit.route)
                        },
                        onNavigateBack = { navController.popBackStack() },
                        onDownloadPdf = { recordId ->
                            navController.navigate(Screen.HistoryPdfPreview.createRoute(recordId))
                        }
                    )
                }
                
                composable(
                    route = "${Screen.HistoryEdit.route}?recordId={recordId}",
                    arguments = listOf(
                        navArgument("recordId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    android.util.Log.d("NEXOGO_DEBUG", "Navegando a HistoryEdit.route")
                    val recordId = backStackEntry.arguments?.getString("recordId")
                    android.util.Log.d("NEXOGO_DEBUG", "RecordId recibido: $recordId")
                    SimpleHistoryEditScreen(
                        recordId = recordId, // Para crear nuevo historial o editar existente
                        onNavigateBack = { 
                            android.util.Log.d("NEXOGO_DEBUG", "Navegando de vuelta desde HistoryEdit")
                            navController.popBackStack() 
                        }
                    )
                }
                
                composable(Screen.HistoryPdfPreview.route) { backStackEntry ->
                    val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
                    val firebaseRepository = FirebaseRepository()
                    val historyRepository = HistoryRepository(firebaseRepository)
                    val historyViewModel = HistoryViewModel(historyRepository)
                    
                    HistoryPdfPreview(
                        recordId = recordId,
                        viewModel = historyViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onDownloadPdf = { recordId ->
                            // TODO: Implementar descarga de PDF
                        }
                    )
                }

                composable(Screen.Inventory.route) {
                    InventoryMainScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToCreate = {
                            navController.navigate(Screen.CreateEditProduct.createRoute(null))
                        },
                        onNavigateToEdit = { productId ->
                            navController.navigate(Screen.CreateEditProduct.createRoute(productId))
                        },
                        onNavigateToCategories = {
                            navController.navigate(Screen.CategoryManagement.route)
                        }
                    )
                }
        
        composable(
            route = Screen.CreateEditProduct.route,
            arguments = listOf(
                androidx.navigation.navArgument("productId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                android.util.Log.d("NEXOGO_NAV", "Navegando a InventoryEdit con productId: $productId")
                InventoryEditScreen(
                productId = productId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProductSaved = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.CategoryManagement.route) {
            CategoryManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Sales.route) {
            SalesMainScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.CreateEditSale.createRoute(null))
                },
                onNavigateToEdit = { saleId ->
                    navController.navigate(Screen.CreateEditSale.createRoute(saleId))
                },
                onNavigateToServices = {
                    navController.navigate(Screen.ServicesManagement.route)
                }
            )
        }
        
        composable(
            route = Screen.CreateEditSale.route,
            arguments = listOf(
                androidx.navigation.navArgument("saleId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val saleId = backStackEntry.arguments?.getString("saleId")
            android.util.Log.d("NEXOGO_NAV", "Navegando a SalesEdit con saleId: $saleId")
            SalesEditScreen(
                saleId = saleId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaleSaved = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ServicesManagement.route) {
            ServicesManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.MedicalRecords.route) {
            MedicalRecordsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ChatList.route) {
            ChatListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChat = { conversationId ->
                    navController.navigate("${Screen.ChatConversation.route}/$conversationId")
                },
                onNavigateToNewChat = {
                    navController.navigate(Screen.NewChat.route)
                }
            )
        }
        
        composable(Screen.NewChat.route) {
            NewChatScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChat = { chatId ->
                    navController.navigate(Screen.ChatConversation.createRoute(chatId))
                }
            )
        }
        
        composable(Screen.ChatConversation.route) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            NewChatScreen(
                conversationId = chatId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ChatbotConfig.route) {
            ChatbotConfigScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
                composable(Screen.Settings.route) {
                    SimpleSettingsScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        onNavigateToStorage = {
                            navController.navigate(Screen.StorageManagement.route)
                        },
                        onNavigateToProductCategories = {
                            navController.navigate(Screen.ProductCategories.route)
                        },
                        onNavigateToChatbotConfig = {
                            navController.navigate(Screen.ChatbotConfig.route)
                        },
                        onNavigateToHelp = {
                            navController.navigate(Screen.Help.route)
                        },
                        onNavigateToAbout = {
                            navController.navigate(Screen.About.route)
                        },
                        onNavigateToProfile = {
                            navController.navigate(Screen.Profile.route)
                        }
                    )
                }
                
                composable(Screen.StorageManagement.route) {
                    StorageManagementScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable(Screen.ProductCategories.route) {
                    ProductCategoriesScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                
                
                
                
                composable("${Screen.SaleDetail.route}/{saleId}") { backStackEntry ->
                    val saleId = backStackEntry.arguments?.getString("saleId") ?: ""
                    // TODO: Create SaleDetailScreen
                    SalesScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToCreateSale = {
                            navController.navigate(Screen.CreateEditSale.route)
                        },
                        onNavigateToSaleDetail = { saleId ->
                            navController.navigate("${Screen.SaleDetail.route}/$saleId")
                        },
                        onNavigateToReports = {
                            navController.navigate(Screen.Reports.route)
                        }
                    )
                }
                
                composable(Screen.Reports.route) {
                    // ReportsMainScreen(
                    //     onNavigateBack = {
                    //         navController.popBackStack()
                    //     },
                    //     onNavigateToSalesReports = {
                    //         navController.navigate(Screen.SalesReports.route)
                    //     },
                    //     onNavigateToAppointmentsReports = {
                    //         navController.navigate(Screen.AppointmentsReports.route)
                    //     },
                    //     onNavigateToPatientsReports = {
                    //         navController.navigate(Screen.PatientsReports.route)
                    //     },
                    //     onNavigateToInventoryReports = {
                    //         navController.navigate(Screen.InventoryReports.route)
                    //     },
                    //     onNavigateToClinicalReports = {
                    //         navController.navigate(Screen.ClinicalReports.route)
                    //     }
                    // )
                    Text("Pantalla en desarrollo")
                }
                
                composable(Screen.ReportsMain.route) {
                    // ReportsMainScreen(
                    //     onNavigateBack = {
                    //         navController.popBackStack()
                    //     },
                    //     onNavigateToSalesReports = {
                    //         navController.navigate(Screen.SalesReports.route)
                    //     },
                    //     onNavigateToAppointmentsReports = {
                    //         navController.navigate(Screen.AppointmentsReports.route)
                    //     },
                    //     onNavigateToPatientsReports = {
                    //         navController.navigate(Screen.PatientsReports.route)
                    //     },
                    //     onNavigateToInventoryReports = {
                    //         navController.navigate(Screen.InventoryReports.route)
                    //     },
                    //     onNavigateToClinicalReports = {
                    //         navController.navigate(Screen.ClinicalReports.route)
                    //     }
                    // )
                    Text("Pantalla en desarrollo")
                }
                
                composable(Screen.SalesReports.route) {
                    // SalesReportsScreen(
                    //     onNavigateBack = {
                    //         navController.popBackStack()
                    //     }
                    // )
                    Text("Pantalla en desarrollo")
                }
                
                composable(Screen.AppointmentsReports.route) {
                    // TODO: Create AppointmentsReportsScreen
                    // ReportsMainScreen(
                    //     onNavigateBack = {
                    //         navController.popBackStack()
                    //     },
                    //     onNavigateToSalesReports = {
                    //         navController.navigate(Screen.SalesReports.route)
                    //     },
                    //     onNavigateToAppointmentsReports = {
                    //         navController.navigate(Screen.AppointmentsReports.route)
                    //     },
                    //     onNavigateToPatientsReports = {
                    //         navController.navigate(Screen.PatientsReports.route)
                    //     },
                    //     onNavigateToInventoryReports = {
                    //         navController.navigate(Screen.InventoryReports.route)
                    //     },
                    //     onNavigateToClinicalReports = {
                    //         navController.navigate(Screen.ClinicalReports.route)
                    //     }
                    // )
                    Text("Pantalla en desarrollo")
                }
                
                composable(Screen.PatientsReports.route) {
                    // TODO: Create PatientsReportsScreen
                    // ReportsMainScreen(
                    //     onNavigateBack = {
                    //         navController.popBackStack()
                    //     },
                    //     onNavigateToSalesReports = {
                    //         navController.navigate(Screen.SalesReports.route)
                    //     },
                    //     onNavigateToAppointmentsReports = {
                    //         navController.navigate(Screen.AppointmentsReports.route)
                    //     },
                    //     onNavigateToPatientsReports = {
                    //         navController.navigate(Screen.PatientsReports.route)
                    //     },
                    //     onNavigateToInventoryReports = {
                    //         navController.navigate(Screen.InventoryReports.route)
                    //     },
                    //     onNavigateToClinicalReports = {
                    //         navController.navigate(Screen.ClinicalReports.route)
                    //     }
                    // )
                    Text("Pantalla en desarrollo")
                }
                
                composable(Screen.InventoryReports.route) {
                    // TODO: Create InventoryReportsScreen
                    // ReportsMainScreen(
                    //     onNavigateBack = {
                    //         navController.popBackStack()
                    //     },
                    //     onNavigateToSalesReports = {
                    //         navController.navigate(Screen.SalesReports.route)
                    //     },
                    //     onNavigateToAppointmentsReports = {
                    //         navController.navigate(Screen.AppointmentsReports.route)
                    //     },
                    //     onNavigateToPatientsReports = {
                    //         navController.navigate(Screen.PatientsReports.route)
                    //     },
                    //     onNavigateToInventoryReports = {
                    //         navController.navigate(Screen.InventoryReports.route)
                    //     },
                    //     onNavigateToClinicalReports = {
                    //         navController.navigate(Screen.ClinicalReports.route)
                    //     }
                    // )
                    Text("Pantalla en desarrollo")
                }
                
                composable(Screen.ClinicalReports.route) {
                    // TODO: Create ClinicalReportsScreen
                    // ReportsMainScreen(
                    //     onNavigateBack = {
                    //         navController.popBackStack()
                    //     },
                    //     onNavigateToSalesReports = {
                    //         navController.navigate(Screen.SalesReports.route)
                    //     },
                    //     onNavigateToAppointmentsReports = {
                    //         navController.navigate(Screen.AppointmentsReports.route)
                    //     },
                    //     onNavigateToPatientsReports = {
                    //         navController.navigate(Screen.PatientsReports.route)
                    //     },
                    //     onNavigateToInventoryReports = {
                    //         navController.navigate(Screen.InventoryReports.route)
                    //     },
                    //     onNavigateToClinicalReports = {
                    //         navController.navigate(Screen.ClinicalReports.route)
                    //     }
                    // )
                    Text("Pantalla en desarrollo")
                }
                
                composable(Screen.Help.route) {
                    HelpScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable(Screen.About.route) {
                    AboutScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Screen.Admin.route) {
                    AdminScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToUserApproval = {
                            navController.navigate(Screen.UserApproval.route)
                        }
                    )
                }
                
                composable(Screen.UserApproval.route) {
                    UserApprovalScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable(Screen.FirebaseTest.route) {
                    FirebaseTestScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
    }
}

