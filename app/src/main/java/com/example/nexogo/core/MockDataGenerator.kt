package com.example.nexogo.core

import android.util.Log
import com.google.firebase.Timestamp
import java.util.*

/**
 * Generador de datos mock para NexoGo
 * Crea usuarios, pacientes, citas, inventario, ventas y chat de prueba
 */
class MockDataGenerator(private val repository: FirebaseRepository) {
    
    suspend fun generateAllMockData() {
        Log.d("NEXOGO_MOCK", "🎭 Generando datos mock para NexoGo...")
        
        try {
            // 1. Crear usuarios de prueba
            createTestUsers()
            
            // 2. Crear pacientes de prueba
            createTestPatients()
            
            // 3. Crear citas de prueba
            createTestAppointments()
            
            // 4. Crear inventario de prueba
            createTestInventory()
            
            // 5. Crear ventas de prueba
            createTestSales()
            
            // 6. Crear chat de prueba
            createTestChat()
            
            // 7. Crear historial clínico de prueba
            createTestClinicalHistory()
            
            // 8. Crear configuración
            createTestConfig()
            
            Log.d("NEXOGO_MOCK", "✅ Datos mock generados exitosamente")
        } catch (e: Exception) {
            Log.e("NEXOGO_MOCK", "❌ Error generando datos mock: ${e.message}")
        }
    }
    
    private suspend fun createTestUsers() {
        Log.d("NEXOGO_MOCK", "👥 Creando usuarios de prueba...")
        
        val testUsers = listOf(
            mapOf(
                "uid" to "admin_001",
                "email" to "admin@nexogo.com",
                "name" to "Administrador NexoGo",
                "phone" to "+57 300 123 4567",
                "whatsapp" to "+57 300 123 4567",
                "role" to "ADMIN",
                "isApproved" to true,
                "profileImageUrl" to "",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            ),
            mapOf(
                "uid" to "vet_001",
                "email" to "vet@nexogo.com",
                "name" to "Dr. María González",
                "phone" to "+57 300 234 5678",
                "whatsapp" to "+57 300 234 5678",
                "role" to "VET",
                "isApproved" to true,
                "profileImageUrl" to "",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            ),
            mapOf(
                "uid" to "assistant_001",
                "email" to "assistant@nexogo.com",
                "name" to "Ana López",
                "phone" to "+57 300 345 6789",
                "whatsapp" to "+57 300 345 6789",
                "role" to "ASSISTANT",
                "isApproved" to true,
                "profileImageUrl" to "",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            ),
            mapOf(
                "uid" to "patient_001",
                "email" to "patient@nexogo.com",
                "name" to "Carlos Rodríguez",
                "phone" to "+57 300 456 7890",
                "whatsapp" to "+57 300 456 7890",
                "role" to "PATIENT",
                "isApproved" to true,
                "profileImageUrl" to "",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            ),
            mapOf(
                "uid" to "patient_002",
                "email" to "patient2@nexogo.com",
                "name" to "Laura Martínez",
                "phone" to "+57 300 567 8901",
                "whatsapp" to "+57 300 567 8901",
                "role" to "PATIENT",
                "isApproved" to true,
                "profileImageUrl" to "",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            )
        )
        
        for (user in testUsers) {
            val uid = user["uid"] as String
            repository.createDocument("users", uid, user)
        }
        
        Log.d("NEXOGO_MOCK", "✅ ${testUsers.size} usuarios creados")
    }
    
    private suspend fun createTestPatients() {
        Log.d("NEXOGO_MOCK", "🐾 Creando pacientes de prueba...")
        
        val testPatients = listOf(
            mapOf(
                "id" to "pet_001",
                "ownerId" to "patient_001",
                "ownerName" to "Carlos Rodríguez",
                "name" to "Max",
                "species" to "Perro",
                "breed" to "Labrador",
                "age" to 3,
                "weight" to 25.5,
                "color" to "Dorado",
                "gender" to "Macho",
                "imageUrl" to "",
                "medicalNotes" to "Paciente saludable, vacunas al día",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            ),
            mapOf(
                "id" to "pet_002",
                "ownerId" to "patient_001",
                "ownerName" to "Carlos Rodríguez",
                "name" to "Luna",
                "species" to "Gato",
                "breed" to "Persa",
                "age" to 2,
                "weight" to 4.2,
                "color" to "Blanco",
                "gender" to "Hembra",
                "imageUrl" to "",
                "medicalNotes" to "Gata tranquila, requiere cepillado regular",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            ),
            mapOf(
                "id" to "pet_003",
                "ownerId" to "patient_002",
                "ownerName" to "Laura Martínez",
                "name" to "Bella",
                "species" to "Perro",
                "breed" to "Golden Retriever",
                "age" to 5,
                "weight" to 28.0,
                "color" to "Dorado",
                "gender" to "Hembra",
                "imageUrl" to "",
                "medicalNotes" to "Perra adulta, control de peso necesario",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            ),
            mapOf(
                "id" to "pet_004",
                "ownerId" to "patient_002",
                "ownerName" to "Laura Martínez",
                "name" to "Simba",
                "species" to "Gato",
                "breed" to "Maine Coon",
                "age" to 1,
                "weight" to 3.8,
                "color" to "Naranja",
                "gender" to "Macho",
                "imageUrl" to "",
                "medicalNotes" to "Gatito joven, muy activo",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            )
        )
        
        for (patient in testPatients) {
            val id = patient["id"] as String
            repository.createDocument("patients", id, patient)
        }
        
        Log.d("NEXOGO_MOCK", "✅ ${testPatients.size} pacientes creados")
    }
    
    private suspend fun createTestAppointments() {
        Log.d("NEXOGO_MOCK", "📅 Creando citas de prueba...")
        
        val calendar = Calendar.getInstance()
        val today = Date()
        calendar.time = today
        
        val testAppointments = listOf(
            mapOf(
                "id" to "appt_001",
                "patientId" to "pet_001",
                "patientName" to "Max",
                "ownerId" to "patient_001",
                "ownerName" to "Carlos Rodríguez",
                "vetId" to "vet_001",
                "vetName" to "Dr. María González",
                "dateTime" to Timestamp(calendar.time),
                "duration" to 30,
                "status" to "SCHEDULED",
                "reason" to "Consulta general",
                "notes" to "Revisión de rutina",
                "createdBy" to "patient_001",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            ),
            mapOf(
                "id" to "appt_002",
                "patientId" to "pet_002",
                "patientName" to "Luna",
                "ownerId" to "patient_001",
                "ownerName" to "Carlos Rodríguez",
                "vetId" to "vet_001",
                "vetName" to "Dr. María González",
                "dateTime" to Timestamp(Date(calendar.timeInMillis + 24 * 60 * 60 * 1000)),
                "duration" to 45,
                "status" to "CONFIRMED",
                "reason" to "Vacunación",
                "notes" to "Aplicar vacuna anual",
                "createdBy" to "vet_001",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            ),
            mapOf(
                "id" to "appt_003",
                "patientId" to "pet_003",
                "patientName" to "Bella",
                "ownerId" to "patient_002",
                "ownerName" to "Laura Martínez",
                "vetId" to "vet_001",
                "vetName" to "Dr. María González",
                "dateTime" to Timestamp(Date(calendar.timeInMillis + 2 * 24 * 60 * 60 * 1000)),
                "duration" to 60,
                "status" to "SCHEDULED",
                "reason" to "Cirugía",
                "notes" to "Esterilización programada",
                "createdBy" to "patient_002",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            )
        )
        
        for (appointment in testAppointments) {
            val id = appointment["id"] as String
            repository.createDocument("appointments", id, appointment)
        }
        
        Log.d("NEXOGO_MOCK", "✅ ${testAppointments.size} citas creadas")
    }
    
    private suspend fun createTestInventory() {
        Log.d("NEXOGO_MOCK", "📦 Creando inventario de prueba...")
        
        val testProducts = listOf(
            mapOf(
                "id" to "prod_001",
                "name" to "Alimento Premium para Perros",
                "description" to "Alimento balanceado para perros adultos",
                "category" to "Alimentos",
                "quantity" to 50,
                "unitPrice" to 45000.0,
                "totalPrice" to 2250000.0,
                "imageUrl" to "",
                "lowStockThreshold" to 10,
                "isActive" to true,
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            ),
            mapOf(
                "id" to "prod_002",
                "name" to "Vacuna Antirrábica",
                "description" to "Vacuna para prevención de rabia",
                "category" to "Medicamentos",
                "quantity" to 25,
                "unitPrice" to 35000.0,
                "totalPrice" to 875000.0,
                "imageUrl" to "",
                "lowStockThreshold" to 5,
                "isActive" to true,
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            ),
            mapOf(
                "id" to "prod_003",
                "name" to "Juguete para Gatos",
                "description" to "Juguete interactivo para gatos",
                "category" to "Accesorios",
                "quantity" to 15,
                "unitPrice" to 25000.0,
                "totalPrice" to 375000.0,
                "imageUrl" to "",
                "lowStockThreshold" to 5,
                "isActive" to true,
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            )
        )
        
        for (product in testProducts) {
            val id = product["id"] as String
            repository.createDocument("inventory", id, product)
        }
        
        Log.d("NEXOGO_MOCK", "✅ ${testProducts.size} productos creados")
    }
    
    private suspend fun createTestSales() {
        Log.d("NEXOGO_MOCK", "💰 Creando ventas de prueba...")
        
        val testSales = listOf(
            mapOf(
                "id" to "sale_001",
                "patientId" to "pet_001",
                "patientName" to "Max",
                "ownerId" to "patient_001",
                "ownerName" to "Carlos Rodríguez",
                "items" to listOf(
                    mapOf(
                        "productId" to "prod_001",
                        "productName" to "Alimento Premium para Perros",
                        "quantity" to 2,
                        "unitPrice" to 45000.0,
                        "totalPrice" to 90000.0
                    )
                ),
                "services" to listOf(
                    mapOf(
                        "serviceId" to "serv_001",
                        "name" to "Consulta General",
                        "description" to "Revisión médica completa",
                        "price" to 50000.0
                    )
                ),
                "totalAmount" to 140000.0,
                "paymentMethod" to "CASH",
                "paymentStatus" to "PAID",
                "invoiceUrl" to "",
                "notes" to "Venta de prueba",
                "createdBy" to "vet_001",
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            )
        )
        
        for (sale in testSales) {
            val id = sale["id"] as String
            repository.createDocument("sales", id, sale)
        }
        
        Log.d("NEXOGO_MOCK", "✅ ${testSales.size} ventas creadas")
    }
    
    private suspend fun createTestChat() {
        Log.d("NEXOGO_MOCK", "💬 Creando chat de prueba...")
        
        val testChats = listOf(
            mapOf(
                "id" to "chat_001",
                "participants" to listOf("patient_001", "vet_001"),
                "lastMessage" to "Hola, ¿cómo está Max?",
                "lastMessageTime" to repository.getCurrentTimestamp(),
                "isActive" to true,
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            )
        )
        
        for (chat in testChats) {
            val id = chat["id"] as String
            repository.createDocument("chats", id, chat)
        }
        
        // Crear mensajes de prueba
        val testMessages = listOf(
            mapOf(
                "id" to "msg_001",
                "chatId" to "chat_001",
                "senderId" to "patient_001",
                "senderName" to "Carlos Rodríguez",
                "text" to "Hola doctora, ¿cómo está Max?",
                "attachments" to emptyList<Map<String, Any>>(),
                "timestamp" to repository.getCurrentTimestamp(),
                "isRead" to true,
                "isDelivered" to true
            ),
            mapOf(
                "id" to "msg_002",
                "chatId" to "chat_001",
                "senderId" to "vet_001",
                "senderName" to "Dr. María González",
                "text" to "Hola Carlos, Max está muy bien. Su última revisión fue excelente.",
                "attachments" to emptyList<Map<String, Any>>(),
                "timestamp" to repository.getCurrentTimestamp(),
                "isRead" to false,
                "isDelivered" to true
            )
        )
        
        for (message in testMessages) {
            val id = message["id"] as String
            repository.createDocument("messages", id, message)
        }
        
        Log.d("NEXOGO_MOCK", "✅ ${testChats.size} chats y ${testMessages.size} mensajes creados")
    }
    
    private suspend fun createTestClinicalHistory() {
        Log.d("NEXOGO_MOCK", "🏥 Creando historial clínico de prueba...")
        
        val testClinicalHistory = listOf(
            mapOf(
                "id" to "history_001",
                "patientId" to "pet_001",
                "patientName" to "Max",
                "ownerId" to "patient_001",
                "ownerName" to "Carlos Rodríguez",
                "vetId" to "vet_001",
                "vetName" to "Dr. María González",
                "consultationDate" to repository.getCurrentTimestamp(),
                "reason" to "Consulta de rutina",
                "anamnesis" to "El propietario reporta que Max está comiendo bien y es activo. No presenta síntomas anormales.",
                "physicalExam" to "Temperatura: 38.5°C, Peso: 25.5kg, FC: 120 lpm, FR: 20 rpm. Mucosas rosadas, hidratación normal.",
                "diagnosis" to "Paciente en buen estado general. No se observan alteraciones.",
                "treatment" to "Continuar con alimentación actual. Revisión en 6 meses.",
                "prognosis" to "Excelente",
                "evolution" to "Paciente evoluciona favorablemente",
                "attachments" to emptyList<Map<String, Any>>(),
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            ),
            mapOf(
                "id" to "history_002",
                "patientId" to "pet_002",
                "patientName" to "Luna",
                "ownerId" to "patient_001",
                "ownerName" to "Carlos Rodríguez",
                "vetId" to "vet_001",
                "vetName" to "Dr. María González",
                "consultationDate" to repository.getCurrentTimestamp(),
                "reason" to "Vacunación anual",
                "anamnesis" to "Gata de 2 años, vacunada anteriormente. No presenta síntomas de enfermedad.",
                "physicalExam" to "Temperatura: 38.2°C, Peso: 4.2kg, FC: 140 lpm, FR: 25 rpm. Mucosas rosadas, hidratación normal.",
                "diagnosis" to "Paciente sana, apta para vacunación",
                "treatment" to "Aplicar vacuna antirrábica. Control en 1 año.",
                "prognosis" to "Excelente",
                "evolution" to "Vacunación aplicada sin complicaciones",
                "attachments" to emptyList<Map<String, Any>>(),
                "createdAt" to repository.getCurrentTimestamp(),
                "updatedAt" to repository.getCurrentTimestamp()
            )
        )
        
        for (history in testClinicalHistory) {
            val id = history["id"] as String
            repository.createDocument("clinical_history", id, history)
        }
        
        Log.d("NEXOGO_MOCK", "✅ ${testClinicalHistory.size} historiales clínicos creados")
    }
    
    private suspend fun createTestConfig() {
        Log.d("NEXOGO_MOCK", "⚙️ Creando configuración de prueba...")
        
        val testConfig = mapOf(
            "id" to "config_001",
            "language" to "es",
            "notificationSettings" to mapOf(
                "appointmentReminders" to true,
                "lowStockAlerts" to true,
                "newMessages" to true,
                "systemUpdates" to true
            ),
            "contactPhone" to "+57 300 123 4567",
            "contactWhatsApp" to "+57 300 123 4567",
            "logoUrl" to "",
            "categories" to listOf("Alimentos", "Medicamentos", "Accesorios", "Higiene"),
            "services" to listOf("Consulta General", "Vacunación", "Cirugía", "Esterilización"),
            "updatedAt" to repository.getCurrentTimestamp()
        )
        
        repository.createDocument("config", "config_001", testConfig)
        
        Log.d("NEXOGO_MOCK", "✅ Configuración creada")
    }
}
