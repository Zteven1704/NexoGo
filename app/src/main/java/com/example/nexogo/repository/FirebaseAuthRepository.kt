package com.example.nexogo.repository

import com.example.nexogo.core.models.User
import com.example.nexogo.core.models.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    init {
        // Crear usuario administrador automáticamente si no existe
        createAdminUserIfNeeded()
    }
    
    private fun createAdminUserIfNeeded() {
        // Verificar si el usuario admin existe en Firestore
        firestore.collection("usuarios")
            .whereEqualTo("correo", "admin@nexogo.com")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result?.documents
                    if (documents.isNullOrEmpty()) {
                        println("DEBUG: FirebaseAuthRepository - Usuario admin no encontrado en Firestore, creando...")
                        createAdminUser()
                    } else {
                        println("DEBUG: FirebaseAuthRepository - Usuario admin ya existe en Firestore")
                    }
                } else {
                    println("DEBUG: FirebaseAuthRepository - Error verificando usuario admin en Firestore: ${task.exception?.message}")
                    // Si hay error, intentar crear de todas formas
                    createAdminUser()
                }
            }
    }
    
    private fun createAdminUser() {
        println("DEBUG: FirebaseAuthRepository - Creando usuario administrador...")
        
        // Primero verificar si ya existe en Auth
        auth.fetchSignInMethodsForEmail("admin@nexogo.com")
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val signInMethods = authTask.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        // No existe en Auth, crearlo
                        auth.createUserWithEmailAndPassword("admin@nexogo.com", "123456")
                            .addOnCompleteListener { createTask ->
                                if (createTask.isSuccessful) {
                                    println("DEBUG: FirebaseAuthRepository - Usuario administrador creado en Auth")
                                    createAdminUserDocument(createTask.result?.user?.uid)
                                } else {
                                    println("DEBUG: FirebaseAuthRepository - Error creando usuario admin en Auth: ${createTask.exception?.message}")
                                }
                            }
                    } else {
                        // Ya existe en Auth, solo crear/actualizar documento en Firestore
                        println("DEBUG: FirebaseAuthRepository - Usuario admin ya existe en Auth, creando documento en Firestore")
                        auth.signInWithEmailAndPassword("admin@nexogo.com", "123456")
                            .addOnCompleteListener { signInTask ->
                                if (signInTask.isSuccessful) {
                                    createAdminUserDocument(signInTask.result?.user?.uid)
                                } else {
                                    println("DEBUG: FirebaseAuthRepository - Error iniciando sesión como admin: ${signInTask.exception?.message}")
                                }
                            }
                    }
                } else {
                    println("DEBUG: FirebaseAuthRepository - Error verificando usuario admin en Auth: ${authTask.exception?.message}")
                }
            }
    }
    
    private fun createAdminUserDocument(uid: String?) {
        if (uid != null) {
            val adminData = mapOf(
                "uid" to uid,
                "nombre" to "Dr. María González",
                "correo" to "admin@nexogo.com",
                "telefono" to "1234567890",
                "whatsapp" to "1234567890",
                "rol" to "ADMIN",
                "estado" to "aprobado",
                "isApproved" to true,
                "fechaRegistro" to com.google.firebase.Timestamp.now()
            )
            
            firestore.collection("usuarios").document(uid)
                .set(adminData)
                .addOnSuccessListener {
                    println("DEBUG: FirebaseAuthRepository - Documento de admin creado/actualizado en Firestore")
                }
                .addOnFailureListener { e ->
                    println("DEBUG: FirebaseAuthRepository - Error creando documento de admin: ${e.message}")
                }
        }
    }
    
    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        role: UserRole,
        phone: String = "",
        whatsapp: String = ""
    ): Result<User> {
        return try {
            println("DEBUG: FirebaseAuthRepository - Iniciando registro para $email")
            
            // Crear usuario en Firebase Auth
            println("DEBUG: FirebaseAuthRepository - Creando usuario en Firebase Auth...")
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Error al crear usuario")
            println("DEBUG: FirebaseAuthRepository - Usuario creado en Auth: ${firebaseUser.uid}")
            
            // Obtener token FCM
            println("DEBUG: FirebaseAuthRepository - Obteniendo token FCM...")
            val fcmToken = try {
                FirebaseMessaging.getInstance().token.await()
            } catch (e: Exception) {
                println("DEBUG: FirebaseAuthRepository - Error obteniendo FCM token: ${e.message}")
                null
            }
            println("DEBUG: FirebaseAuthRepository - FCM token obtenido: ${fcmToken != null}")
            
            // Crear documento de usuario en Firestore
            println("DEBUG: FirebaseAuthRepository - Creando objeto User...")
            val user = User(
                id = firebaseUser.uid,
                name = name,
                email = email,
                role = role,
                isApproved = false, // Todos los usuarios requieren aprobación
                fcmToken = fcmToken,
                phone = phone,
                whatsapp = whatsapp,
                profileImageUrl = getDefaultProfileImage(role)
            )
            println("DEBUG: FirebaseAuthRepository - Objeto User creado: ${user.name}")
            
            // Guardar en Firestore
            println("DEBUG: FirebaseAuthRepository - Guardando en Firestore...")
            firestore.collection("usuarios").document(firebaseUser.uid)
                .set(mapOf(
                    "uid" to firebaseUser.uid,
                    "nombre" to name,
                    "correo" to email,
                    "telefono" to phone,
                    "whatsapp" to whatsapp,
                    "rol" to role.name,
                    "estado" to "pendiente",
                    "token" to fcmToken,
                    "fechaRegistro" to com.google.firebase.Timestamp.now(),
                    "isApproved" to false
                )).await()
            println("DEBUG: FirebaseAuthRepository - Usuario guardado en Firestore")
            
            // Enviar notificación al administrador
            println("DEBUG: FirebaseAuthRepository - Enviando notificación al admin...")
            sendAdminNotification(user)
            println("DEBUG: FirebaseAuthRepository - Notificación enviada")
            
            println("DEBUG: FirebaseAuthRepository - Registro completado exitosamente")
            Result.success(user)
        } catch (e: Exception) {
            println("DEBUG: FirebaseAuthRepository - Error en registro: ${e.message}")
            println("DEBUG: FirebaseAuthRepository - Stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
    
    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            println("DEBUG: FirebaseAuthRepository - Iniciando login para: $email")
            
            // Autenticar con Firebase Auth
            println("DEBUG: FirebaseAuthRepository - Autenticando con Firebase Auth...")
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Error al autenticar")
            println("DEBUG: FirebaseAuthRepository - Usuario autenticado en Auth: ${firebaseUser.uid}")
            
            // Obtener datos del usuario desde Firestore
            println("DEBUG: FirebaseAuthRepository - Buscando usuario en Firestore con UID: ${firebaseUser.uid}")
            val doc = firestore.collection("usuarios").document(firebaseUser.uid).get().await()
            
            if (!doc.exists()) {
                println("DEBUG: FirebaseAuthRepository - Documento no existe en Firestore para UID: ${firebaseUser.uid}")
                // Intentar buscar por email como alternativa
                println("DEBUG: FirebaseAuthRepository - Buscando por email: $email")
                val emailQuery = firestore.collection("usuarios")
                    .whereEqualTo("correo", email)
                    .get().await()
                
                if (emailQuery.documents.isNotEmpty()) {
                    val emailDoc = emailQuery.documents.first()
                    println("DEBUG: FirebaseAuthRepository - Usuario encontrado por email, actualizando UID...")
                    // Actualizar el documento con el UID correcto
                    emailDoc.reference.update("uid", firebaseUser.uid)
                    // Usar el documento encontrado
                    val data = emailDoc.data ?: throw Exception("Datos de usuario no disponibles")
                    val isApproved = data["isApproved"] as? Boolean ?: false
                    val rol = data["rol"] as? String ?: "USER"
                    
                    if (!isApproved) {
                        throw Exception("Tu cuenta está pendiente de aprobación por el administrador.")
                    }
                    
                    val user = User(
                        id = firebaseUser.uid,
                        name = data["nombre"] as? String ?: "Usuario",
                        email = data["correo"] as? String ?: email,
                        role = UserRole.valueOf(rol),
                        isApproved = isApproved,
                        fcmToken = data["token"] as? String,
                        phone = data["telefono"] as? String ?: "",
                        whatsapp = data["whatsapp"] as? String ?: "",
                        profileImageUrl = data["profileImageUrl"] as? String ?: getDefaultProfileImage(UserRole.valueOf(rol))
                    )
                    
                    return Result.success(user)
                } else {
                    throw Exception("Usuario no encontrado en la base de datos")
                }
            }
            
            val data = doc.data ?: throw Exception("Datos de usuario no disponibles")
            val isApproved = data["isApproved"] as? Boolean ?: false
            val rol = data["rol"] as? String ?: "USER"
            
            // Verificar que el usuario esté aprobado
            if (!isApproved) {
                throw Exception("Tu cuenta está pendiente de aprobación por el administrador.")
            }
            
            // Crear objeto User
            val user = User(
                id = firebaseUser.uid,
                name = data["nombre"] as? String ?: "Usuario",
                email = data["correo"] as? String ?: email,
                role = UserRole.valueOf(rol),
                isApproved = isApproved,
                fcmToken = data["token"] as? String,
                phone = data["telefono"] as? String ?: "",
                whatsapp = data["whatsapp"] as? String ?: "",
                profileImageUrl = data["profileImageUrl"] as? String ?: getDefaultProfileImage(UserRole.valueOf(rol))
            )
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getPendingUsers(): Result<List<User>> {
        return try {
            println("DEBUG: FirebaseAuthRepository - Buscando usuarios con isApproved = false...")
            val snapshot = firestore.collection("usuarios")
                .whereEqualTo("isApproved", false)
                .get().await()
            
            println("DEBUG: FirebaseAuthRepository - Documentos encontrados: ${snapshot.documents.size}")
            snapshot.documents.forEach { doc ->
                val data = doc.data
                println("DEBUG: FirebaseAuthRepository - Documento: ${doc.id}")
                println("  - isApproved: ${data?.get("isApproved")}")
                println("  - rol: ${data?.get("rol")}")
                println("  - nombre: ${data?.get("nombre")}")
                println("  - correo: ${data?.get("correo")}")
            }
            
            val users = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val rol = data["rol"] as? String ?: "USER"
                val isApproved = data["isApproved"] as? Boolean ?: false
                
                println("DEBUG: FirebaseAuthRepository - Procesando usuario: ${data["nombre"]} - Rol: $rol - Aprobado: $isApproved")
                
                User(
                    id = data["uid"] as? String ?: doc.id,
                    name = data["nombre"] as? String ?: "",
                    email = data["correo"] as? String ?: "",
                    role = try {
                        UserRole.valueOf(rol)
                    } catch (e: Exception) {
                        println("DEBUG: FirebaseAuthRepository - Error parseando rol '$rol': ${e.message}")
                        UserRole.USER
                    },
                    isApproved = isApproved,
                    fcmToken = data["token"] as? String,
                    phone = data["telefono"] as? String ?: "",
                    whatsapp = data["whatsapp"] as? String ?: "",
                    profileImageUrl = data["profileImageUrl"] as? String ?: getDefaultProfileImage(UserRole.valueOf(rol))
                )
            }
            
            println("DEBUG: FirebaseAuthRepository - Usuarios pendientes procesados: ${users.size}")
            Result.success(users)
        } catch (e: Exception) {
            println("DEBUG: FirebaseAuthRepository - Error en getPendingUsers: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun approveUser(userId: String): Result<Unit> {
        return try {
            firestore.collection("usuarios").document(userId)
                .update(
                    mapOf(
                        "estado" to "aprobado",
                        "isApproved" to true,
                        "fechaAprobacion" to com.google.firebase.Timestamp.now()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun rejectUser(userId: String): Result<Unit> {
        return try {
            firestore.collection("usuarios").document(userId)
                .update(
                    mapOf(
                        "estado" to "rechazado",
                        "isApproved" to false,
                        "fechaRechazo" to com.google.firebase.Timestamp.now()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            println("DEBUG: FirebaseAuthRepository - Actualizando perfil del usuario: ${user.name}")
            
            // Actualizar en Firestore
            firestore.collection("usuarios").document(user.id)
                .update(
                    mapOf(
                        "nombre" to user.name,
                        "telefono" to user.phone,
                        "whatsapp" to user.whatsapp,
                        "profileImageUrl" to user.profileImageUrl,
                        "fechaActualizacion" to com.google.firebase.Timestamp.now()
                    )
                ).await()
            
            println("DEBUG: FirebaseAuthRepository - Perfil actualizado en Firestore exitosamente")
            Result.success(user)
        } catch (e: Exception) {
            println("DEBUG: FirebaseAuthRepository - Error actualizando perfil: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val doc = firestore.collection("usuarios").document(userId).get().await()
            
            if (!doc.exists()) {
                throw Exception("Usuario no encontrado")
            }
            
            val data = doc.data ?: throw Exception("Datos de usuario no disponibles")
            val isApproved = data["isApproved"] as? Boolean ?: false
            
            // Debug específico para el rol
            val rolFromFirestore = data["rol"] as? String
            val roleFromFirestore = data["role"] as? String
            val rolFromFirestore2 = data["Rol"] as? String
            val roleFromFirestore2 = data["Role"] as? String
            
            println("DEBUG: FirebaseAuthRepository - Campos de rol encontrados:")
            println("  - rol: '$rolFromFirestore'")
            println("  - role: '$roleFromFirestore'")
            println("  - Rol: '$rolFromFirestore2'")
            println("  - Role: '$roleFromFirestore2'")
            
            // Intentar obtener el rol de diferentes campos posibles
            val finalRole = rolFromFirestore ?: roleFromFirestore ?: rolFromFirestore2 ?: roleFromFirestore2 ?: "USER"
            println("DEBUG: FirebaseAuthRepository - Rol final seleccionado: '$finalRole'")
            
            val user = User(
                id = data["uid"] as? String ?: userId,
                name = data["nombre"] as? String ?: "Usuario",
                email = data["correo"] as? String ?: "",
                role = try {
                    UserRole.valueOf(finalRole)
                } catch (e: Exception) {
                    println("DEBUG: FirebaseAuthRepository - Error parseando rol '$finalRole': ${e.message}")
                    UserRole.USER
                },
                isApproved = isApproved,
                fcmToken = data["token"] as? String,
                phone = data["telefono"] as? String ?: "",
                whatsapp = data["whatsapp"] as? String ?: "",
                profileImageUrl = data["profileImageUrl"] as? String ?: getDefaultProfileImage(UserRole.valueOf(finalRole))
            )
            
            println("DEBUG: FirebaseAuthRepository - Usuario creado con rol: ${user.role}")
            
            // Si es admin@nexogo.com pero no tiene rol ADMIN, corregirlo
            if (user.email == "admin@nexogo.com" && user.role != UserRole.ADMIN) {
                println("DEBUG: FirebaseAuthRepository - Admin detectado con rol incorrecto, corrigiendo...")
                try {
                    val updates = hashMapOf<String, Any>(
                        "rol" to "ADMIN",
                        "role" to "ADMIN",
                        "isApproved" to true,
                        "isProfessional" to false,
                        "fechaActualizacion" to com.google.firebase.Timestamp.now()
                    )
                    
                    firestore.collection("usuarios").document(userId).update(updates).await()
                    println("DEBUG: FirebaseAuthRepository - Rol de admin corregido exitosamente")
                    
                    // Crear usuario con rol corregido
                    val correctedUser = user.copy(role = UserRole.ADMIN)
                    return Result.success(correctedUser)
                } catch (e: Exception) {
                    println("DEBUG: FirebaseAuthRepository - Error corrigiendo rol de admin: ${e.message}")
                    // Continuar con el usuario original si falla la corrección
                }
            }
            
            // Para usuarios normales, asegurar que NO sean ADMIN a menos que sea admin@nexogo.com
            if (user.email != "admin@nexogo.com" && user.role == UserRole.ADMIN) {
                println("DEBUG: FirebaseAuthRepository - Usuario normal detectado con rol ADMIN incorrecto, corrigiendo a USER...")
                try {
                    val updates = hashMapOf<String, Any>(
                        "rol" to "USER",
                        "role" to "USER",
                        "fechaActualizacion" to com.google.firebase.Timestamp.now()
                    )
                    
                    firestore.collection("usuarios").document(userId).update(updates).await()
                    println("DEBUG: FirebaseAuthRepository - Rol de usuario normal corregido a USER")
                    
                    // Crear usuario con rol corregido
                    val correctedUser = user.copy(role = UserRole.USER)
                    return Result.success(correctedUser)
                } catch (e: Exception) {
                    println("DEBUG: FirebaseAuthRepository - Error corrigiendo rol de usuario normal: ${e.message}")
                    // Continuar con el usuario original si falla la corrección
                }
            }
            
            // Para usuarios VET y VET_ASSISTANT, asegurar que NO sean ADMIN
            val originalRoleFromFirestore = data["rol"] as? String ?: "USER"
            if (user.email != "admin@nexogo.com" && user.role == UserRole.ADMIN && (originalRoleFromFirestore == "VET" || originalRoleFromFirestore == "VET_ASSISTANT")) {
                println("DEBUG: FirebaseAuthRepository - Usuario profesional detectado con rol ADMIN incorrecto, corrigiendo...")
                try {
                    val correctedRole = when (originalRoleFromFirestore) {
                        "VET" -> UserRole.VET
                        "VET_ASSISTANT" -> UserRole.VET_ASSISTANT
                        else -> UserRole.USER
                    }
                    
                    val updates = hashMapOf<String, Any>(
                        "rol" to correctedRole.name,
                        "role" to correctedRole.name,
                        "fechaActualizacion" to com.google.firebase.Timestamp.now()
                    )
                    
                    firestore.collection("usuarios").document(userId).update(updates).await()
                    println("DEBUG: FirebaseAuthRepository - Rol de usuario profesional corregido a ${correctedRole.name}")
                    
                    // Crear usuario con rol corregido
                    val correctedUser = user.copy(role = correctedRole)
                    return Result.success(correctedUser)
                } catch (e: Exception) {
                    println("DEBUG: FirebaseAuthRepository - Error corrigiendo rol de usuario profesional: ${e.message}")
                    // Continuar con el usuario original si falla la corrección
                }
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentUser() = auth.currentUser
    
    private suspend fun sendAdminNotification(user: User) {
        try {
            firestore.collection("notificaciones_admin").add(mapOf(
                "tipo" to "nuevo_usuario",
                "usuarioId" to user.id,
                "usuarioNombre" to user.name,
                "usuarioEmail" to user.email,
                "usuarioRol" to user.role.name,
                "fecha" to com.google.firebase.Timestamp.now(),
                "leido" to false
            )).await()
        } catch (e: Exception) {
            // Log error but don't fail registration
            println("Error enviando notificación al admin: ${e.message}")
        }
    }
    
    private fun getDefaultProfileImage(role: UserRole): String {
        return when (role) {
            UserRole.ADMIN -> "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=150&h=150&fit=crop&crop=face"
            UserRole.VET -> "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?w=150&h=150&fit=crop&crop=face"
            UserRole.VET_ASSISTANT -> "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=150&h=150&fit=crop&crop=face"
            UserRole.USER -> "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&h=150&fit=crop&crop=face"
        }
    }
}