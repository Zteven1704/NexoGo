package com.example.nexogo.modules.domicilios.ui

import com.example.nexogo.model.TipoServicioVeterinario
import java.text.SimpleDateFormat
import java.util.*

object DomicilioUtils {
    fun formatDate(date: Date): String {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }
    
    fun getTipoServicioDisplayName(tipo: TipoServicioVeterinario): String {
        return when (tipo) {
            TipoServicioVeterinario.CONSULTA_GENERAL -> "Consulta general"
            TipoServicioVeterinario.VACUNACION -> "Vacunación"
            TipoServicioVeterinario.URGENCIAS -> "Urgencias"
            TipoServicioVeterinario.CONTROL_POSTOPERATORIO -> "Control postoperatorio"
            TipoServicioVeterinario.OTRO -> "Otro"
        }
    }
}

