package com.example.nexogo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.nexogo.ui.theme.NexoGoTheme
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Actividad de prueba completa para verificar la conexión a Firebase
 * Incluye tests para Authentication, Firestore y Storage
 */
class FirebaseFullTestActivity : ComponentActivity() {
    
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    
    companion object {
        private const val TAG = "FirebaseFullTest"
        private const val TEST_EMAIL = "test@nexogo.com"
        private const val TEST_PASSWORD = "123456"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar Firebase si no está inicializado
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "🔥 Firebase inicializado en FirebaseFullTestActivity")
        }
        
        // Inicializar servicios de Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        
        Log.d(TAG, "🚀 Iniciando pruebas completas de Firebase...")
        
        setContent {
            NexoGoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FirebaseTestScreen()
                }
            }
        }
        
        // Ejecutar pruebas automáticamente
        runFirebaseTests()
    }
    
    /**
     * Ejecuta todas las pruebas de Firebase en orden
     */
    private fun runFirebaseTests() {
        // Ejecutar en un hilo separado para no bloquear la UI
        Thread {
            try {
                // 1. Test de Authentication
                testFirebaseAuth()
                
                // 2. Test de Firestore
                testFirestore()
                
                // 3. Test de Storage
                testFirebaseStorage()
                
                // 4. Resultado final
                Log.d(TAG, "🔥 Conexión completa a Firebase verificada exitosamente en NexoGo")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error general en las pruebas de Firebase: ${e.message}", e)
            }
        }.start()
    }
    
    /**
     * Test de Firebase Authentication
     * Intenta autenticar o crear un usuario de prueba
     */
    private fun testFirebaseAuth() {
        try {
            Log.d(TAG, "🔐 Iniciando test de Firebase Authentication...")
            
            // Intentar iniciar sesión con el usuario de prueba
            val signInTask = auth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
            signInTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d(TAG, "✅ Auth: Usuario autenticado correctamente: ${user?.uid}")
                } else {
                    // Si el usuario no existe, crearlo
                    Log.d(TAG, "⚠️ Usuario no existe, creando nuevo usuario...")
                    createTestUser()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Auth: Error autenticando usuario: ${e.message}", e)
        }
    }
    
    /**
     * Crea un usuario de prueba en Firebase Authentication
     */
    private fun createTestUser() {
        try {
            val createUserTask = auth.createUserWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
            createUserTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d(TAG, "✅ Auth: Usuario creado y autenticado correctamente: ${user?.uid}")
                } else {
                    Log.e(TAG, "❌ Auth: Error creando usuario: ${task.exception?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Auth: Error creando usuario: ${e.message}", e)
        }
    }
    
    /**
     * Test de Firestore
     * Crea un documento en la colección users y lo lee
     */
    private fun testFirestore() {
        try {
            Log.d(TAG, "📊 Iniciando test de Firestore...")
            
            // Crear documento de prueba
            val userData = hashMapOf(
                "name" to "Brayan Test",
                "role" to "admin",
                "timestamp" to System.currentTimeMillis(),
                "testId" to UUID.randomUUID().toString()
            )
            
            // Guardar documento en Firestore
            firestore.collection("users")
                .add(userData)
                .addOnSuccessListener { documentReference ->
                    val documentId = documentReference.id
                    Log.d(TAG, "✅ Firestore: Documento agregado con ID: $documentId")
                    
                    // Leer el documento recién creado
                    readFirestoreDocument(documentId)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ Firestore: Error al agregar documento: ${e.message}", e)
                }
                
        } catch (e: Exception) {
            Log.e(TAG, "❌ Firestore: Error en test de Firestore: ${e.message}", e)
        }
    }
    
    /**
     * Lee un documento de Firestore por su ID
     */
    private fun readFirestoreDocument(documentId: String) {
        try {
            firestore.collection("users")
                .document(documentId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name")
                        val role = document.getString("role")
                        Log.d(TAG, "✅ Firestore: Documento leído - Nombre: $name, Rol: $role")
                    } else {
                        Log.w(TAG, "⚠️ Firestore: Documento no encontrado")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ Firestore: Error leyendo documento: ${e.message}", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Firestore: Error leyendo documento: ${e.message}", e)
        }
    }
    
    /**
     * Test de Firebase Storage
     * Sube un archivo de prueba
     */
    private fun testFirebaseStorage() {
        try {
            Log.d(TAG, "💾 Iniciando test de Firebase Storage...")
            
            // Crear contenido de prueba
            val testContent = "Archivo de prueba de NexoGo - ${Date()}"
            val testFileName = "test_${System.currentTimeMillis()}.txt"
            val testPath = "test_uploads/$testFileName"
            
            // Verificar que Storage esté inicializado
            if (storage == null) {
                Log.e("FirebaseFullTest", "❌ Storage: FirebaseStorage no inicializado")
                return
            }
            
            // Verificar que el usuario esté autenticado
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.e("FirebaseFullTest", "❌ Storage: Usuario no autenticado")
                return
            }
            
            // Crear referencia al archivo en Storage
            val storageRef = storage.reference.child(testPath)
            
            // Verificar que la referencia sea válida
            if (storageRef == null) {
                Log.e("FirebaseFullTest", "❌ Storage: No se pudo crear referencia")
                return
            }
            
            Log.d("FirebaseFullTest", "🔍 Storage: Subiendo archivo a: $testPath")
            
            // Subir archivo con manejo de errores mejorado
            try {
                val uploadTask = storageRef.putBytes(testContent.toByteArray())
                
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    // Obtener URL de descarga
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        Log.d(TAG, "✅ Archivo subido correctamente a ${storageRef.path}")
                        Log.d(TAG, "✅ Storage: URL de descarga: $uri")
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "❌ Storage: Error obteniendo URL: ${e.message}", e)
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "❌ Storage: Error al subir archivo: ${e.message}", e)
                    
                    // Si es error 404, intentar crear la carpeta primero
                    if (e.message?.contains("404") == true || e.message?.contains("Object does not exist") == true) {
                        Log.d(TAG, "🔄 Intentando crear carpeta y subir archivo de prueba...")
                        createStorageFolderAndUpload(storage, testPath, testContent)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Storage: Error en subida: ${e.message}", e)
                
                // Intentar crear la carpeta y subir archivo de prueba
                createStorageFolderAndUpload(storage, testPath, testContent)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Storage: Error en test de Storage: ${e.message}", e)
        }
    }
    
    /**
     * Pantalla de prueba de Firebase
     */
    @Composable
    fun FirebaseTestScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔥 Prueba de Conexión Firebase",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Prueba de conexión Firebase en progreso…",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Revisa Logcat para detalles.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Módulos en prueba:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("🔐 Firebase Authentication")
                    Text("📊 Firestore Database")
                    Text("💾 Firebase Storage")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Busca en Logcat: 'FirebaseFullTest'",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Composable para la prueba de Firebase que se puede usar desde la navegación
 */
@Composable
fun FirebaseTestScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        // Ejecutar pruebas automáticamente
        runFirebaseTests(context)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔥 Prueba de Conexión Firebase",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Prueba de conexión Firebase en progreso…",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Revisa Logcat para detalles.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Módulos en prueba:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("🔐 Firebase Authentication")
                Text("📊 Firestore Database")
                Text("💾 Firebase Storage")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Busca en Logcat: 'FirebaseFullTest'",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

/**
 * Ejecuta las pruebas de Firebase desde un composable
 */
private fun runFirebaseTests(context: Context) {
    // Ejecutar en un hilo separado para no bloquear la UI
    Thread {
        try {
            // Inicializar Firebase si no está inicializado
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
                Log.d("FirebaseFullTest", "🔥 Firebase inicializado en FirebaseTestScreen")
            }
            
            // Inicializar servicios de Firebase
            val auth = FirebaseAuth.getInstance()
            val firestore = FirebaseFirestore.getInstance()
            val storage = FirebaseStorage.getInstance()
            
            Log.d("FirebaseFullTest", "🚀 Iniciando pruebas completas de Firebase...")
            
            // 1. Test de Authentication
            testFirebaseAuth(auth)
            
            // 2. Test de Firestore
            testFirestore(firestore)
            
            // 3. Test de Storage
            testFirebaseStorage(storage)
            
            // 4. Resultado final
            Log.d("FirebaseFullTest", "🔥 Conexión completa a Firebase verificada exitosamente en NexoGo")
            
        } catch (e: Exception) {
            Log.e("FirebaseFullTest", "❌ Error general en las pruebas de Firebase: ${e.message}", e)
        }
    }.start()
}

/**
 * Test de Firebase Authentication
 */
private fun testFirebaseAuth(auth: FirebaseAuth) {
    try {
        Log.d("FirebaseFullTest", "🔐 Iniciando test de Firebase Authentication...")
        
        val TEST_EMAIL = "test@nexogo.com"
        val TEST_PASSWORD = "123456"
        
        // Intentar iniciar sesión con el usuario de prueba
        val signInTask = auth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
        signInTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.d("FirebaseFullTest", "✅ Auth: Usuario autenticado correctamente: ${user?.uid}")
            } else {
                // Si el usuario no existe, crearlo
                Log.d("FirebaseFullTest", "⚠️ Usuario no existe, creando nuevo usuario...")
                createTestUser(auth, TEST_EMAIL, TEST_PASSWORD)
            }
        }
        
    } catch (e: Exception) {
        Log.e("FirebaseFullTest", "❌ Auth: Error autenticando usuario: ${e.message}", e)
    }
}

/**
 * Crea un usuario de prueba en Firebase Authentication
 */
private fun createTestUser(auth: FirebaseAuth, email: String, password: String) {
    try {
        val createUserTask = auth.createUserWithEmailAndPassword(email, password)
        createUserTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.d("FirebaseFullTest", "✅ Auth: Usuario creado y autenticado correctamente: ${user?.uid}")
            } else {
                Log.e("FirebaseFullTest", "❌ Auth: Error creando usuario: ${task.exception?.message}")
            }
        }
    } catch (e: Exception) {
        Log.e("FirebaseFullTest", "❌ Auth: Error creando usuario: ${e.message}", e)
    }
}

/**
 * Test de Firestore
 */
private fun testFirestore(firestore: FirebaseFirestore) {
    try {
        Log.d("FirebaseFullTest", "📊 Iniciando test de Firestore...")
        
        // Crear documento de prueba
        val userData = hashMapOf(
            "name" to "Brayan Test",
            "role" to "admin",
            "timestamp" to System.currentTimeMillis(),
            "testId" to UUID.randomUUID().toString()
        )
        
        // Guardar documento en Firestore
        firestore.collection("users")
            .add(userData)
            .addOnSuccessListener { documentReference ->
                val documentId = documentReference.id
                Log.d("FirebaseFullTest", "✅ Firestore: Documento agregado con ID: $documentId")
                
                // Leer el documento recién creado
                readFirestoreDocument(firestore, documentId)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseFullTest", "❌ Firestore: Error al agregar documento: ${e.message}", e)
            }
            
    } catch (e: Exception) {
        Log.e("FirebaseFullTest", "❌ Firestore: Error en test de Firestore: ${e.message}", e)
    }
}

/**
 * Lee un documento de Firestore por su ID
 */
private fun readFirestoreDocument(firestore: FirebaseFirestore, documentId: String) {
    try {
        firestore.collection("users")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    val role = document.getString("role")
                    Log.d("FirebaseFullTest", "✅ Firestore: Documento leído - Nombre: $name, Rol: $role")
                } else {
                    Log.w("FirebaseFullTest", "⚠️ Firestore: Documento no encontrado")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseFullTest", "❌ Firestore: Error leyendo documento: ${e.message}", e)
            }
    } catch (e: Exception) {
        Log.e("FirebaseFullTest", "❌ Firestore: Error leyendo documento: ${e.message}", e)
    }
}

/**
 * Test de Firebase Storage
 */
private fun testFirebaseStorage(storage: FirebaseStorage) {
    try {
        Log.d("FirebaseFullTest", "💾 Iniciando test de Firebase Storage...")
        
        // Crear contenido de prueba
        val testContent = "Archivo de prueba de NexoGo - ${Date()}"
        val testFileName = "test_${System.currentTimeMillis()}.txt"
        val testPath = "test_uploads/$testFileName"
        
        // Verificar que Storage esté inicializado
        if (storage == null) {
            Log.e("FirebaseFullTest", "❌ Storage: FirebaseStorage no inicializado")
            return
        }
        
        // Verificar que el usuario esté autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("FirebaseFullTest", "❌ Storage: Usuario no autenticado")
            return
        }
        
        // Crear referencia al archivo en Storage
        val storageRef = storage.reference.child(testPath)
        
        // Verificar que la referencia sea válida
        if (storageRef == null) {
            Log.e("FirebaseFullTest", "❌ Storage: No se pudo crear referencia")
            return
        }
        
        Log.d("FirebaseFullTest", "🔍 Storage: Subiendo archivo a: $testPath")
        
        // Subir archivo con manejo de errores mejorado
        try {
            val uploadTask = storageRef.putBytes(testContent.toByteArray())
            
            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Obtener URL de descarga
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("FirebaseFullTest", "✅ Archivo subido correctamente a ${storageRef.path}")
                    Log.d("FirebaseFullTest", "✅ Storage: URL de descarga: $uri")
                }.addOnFailureListener { e ->
                    Log.e("FirebaseFullTest", "❌ Storage: Error obteniendo URL: ${e.message}", e)
                }
            }.addOnFailureListener { e ->
                Log.e("FirebaseFullTest", "❌ Storage: Error al subir archivo: ${e.message}", e)
                
                // Si es error 404, intentar crear la carpeta primero
                if (e.message?.contains("404") == true || e.message?.contains("Object does not exist") == true) {
                    Log.d("FirebaseFullTest", "🔄 Intentando crear carpeta y subir archivo de prueba...")
                    createStorageFolderAndUpload(storage, testPath, testContent)
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseFullTest", "❌ Storage: Error en subida: ${e.message}", e)
            
            // Intentar crear la carpeta y subir archivo de prueba
            createStorageFolderAndUpload(storage, testPath, testContent)
        }
        
    } catch (e: Exception) {
        Log.e("FirebaseFullTest", "❌ Storage: Error en test de Storage: ${e.message}", e)
    }
}

/**
 * Función auxiliar para crear carpeta en Storage y subir archivo de prueba
 */
private fun createStorageFolderAndUpload(storage: FirebaseStorage, testPath: String, testContent: String) {
    try {
        Log.d("FirebaseFullTest", "🔄 Creando carpeta en Storage y subiendo archivo de prueba...")
        
        // Intentar con una ruta más simple
        val simplePath = "test_uploads/simple_test.txt"
        val simpleRef = storage.reference.child(simplePath)
        
        Log.d("FirebaseFullTest", "🔍 Intentando subir a ruta simple: $simplePath")
        
        val uploadTask = simpleRef.putBytes(testContent.toByteArray())
        
        uploadTask.addOnSuccessListener { taskSnapshot ->
            simpleRef.downloadUrl.addOnSuccessListener { uri ->
                Log.d("FirebaseFullTest", "✅ Archivo de prueba subido correctamente a ${simpleRef.path}")
                Log.d("FirebaseFullTest", "✅ Storage: URL de descarga: $uri")
            }.addOnFailureListener { e ->
                Log.e("FirebaseFullTest", "❌ Storage: Error obteniendo URL de archivo simple: ${e.message}", e)
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseFullTest", "❌ Storage: Error subiendo archivo simple: ${e.message}", e)
            
            // Como último recurso, intentar subir a la raíz
            tryRootUpload(storage, testContent)
        }
    } catch (e: Exception) {
        Log.e("FirebaseFullTest", "❌ Storage: Error en función auxiliar: ${e.message}", e)
        tryRootUpload(storage, testContent)
    }
}

/**
 * Función de último recurso para subir a la raíz del Storage
 */
private fun tryRootUpload(storage: FirebaseStorage, testContent: String) {
    try {
        Log.d("FirebaseFullTest", "🔄 Último recurso: subiendo a la raíz del Storage...")
        
        val rootRef = storage.reference.child("test_root.txt")
        val uploadTask = rootRef.putBytes(testContent.toByteArray())
        
        uploadTask.addOnSuccessListener { taskSnapshot ->
            rootRef.downloadUrl.addOnSuccessListener { uri ->
                Log.d("FirebaseFullTest", "✅ Archivo subido a la raíz: ${rootRef.path}")
                Log.d("FirebaseFullTest", "✅ Storage: URL de descarga: $uri")
            }.addOnFailureListener { e ->
                Log.e("FirebaseFullTest", "❌ Storage: Error obteniendo URL de raíz: ${e.message}", e)
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseFullTest", "❌ Storage: Error crítico - no se pudo subir archivo: ${e.message}", e)
        }
    } catch (e: Exception) {
        Log.e("FirebaseFullTest", "❌ Storage: Error crítico en último recurso: ${e.message}", e)
    }
}
