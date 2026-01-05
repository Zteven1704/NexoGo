package com.example.nexogo.model

import com.google.firebase.Timestamp

data class Appointment(
    val id: String = "",
    val patientId: String = "", // ID de la mascota
    val patientName: String = "", // Nombre de la mascota
    val ownerId: String = "", // ID del dueño
    val ownerName: String = "", // Nombre del dueño
    val vetId: String = "", // ID del veterinario
    val vetName: String = "", // Nombre del veterinario
    val dateTime: Timestamp = Timestamp.now(),
    val time: String = "", // Formato HH:mm
    val duration: Int = 30, // Duración en minutos
    val reason: String = "", // Motivo de la cita
    val notes: String = "", // Notas adicionales
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val createdBy: String = "", // ID del usuario que creó la cita
    val isRecurring: Boolean = false,
    val recurringType: RecurringType? = null, // Si es recurrente
    val reminderSent: Boolean = false,
    val reminderSentAt: Timestamp? = null
)

enum class AppointmentStatus {
    SCHEDULED,      // Programada
    CONFIRMED,      // Confirmada
    IN_PROGRESS,    // En progreso
    COMPLETED,      // Completada
    CANCELLED,      // Cancelada
    NO_SHOW,        // No se presentó
    RESCHEDULED     // Reprogramada
}

enum class RecurringType {
    DAILY,          // Diario
    WEEKLY,         // Semanal
    MONTHLY,        // Mensual
    YEARLY          // Anual
}