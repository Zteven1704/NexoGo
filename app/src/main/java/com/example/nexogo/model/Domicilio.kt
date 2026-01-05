package com.example.nexogo.model

import com.google.firebase.Timestamp

/**
 * Modelo de Solicitud de Domicilio (Productos o Servicios)
 */
data class Domicilio(
    val domicilioId: String = "",
    val usuarioId: String = "",
    val nombreUsuario: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val tipoSolicitud: TipoSolicitudDomicilio = TipoSolicitudDomicilio.SERVICIO,
    val datosServicio: DatosServicio? = null,
    val datosProducto: DatosProducto? = null,
    val fotoAdjuntaUrl: String? = null,
    val estado: DomicilioEstado = DomicilioEstado.PENDIENTE,
    val motivoRechazo: String? = null,
    val historialEventos: List<DomicilioEvento> = emptyList(),
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaActualizacion: Timestamp = Timestamp.now(),
    val veterinarioAsignadoId: String? = null,
    val veterinarioAsignadoNombre: String? = null
) {
    // Campos legacy para compatibilidad (deprecated)
    @Deprecated("Usar tipoSolicitud y datosServicio")
    val tipoServicio: String
        get() = datosServicio?.tipoServicio?.name ?: ""
    
    @Deprecated("Usar datosServicio.fechaSolicitada")
    val fechaSolicitada: Timestamp
        get() = datosServicio?.fechaSolicitada ?: Timestamp.now()
}

/**
 * Tipo de solicitud de domicilio
 */
enum class TipoSolicitudDomicilio {
    PRODUCTO,
    SERVICIO
}

/**
 * Datos específicos para solicitud de servicio veterinario
 */
data class DatosServicio(
    val tipoServicio: TipoServicioVeterinario = TipoServicioVeterinario.CONSULTA_GENERAL,
    val descripcion: String = "",
    val fechaSolicitada: Timestamp = Timestamp.now(),
    val tipoServicioOtro: String? = null // Si selecciona "Otro"
)

/**
 * Tipos de servicio veterinario disponibles
 */
enum class TipoServicioVeterinario {
    CONSULTA_GENERAL,
    VACUNACION,
    URGENCIAS,
    CONTROL_POSTOPERATORIO,
    OTRO
}

/**
 * Datos específicos para solicitud de productos
 */
data class DatosProducto(
    val listaProductos: List<ProductoDomicilio> = emptyList(),
    val observaciones: String = ""
)

/**
 * Producto en una solicitud de domicilio
 */
data class ProductoDomicilio(
    val productoId: String = "",
    val nombre: String = "",
    val cantidad: Int = 1,
    val observaciones: String = ""
)

/**
 * Modelo de Evento en el Historial
 */
data class DomicilioEvento(
    val fecha: Timestamp = Timestamp.now(),
    val usuarioAccion: String = "",
    val accion: String = "",
    val estadoFinal: DomicilioEstado = DomicilioEstado.PENDIENTE,
    val observaciones: String? = null
)

/**
 * Estados de la Solicitud de Domicilio
 */
enum class DomicilioEstado {
    PENDIENTE,
    APROBADO,
    RECHAZADO,
    EN_CAMINO,
    FINALIZADO,
    CANCELADO
}

