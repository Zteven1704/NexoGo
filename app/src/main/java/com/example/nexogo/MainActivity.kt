package com.example.nexogo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.nexogo.navigation.NavGraph
import com.example.nexogo.navigation.Screen
import com.example.nexogo.ui.theme.NexoGoTheme
import com.example.nexogo.core.MockDataGenerator
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.core.firebase.FirebaseDiagnostics
import com.example.nexogo.core.firebase.FirestorePermissionTester
import com.example.nexogo.core.firebase.FirebaseErrorHandler
import com.example.nexogo.core.firebase.SafeFirestoreOperations
import com.example.nexogo.core.firebase.AppointmentDiagnostics
import com.example.nexogo.core.firebase.AppointmentDebugger
import kotlinx.coroutines.launch
import android.util.Log
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Manejo global de excepciones para evitar que la app se cierre
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            FirebaseErrorHandler.handleError(Exception(throwable), "GlobalException")
        }
        
        setContent {
            NexoGoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NexoGoApp()
                }
            }
        }
    }
    
}

@Composable
fun NexoGoApp() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Inicializar datos mock y diagnosticar Firebase al cargar la app
    LaunchedEffect(Unit) {
        try {
            Log.d("NEXOGO_MAIN", "🚀 Iniciando diagnóstico completo de NexoGo...")
            
            // 1. Probar conectividad con Firestore de manera segura
            val connectionTest = SafeFirestoreOperations.safeSet("test", "connection", mapOf("test" to true))
            if (!connectionTest) {
                Log.e("NEXOGO_MAIN", "❌ No se puede conectar a Firestore")
                Log.e("NEXOGO_MAIN", "💡 Aplicar reglas de emergencia en Firebase Console")
                return@LaunchedEffect
            }
            
            // 2. Diagnóstico completo de Firebase
            FirebaseDiagnostics.runFullDiagnostics(context)
            
            // 3. Prueba específica de permisos de Firestore
            val permissionTest = FirestorePermissionTester.testAllCollections()
            if (permissionTest) {
                Log.d("NEXOGO_MAIN", "✅ Permisos de Firestore verificados correctamente")
            } else {
                Log.e("NEXOGO_MAIN", "❌ Problemas de permisos detectados en Firestore")
                Log.e("NEXOGO_MAIN", "💡 Ejecutar: SOLUCION_EMERGENCIA_FIRESTORE.ps1")
            }
            
            // 4. Generar reporte de permisos
            val report = FirestorePermissionTester.generatePermissionReport()
            Log.d("NEXOGO_MAIN", "📊 Reporte de permisos:\n$report")
            
            // 5. Diagnóstico específico de citas
            val appointmentsExist = AppointmentDiagnostics.checkAppointmentsInFirestore()
            if (appointmentsExist) {
                Log.d("NEXOGO_MAIN", "✅ Citas encontradas en Firestore")
            } else {
                Log.w("NEXOGO_MAIN", "⚠️ No se encontraron citas en Firestore")
                Log.d("NEXOGO_MAIN", "🧪 Creando cita de prueba...")
                AppointmentDiagnostics.createTestAppointment()
            }
            
            // 6. Generar reporte de citas
            val appointmentReport = AppointmentDiagnostics.generateAppointmentReport()
            Log.d("NEXOGO_MAIN", "📊 Reporte de citas:\n$appointmentReport")
            
            // 7. Diagnóstico detallado de citas con nuevo debugger
            AppointmentDebugger.debugAppointmentsInFirestore()
            AppointmentDebugger.createTestAppointmentIfNeeded()
            
            // 5. Inicializar datos mock solo si los permisos están OK
            if (permissionTest) {
                Log.d("NEXOGO_MAIN", "🎭 Inicializando datos mock...")
                val repository = FirebaseRepository()
                val mockData = MockDataGenerator(repository)
                mockData.generateAllMockData()
                Log.d("NEXOGO_MAIN", "✅ Datos mock inicializados")
            } else {
                Log.w("NEXOGO_MAIN", "⚠️ Saltando inicialización de datos mock debido a problemas de permisos")
                Log.w("NEXOGO_MAIN", "🔧 Usar SafeFirestoreOperations para operaciones seguras")
            }
            
        } catch (e: Exception) {
            Log.e("NEXOGO_MAIN", "❌ Error en inicialización: ${e.message}", e)
            FirebaseErrorHandler.handleError(e, "MainActivity_Initialization")
        }
    }
    
    // Navegación con SplashScreen como punto de entrada
    NavGraph(
        navController = navController,
        startDestination = Screen.Splash.route
    )
}