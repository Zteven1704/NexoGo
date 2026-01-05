package com.example.nexogo.modules.history.models

import com.google.firebase.Timestamp

/**
 * Modelo para mascotas
 */
data class Pet(
    val petId: String = "",
    val name: String = "",
    val species: String = "", // Perro, Gato, etc.
    val breed: String = "",
    val age: Int = 0,
    val gender: String = "", // Macho, Hembra
    val color: String = "",
    val weight: Double = 0.0,
    val ownerId: String = "",
    val ownerName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isActive: Boolean = true
)

