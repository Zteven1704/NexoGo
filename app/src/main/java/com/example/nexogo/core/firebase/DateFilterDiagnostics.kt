package com.example.nexogo.core.firebase

import android.util.Log
import com.google.firebase.Timestamp
import java.util.*

/**
 * DateFilterDiagnostics
 * ---------------------
 * Herramienta de diagnóstico para verificar el filtrado de fechas
 * en citas y identificar problemas de visualización.
 */
object DateFilterDiagnostics {

    private const val TAG = "DateFilterDiagnostics"

    /**
     * 🔍 Diagnostica el filtrado de fechas para una cita específica
     */
    fun diagnoseDateFiltering(
        appointment: com.example.nexogo.model.Appointment,
        selectedDate: Date
    ) {
        Log.d(TAG, "🔍 DIAGNÓSTICO DE FILTRADO DE FECHAS")
        Log.d(TAG, "=====================================")
        
        // Información de la cita
        Log.d(TAG, "📋 CITA:")
        Log.d(TAG, "   - ID: ${appointment.id}")
        Log.d(TAG, "   - Patient: ${appointment.patientName}")
        Log.d(TAG, "   - Reason: ${appointment.reason}")
        
        // Fecha de la cita
        val appointmentDate = appointment.dateTime.toDate()
        Log.d(TAG, "📅 FECHA DE LA CITA:")
        Log.d(TAG, "   - Timestamp: ${appointment.dateTime}")
        Log.d(TAG, "   - Date: $appointmentDate")
        Log.d(TAG, "   - Day: ${appointmentDate.date}")
        Log.d(TAG, "   - Month: ${appointmentDate.month}")
        Log.d(TAG, "   - Year: ${appointmentDate.year}")
        
        // Fecha seleccionada
        Log.d(TAG, "📅 FECHA SELECCIONADA:")
        Log.d(TAG, "   - Date: $selectedDate")
        Log.d(TAG, "   - Day: ${selectedDate.date}")
        Log.d(TAG, "   - Month: ${selectedDate.month}")
        Log.d(TAG, "   - Year: ${selectedDate.year}")
        
        // Comparación
        val dayMatch = appointmentDate.date == selectedDate.date
        val monthMatch = appointmentDate.month == selectedDate.month
        val yearMatch = appointmentDate.year == selectedDate.year
        val allMatch = dayMatch && monthMatch && yearMatch
        
        Log.d(TAG, "🔍 COMPARACIÓN:")
        Log.d(TAG, "   - Día coincide: $dayMatch")
        Log.d(TAG, "   - Mes coincide: $monthMatch")
        Log.d(TAG, "   - Año coincide: $yearMatch")
        Log.d(TAG, "   - TODAS coinciden: $allMatch")
        
        if (!allMatch) {
            Log.w(TAG, "⚠️ La cita NO debería aparecer en esta fecha")
        } else {
            Log.d(TAG, "✅ La cita SÍ debería aparecer en esta fecha")
        }
    }

    /**
     * 🧪 Prueba el filtrado con diferentes métodos
     */
    fun testDifferentFilteringMethods(
        appointments: List<com.example.nexogo.model.Appointment>,
        selectedDate: Date
    ) {
        Log.d(TAG, "🧪 PROBANDO DIFERENTES MÉTODOS DE FILTRADO")
        Log.d(TAG, "===========================================")
        
        // Método 1: Comparación directa (actual)
        val method1 = appointments.filter { appointment ->
            val appointmentDate = appointment.dateTime.toDate()
            appointmentDate.date == selectedDate.date &&
            appointmentDate.month == selectedDate.month &&
            appointmentDate.year == selectedDate.year
        }
        Log.d(TAG, "📊 Método 1 (directo): ${method1.size} citas")
        
        // Método 2: Comparación con Calendar
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = Timestamp(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = Timestamp(calendar.time)
        
        val method2 = appointments.filter { appointment ->
            appointment.dateTime >= startOfDay && appointment.dateTime < endOfDay
        }
        Log.d(TAG, "📊 Método 2 (Calendar): ${method2.size} citas")
        
        // Método 3: Comparación de strings
        val selectedDateStr = "${selectedDate.date}/${selectedDate.month + 1}/${selectedDate.year + 1900}"
        val method3 = appointments.filter { appointment ->
            val appointmentDate = appointment.dateTime.toDate()
            val appointmentDateStr = "${appointmentDate.date}/${appointmentDate.month + 1}/${appointmentDate.year + 1900}"
            appointmentDateStr == selectedDateStr
        }
        Log.d(TAG, "📊 Método 3 (string): ${method3.size} citas")
        Log.d(TAG, "📅 Fecha seleccionada (string): $selectedDateStr")
        
        // Mostrar detalles de cada método
        method1.forEachIndexed { index, appointment ->
            val appointmentDate = appointment.dateTime.toDate()
            Log.d(TAG, "   Método 1 - Cita $index: ${appointment.patientName} - ${appointmentDate.date}/${appointmentDate.month + 1}/${appointmentDate.year + 1900}")
        }
        
        method2.forEachIndexed { index, appointment ->
            val appointmentDate = appointment.dateTime.toDate()
            Log.d(TAG, "   Método 2 - Cita $index: ${appointment.patientName} - ${appointmentDate.date}/${appointmentDate.month + 1}/${appointmentDate.year + 1900}")
        }
        
        method3.forEachIndexed { index, appointment ->
            val appointmentDate = appointment.dateTime.toDate()
            Log.d(TAG, "   Método 3 - Cita $index: ${appointment.patientName} - ${appointmentDate.date}/${appointmentDate.month + 1}/${appointmentDate.year + 1900}")
        }
    }

    /**
     * 📊 Genera reporte de todas las citas con sus fechas
     */
    fun generateAppointmentsDateReport(
        appointments: List<com.example.nexogo.model.Appointment>
    ): String {
        val report = StringBuilder()
        report.appendLine("📊 REPORTE DE FECHAS DE CITAS")
        report.appendLine("=============================")
        report.appendLine("Total de citas: ${appointments.size}")
        report.appendLine("")
        
        appointments.forEachIndexed { index, appointment ->
            val appointmentDate = appointment.dateTime.toDate()
            report.appendLine("${index + 1}. ${appointment.patientName}")
            report.appendLine("   - Fecha: ${appointmentDate.date}/${appointmentDate.month + 1}/${appointmentDate.year + 1900}")
            report.appendLine("   - Hora: ${appointmentDate.hours}:${appointmentDate.minutes}")
            report.appendLine("   - Timestamp: ${appointment.dateTime}")
            report.appendLine("")
        }
        
        return report.toString()
    }
}
