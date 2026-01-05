package com.example.nexogo.firebase

import com.example.nexogo.core.models.User
import com.example.nexogo.core.models.UserRole
import com.example.nexogo.model.Product
import com.example.nexogo.model.Appointment
import com.example.nexogo.modules.sales.model.Service
import com.example.nexogo.model.AppointmentStatus
import com.example.nexogo.modules.sales.model.ServiceCategory
import com.google.firebase.Timestamp
import java.util.*

/**
 * Datos de prueba para Firebase
 * Crea usuarios de ejemplo para cada rol
 */
object FirebaseTestData {
    
    /**
     * Crea usuarios de prueba para cada rol
     */
    fun createTestUsers(): List<User> {
        val now = Timestamp.now()
        
        return listOf(
            // Administrador
            User(
                id = "admin_001",
                email = "admin@nexogo.com",
                name = "Dr. María González",
                phone = "+52 55 1234 5678",
                whatsapp = "+52 55 1234 5678",
                address = "Av. Reforma 123, CDMX",
                role = UserRole.ADMIN,
                isApproved = true,
                isProfessional = true,
                specialization = "Administración Veterinaria",
                licenseNumber = "ADM001",
                createdAt = now,
                language = "es"
            ),
            
            // Veterinario
            User(
                id = "vet_001",
                email = "veterinario@nexogo.com",
                name = "Dr. Carlos Martínez",
                phone = "+52 55 2345 6789",
                whatsapp = "+52 55 2345 6789",
                address = "Calle Insurgentes 456, CDMX",
                role = UserRole.VET,
                isApproved = true,
                isProfessional = true,
                specialization = "Medicina General",
                licenseNumber = "VET001",
                createdAt = now,
                language = "es"
            ),
            
            // Auxiliar Veterinario
            User(
                id = "aux_001",
                email = "auxiliar@nexogo.com",
                name = "Ana López",
                phone = "+52 55 3456 7890",
                whatsapp = "+52 55 3456 7890",
                address = "Av. Insurgentes Sur 789, CDMX",
                role = UserRole.VET_ASSISTANT,
                isApproved = true,
                isProfessional = true,
                specialization = "Auxiliar Veterinario",
                licenseNumber = "AUX001",
                createdAt = now,
                language = "es"
            ),
            
            // Paciente 1
            User(
                id = "USER_001",
                email = "maria.garcia@email.com",
                name = "María García",
                phone = "+52 55 4567 8901",
                whatsapp = "+52 55 4567 8901",
                address = "Calle Roma Norte 321, CDMX",
                role = UserRole.USER,
                isApproved = true,
                isProfessional = false,
                createdAt = now,
                language = "es"
            ),
            
            // Paciente 2
            User(
                id = "USER_002",
                email = "juan.perez@email.com",
                name = "Juan Pérez",
                phone = "+52 55 5678 9012",
                whatsapp = "+52 55 5678 9012",
                address = "Av. Polanco 654, CDMX",
                role = UserRole.USER,
                isApproved = true,
                isProfessional = false,
                createdAt = now,
                language = "es"
            ),
            
            // Veterinario pendiente de aprobación
            User(
                id = "vet_pending_001",
                email = "veterinario.pendiente@email.com",
                name = "Dr. Laura Rodríguez",
                phone = "+52 55 6789 0123",
                whatsapp = "+52 55 6789 0123",
                address = "Calle Condesa 987, CDMX",
                role = UserRole.VET,
                isApproved = false,
                isProfessional = true,
                specialization = "Cirugía",
                licenseNumber = "VET002",
                createdAt = now,
                language = "es"
            )
        )
    }
    
    /**
     * Crea productos de prueba para el inventario
     */
    fun createTestProducts(): List<Product> {
        val now = Timestamp.now()
        
        return listOf(
            Product(
                id = "prod_001",
                name = "Vacuna Triple Felina",
                description = "Vacuna para gatos contra panleucopenia, calicivirus y rinotraqueítis",
                category = "Vacunas",
                sku = "VAC-TF-001",
                barcode = "1234567890123",
                currentStock = 25,
                minStock = 5,
                maxStock = 100,
                unitPrice = 350.0,
                salePrice = 450.0,
                costPrice = 280.0,
                isPrescriptionRequired = true,
                supplier = "Laboratorios Veterinarios S.A.",
                supplierContact = "contacto@labvet.com",
                createdAt = now,
                createdBy = "admin_001"
            ),
            
            Product(
                id = "prod_002",
                name = "Antiparasitario Interno",
                description = "Tabletas para desparasitación interna de perros y gatos",
                category = "Medicamentos",
                sku = "ANT-INT-002",
                barcode = "1234567890124",
                currentStock = 8,
                minStock = 10,
                maxStock = 50,
                unitPrice = 120.0,
                salePrice = 180.0,
                costPrice = 90.0,
                isPrescriptionRequired = true,
                supplier = "Farmacéutica Animal",
                supplierContact = "ventas@farmanimal.com",
                createdAt = now,
                createdBy = "admin_001"
            ),
            
            Product(
                id = "prod_003",
                name = "Alimento Premium para Perros",
                description = "Alimento balanceado para perros adultos de razas medianas",
                category = "Alimentos",
                sku = "ALI-PER-003",
                barcode = "1234567890125",
                currentStock = 15,
                minStock = 5,
                maxStock = 30,
                unitPrice = 450.0,
                salePrice = 650.0,
                costPrice = 380.0,
                isPrescriptionRequired = false,
                supplier = "NutriPet S.A.",
                supplierContact = "distribuidores@nutripet.com",
                createdAt = now,
                createdBy = "admin_001"
            ),
            
            Product(
                id = "prod_004",
                name = "Jeringa Desechable 5ml",
                description = "Jeringa desechable estéril de 5ml con aguja",
                category = "Insumos Médicos",
                sku = "JER-5ML-004",
                barcode = "1234567890126",
                currentStock = 3,
                minStock = 20,
                maxStock = 100,
                unitPrice = 8.0,
                salePrice = 12.0,
                costPrice = 6.0,
                isPrescriptionRequired = false,
                supplier = "MedSupply México",
                supplierContact = "ventas@medsupply.com",
                createdAt = now,
                createdBy = "admin_001"
            )
        )
    }
    
    /**
     * Crea servicios veterinarios de prueba
     */
    fun createTestServices(): List<Service> {
        val now = Timestamp.now()
        
        return listOf(
            Service(
                id = "serv_001",
                name = "Consulta General",
                description = "Consulta veterinaria general para evaluación de salud",
                category = "CONSULTATION",
                price = 500.0,
                duration = 30,
                isActive = true,
                createdAt = now,
                createdBy = "admin_001"
            ),
            
            Service(
                id = "serv_002",
                name = "Vacunación",
                description = "Aplicación de vacunas según esquema de vacunación",
                category = "VACCINATION",
                price = 300.0,
                duration = 15,
                isActive = true,
                createdAt = now,
                createdBy = "admin_001"
            ),
            
            Service(
                id = "serv_003",
                name = "Cirugía de Esterilización",
                description = "Cirugía de esterilización para perros y gatos",
                category = "SURGERY",
                price = 2500.0,
                duration = 120,
                isActive = true,
                createdAt = now,
                createdBy = "admin_001"
            ),
            
            Service(
                id = "serv_004",
                name = "Radiografía",
                description = "Toma de radiografías para diagnóstico",
                category = "IMAGING",
                price = 800.0,
                duration = 45,
                isActive = true,
                createdAt = now,
                createdBy = "admin_001"
            )
        )
    }
    
    /**
     * Crea citas de prueba
     */
    fun createTestAppointments(): List<Appointment> {
        val now = Timestamp.now()
        val tomorrow = Timestamp(Date(now.toDate().time + 24 * 60 * 60 * 1000))
        val nextWeek = Timestamp(Date(now.toDate().time + 7 * 24 * 60 * 60 * 1000))
        
        return listOf(
            Appointment(
                id = "appt_001",
                patientId = "pet_001",
                patientName = "Max",
                ownerId = "USER_001",
                ownerName = "María García",
                vetId = "vet_001",
                vetName = "Dr. Carlos Martínez",
                dateTime = tomorrow,
                time = "10:00",
                duration = 30,
                reason = "Consulta de rutina",
                notes = "Primera consulta del cachorro",
                status = AppointmentStatus.SCHEDULED,
                createdAt = now,
                createdBy = "USER_001"
            ),
            
            Appointment(
                id = "appt_002",
                patientId = "pet_002",
                patientName = "Luna",
                ownerId = "USER_002",
                ownerName = "Juan Pérez",
                vetId = "vet_001",
                vetName = "Dr. Carlos Martínez",
                dateTime = nextWeek,
                time = "14:30",
                duration = 45,
                reason = "Vacunación anual",
                notes = "Aplicar vacuna triple y antirrábica",
                status = AppointmentStatus.SCHEDULED,
                createdAt = now,
                createdBy = "USER_002"
            )
        )
    }
}

