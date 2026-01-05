package com.example.nexogo.navigation

sealed class Screen(val route: String) {
    // Authentication
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ModernLogin : Screen("modern_login")
    object UltraSimpleLogin : Screen("ultra_simple_login")
    object MinimalLogin : Screen("minimal_login")
    
    // Main screens
    object Home : Screen("home")
    object Dashboard : Screen("dashboard")
    object Profile : Screen("profile")
    object Appointments : Screen("appointments")
    object CreateAppointment : Screen("create_appointment")
    object EditAppointment : Screen("edit_appointment/{appointmentId}") {
        fun createRoute(appointmentId: String) = "edit_appointment/$appointmentId"
    }
    object Patients : Screen("patients")
    object CreateEditPatient : Screen("create_edit_patient")
    object ClinicalRecords : Screen("clinical_records")
    object CreateEditClinicalRecord : Screen("create_edit_clinical_record")
    object ViewClinicalRecord : Screen("view_clinical_record")
    
    // History module screens
    object HistoryList : Screen("history_list")
    object HistoryDetail : Screen("history_detail/{recordId}") {
        fun createRoute(recordId: String) = "history_detail/$recordId"
    }
    object HistoryEdit : Screen("history_edit")
    object HistoryPdfPreview : Screen("history_pdf_preview/{recordId}") {
        fun createRoute(recordId: String) = "history_pdf_preview/$recordId"
    }
    object Inventory : Screen("inventory")
    object CreateEditProduct : Screen("create_edit_product?productId={productId}") {
        fun createRoute(productId: String?) = "create_edit_product" + (productId?.let { "?productId=$it" } ?: "")
    }
    object CategoryManagement : Screen("category_management")
    object Sales : Screen("sales")
    object CreateEditSale : Screen("create_edit_sale?saleId={saleId}") {
        fun createRoute(saleId: String?) = "create_edit_sale" + (saleId?.let { "?saleId=$it" } ?: "")
    }
    object ServicesManagement : Screen("services_management")
    object MedicalRecords : Screen("medical_records")
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat")
    object NewChat : Screen("new_chat")
    object ChatConversation : Screen("chat_conversation/{chatId}") {
        fun createRoute(chatId: String) = "chat_conversation/$chatId"
    }
    object ChatbotConfig : Screen("chatbot_config")
    object Settings : Screen("settings")
    object StorageManagement : Screen("storage_management")
    object ProductCategories : Screen("product_categories")
    object SaleDetail : Screen("sale_detail")
    object Reports : Screen("reports")
    object ReportsMain : Screen("reports_main")
    object SalesReports : Screen("sales_reports")
    object AppointmentsReports : Screen("appointments_reports")
    object PatientsReports : Screen("patients_reports")
    object InventoryReports : Screen("inventory_reports")
    object ClinicalReports : Screen("clinical_reports")
    object Help : Screen("help")
    object About : Screen("about")
    
    // Admin screens
    object Admin : Screen("admin")
    object AdminDashboard : Screen("admin_dashboard")
    object UserManagement : Screen("user_management")
    object UserApproval : Screen("user_approval")
    object FirebaseTest : Screen("firebase_test")
    
    // Professional screens
    object ProfessionalDashboard : Screen("professional_dashboard")
    
    // Patient screens
    object PatientDashboard : Screen("patient_dashboard")
}
