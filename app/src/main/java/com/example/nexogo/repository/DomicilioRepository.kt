package com.example.nexogo.repository

import android.net.Uri
import android.util.Log
import com.example.nexogo.model.Domicilio
import com.example.nexogo.model.DomicilioEstado
import com.example.nexogo.model.DomicilioEvento
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para manejar operaciones de Domicilios en Firestore
 */
@Singleton
class DomicilioRepository @Inject constructor() {
    
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    
    companion object {
        private const val TAG = "DomicilioRepository"
        private const val COLLECTION_DOMICILIOS = "domicilios"
        private const val COLLECTION_USERS = "users"
    }
    
    /**
     * Crea una nueva solicitud de domicilio
     */
    suspend fun createDomicilio(
        domicilio: Domicilio,
        fotoUri: Uri?
    ): Result<String> {
        return try {
            // Crear evento inicial en historial
            val eventoInicial = DomicilioEvento(
                fecha = Timestamp.now(),
                usuarioAccion = domicilio.usuarioId,
                accion = "Solicitud creada",
                estadoFinal = DomicilioEstado.PENDIENTE
            )
            
            val domicilioInicial = domicilio.copy(
                historialEventos = listOf(eventoInicial),
                fechaCreacion = Timestamp.now(),
                fechaActualizacion = Timestamp.now()
            )
            
            // Crear el documento primero para obtener el ID
            val docRef = firestore.collection(COLLECTION_DOMICILIOS)
                .add(domicilioInicial)
                .await()
            
            val domicilioId = docRef.id
            var fotoUrl: String? = null
            
            // Subir foto si existe (ahora que tenemos el ID)
            if (fotoUri != null) {
                val uploadResult = uploadDomicilioPhoto(domicilioId, fotoUri)
                fotoUrl = uploadResult.getOrNull()
                
                // Actualizar el documento con la URL de la foto
                if (fotoUrl != null) {
                    docRef.update("fotoAdjuntaUrl", fotoUrl).await()
                }
            }
            
            Log.d(TAG, "✅ Domicilio creado: $domicilioId")
            Result.success(domicilioId)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al crear domicilio: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene un domicilio por ID
     */
    suspend fun getDomicilioById(domicilioId: String): Result<Domicilio?> {
        return try {
            val doc = firestore.collection(COLLECTION_DOMICILIOS)
                .document(domicilioId)
                .get()
                .await()
            
            if (doc.exists()) {
                val domicilio = doc.toObject(Domicilio::class.java)
                    ?.copy(domicilioId = doc.id)
                Result.success(domicilio)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener domicilio: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los domicilios de un usuario
     */
    suspend fun getDomiciliosByUsuario(usuarioId: String): Result<List<Domicilio>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_DOMICILIOS)
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val domicilios = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Domicilio::class.java)?.copy(domicilioId = doc.id)
            }
            
            Log.d(TAG, "✅ Domicilios obtenidos: ${domicilios.size}")
            Result.success(domicilios)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener domicilios: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene todos los domicilios pendientes (para Admin)
     */
    suspend fun getDomiciliosPendientes(): Result<List<Domicilio>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_DOMICILIOS)
                .whereEqualTo("estado", DomicilioEstado.PENDIENTE.name)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val domicilios = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Domicilio::class.java)?.copy(domicilioId = doc.id)
            }
            
            Log.d(TAG, "✅ Domicilios pendientes obtenidos: ${domicilios.size}")
            Result.success(domicilios)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener domicilios pendientes: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene domicilios asignados a un veterinario/auxiliar
     */
    suspend fun getDomiciliosAsignados(veterinarioId: String): Result<List<Domicilio>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_DOMICILIOS)
                .whereEqualTo("veterinarioAsignadoId", veterinarioId)
                .whereIn("estado", listOf(
                    DomicilioEstado.APROBADO.name,
                    DomicilioEstado.EN_CAMINO.name
                ))
                .orderBy("fechaSolicitada", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val domicilios = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Domicilio::class.java)?.copy(domicilioId = doc.id)
            }
            
            Log.d(TAG, "✅ Domicilios asignados obtenidos: ${domicilios.size}")
            Result.success(domicilios)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener domicilios asignados: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza el estado de un domicilio
     */
    suspend fun updateEstadoDomicilio(
        domicilioId: String,
        nuevoEstado: DomicilioEstado,
        usuarioAccion: String,
        motivoRechazo: String? = null,
        veterinarioAsignadoId: String? = null,
        veterinarioAsignadoNombre: String? = null
    ): Result<Unit> {
        return try {
            // Obtener domicilio actual
            val domicilioActual = getDomicilioById(domicilioId).getOrNull()
                ?: return Result.failure(Exception("Domicilio no encontrado"))
            
            // Crear nuevo evento
            val nuevoEvento = DomicilioEvento(
                fecha = Timestamp.now(),
                usuarioAccion = usuarioAccion,
                accion = when (nuevoEstado) {
                    DomicilioEstado.APROBADO -> "Solicitud aprobada"
                    DomicilioEstado.RECHAZADO -> "Solicitud rechazada"
                    DomicilioEstado.EN_CAMINO -> "Veterinario en camino"
                    DomicilioEstado.FINALIZADO -> "Servicio finalizado"
                    DomicilioEstado.CANCELADO -> "Solicitud cancelada"
                    else -> "Estado actualizado"
                },
                estadoFinal = nuevoEstado,
                observaciones = motivoRechazo
            )
            
            // Agregar evento al historial
            val historialActualizado = domicilioActual.historialEventos + nuevoEvento
            
            // Preparar datos de actualización
            val updateData = mutableMapOf<String, Any>(
                "estado" to nuevoEstado.name,
                "fechaActualizacion" to Timestamp.now(),
                "historialEventos" to historialActualizado.map { evento ->
                    mapOf(
                        "fecha" to evento.fecha,
                        "usuarioAccion" to evento.usuarioAccion,
                        "accion" to evento.accion,
                        "estadoFinal" to evento.estadoFinal.name,
                        "observaciones" to (evento.observaciones ?: "")
                    )
                }
            )
            
            if (motivoRechazo != null) {
                updateData["motivoRechazo"] = motivoRechazo
            }
            
            if (veterinarioAsignadoId != null) {
                updateData["veterinarioAsignadoId"] = veterinarioAsignadoId
            }
            
            if (veterinarioAsignadoNombre != null) {
                updateData["veterinarioAsignadoNombre"] = veterinarioAsignadoNombre
            }
            
            firestore.collection(COLLECTION_DOMICILIOS)
                .document(domicilioId)
                .update(updateData)
                .await()
            
            Log.d(TAG, "✅ Estado actualizado: $domicilioId -> ${nuevoEstado.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al actualizar estado: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Cancela un domicilio (solo si está pendiente)
     */
    suspend fun cancelarDomicilio(
        domicilioId: String,
        usuarioId: String
    ): Result<Unit> {
        return try {
            val domicilio = getDomicilioById(domicilioId).getOrNull()
                ?: return Result.failure(Exception("Domicilio no encontrado"))
            
            if (domicilio.estado != DomicilioEstado.PENDIENTE) {
                return Result.failure(Exception("Solo se pueden cancelar solicitudes pendientes"))
            }
            
            if (domicilio.usuarioId != usuarioId) {
                return Result.failure(Exception("No tienes permiso para cancelar esta solicitud"))
            }
            
            updateEstadoDomicilio(
                domicilioId = domicilioId,
                nuevoEstado = DomicilioEstado.CANCELADO,
                usuarioAccion = usuarioId,
                motivoRechazo = "Cancelado por el usuario"
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al cancelar domicilio: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene el token FCM de un usuario
     */
    suspend fun getUserFcmToken(userId: String): Result<String?> {
        return try {
            val doc = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()
            
            if (doc.exists()) {
                val token = doc.getString("tokenFCM")
                Result.success(token)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener token FCM: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Actualiza el token FCM de un usuario
     */
    suspend fun updateUserFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update("tokenFCM", token)
                .await()
            
            Log.d(TAG, "✅ Token FCM actualizado para usuario: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al actualizar token FCM: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sube foto adjunta de domicilio
     */
    private suspend fun uploadDomicilioPhoto(domicilioId: String, fotoUri: Uri): Result<String> {
        return try {
            val fileName = "domicilio_${domicilioId}_${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("domicilios/$domicilioId/$fileName")
            
            val uploadTask = ref.putFile(fotoUri).await()
            val downloadUrl = ref.downloadUrl.await()
            
            Log.d(TAG, "✅ Foto subida: ${downloadUrl.toString()}")
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al subir foto: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Escucha cambios en tiempo real de domicilios de un usuario
     */
    fun listenToUserDomicilios(usuarioId: String): Flow<List<Domicilio>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_DOMICILIOS)
            .whereEqualTo("usuarioId", usuarioId)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val domicilios = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Domicilio::class.java)?.copy(domicilioId = doc.id)
                } ?: emptyList()
                
                trySend(domicilios)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Escucha cambios en tiempo real de domicilios pendientes
     */
    fun listenToPendientes(): Flow<List<Domicilio>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_DOMICILIOS)
            .whereEqualTo("estado", DomicilioEstado.PENDIENTE.name)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val domicilios = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Domicilio::class.java)?.copy(domicilioId = doc.id)
                } ?: emptyList()
                
                trySend(domicilios)
            }
        
        awaitClose { listener.remove() }
    }
}

