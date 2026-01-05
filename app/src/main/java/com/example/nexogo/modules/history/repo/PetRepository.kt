package com.example.nexogo.modules.history.repo

import android.util.Log
import com.example.nexogo.core.FirebaseRepository
import com.example.nexogo.modules.history.models.Pet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repositorio para operaciones de mascotas
 */
class PetRepository(
    private val firebaseRepository: FirebaseRepository
) {
    companion object {
        private const val TAG = "NEXOGO_PETS"
        private const val COLLECTION_NAME = "pets"
    }

    /**
     * Obtiene todas las mascotas
     */
    suspend fun getPets(): Flow<List<Pet>> = flow {
        try {
            Log.d(TAG, "Obteniendo mascotas")
            val result = firebaseRepository.getCollection(COLLECTION_NAME)
            
            if (result.isSuccess) {
                val dataList = result.getOrNull() ?: emptyList()
                val pets = dataList.mapNotNull { data ->
                    try {
                        Pet(
                            petId = data["petId"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            species = data["species"] as? String ?: "",
                            breed = data["breed"] as? String ?: "",
                            age = (data["age"] as? Number)?.toInt() ?: 0,
                            gender = data["gender"] as? String ?: "",
                            color = data["color"] as? String ?: "",
                            weight = (data["weight"] as? Number)?.toDouble() ?: 0.0,
                            ownerId = data["ownerId"] as? String ?: "",
                            ownerName = data["ownerName"] as? String ?: "",
                            createdAt = data["createdAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                            updatedAt = data["updatedAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                            isActive = data["isActive"] as? Boolean ?: true
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parseando mascota: ${e.message}")
                        null
                    }
                }
                
                Log.d(TAG, "Mascotas obtenidas: ${pets.size}")
                emit(pets)
            } else {
                Log.e(TAG, "Error obteniendo mascotas: ${result.exceptionOrNull()?.message}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo mascotas: ${e.message}")
            emit(emptyList())
        }
    }

    /**
     * Busca mascotas por nombre
     */
    suspend fun searchPets(query: String): Flow<List<Pet>> = flow {
        try {
            Log.d(TAG, "Buscando mascotas: $query")
            val result = firebaseRepository.getCollection(COLLECTION_NAME)
            
            if (result.isSuccess) {
                val dataList = result.getOrNull() ?: emptyList()
                val pets = dataList.mapNotNull { data ->
                    try {
                        val pet = Pet(
                            petId = data["petId"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            species = data["species"] as? String ?: "",
                            breed = data["breed"] as? String ?: "",
                            age = (data["age"] as? Number)?.toInt() ?: 0,
                            gender = data["gender"] as? String ?: "",
                            color = data["color"] as? String ?: "",
                            weight = (data["weight"] as? Number)?.toDouble() ?: 0.0,
                            ownerId = data["ownerId"] as? String ?: "",
                            ownerName = data["ownerName"] as? String ?: "",
                            createdAt = data["createdAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                            updatedAt = data["updatedAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                            isActive = data["isActive"] as? Boolean ?: true
                        )
                        
                        // Filtrar por query
                        if (query.isBlank() || 
                            pet.name.contains(query, ignoreCase = true) ||
                            pet.species.contains(query, ignoreCase = true) ||
                            pet.breed.contains(query, ignoreCase = true) ||
                            pet.ownerName.contains(query, ignoreCase = true)) {
                            pet
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parseando mascota: ${e.message}")
                        null
                    }
                }
                
                Log.d(TAG, "Mascotas encontradas: ${pets.size}")
                emit(pets)
            } else {
                Log.e(TAG, "Error buscando mascotas: ${result.exceptionOrNull()?.message}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error buscando mascotas: ${e.message}")
            emit(emptyList())
        }
    }
}

