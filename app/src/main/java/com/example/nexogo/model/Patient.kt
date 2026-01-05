package com.example.nexogo.model

import com.google.firebase.Timestamp

data class Patient(
    val id: String = "",
    val userId: String = "", // Reference to User
    val patientName: String = "", // Nombre del paciente (mascota)
    val petName: String = "", // Nombre de la mascota
    val ownerName: String = "",
    val ownerPhone: String = "",
    val ownerEmail: String = "",
    val ownerAddress: String = "",
    val pets: List<Pet> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class Pet(
    val id: String = "",
    val name: String = "",
    val petName: String = "", // Nombre de la mascota
    val petSpecies: String = "", // Dog, Cat, Bird, etc.
    val petBreed: String = "",
    val petAge: Int = 0, // Edad en años
    val petGender: String = "", // Male, Female
    val petColor: String = "",
    val species: String = "", // Dog, Cat, Bird, etc.
    val breed: String = "",
    val gender: String = "", // Male, Female
    val birthDate: Timestamp? = null,
    val weight: Double = 0.0,
    val color: String = "",
    val microchipNumber: String = "",
    val photoUrl: String = "",
    val isActive: Boolean = true,
    val medicalNotes: String = "",
    val allergies: List<String> = emptyList(),
    val vaccinations: List<Vaccination> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class Vaccination(
    val id: String = "",
    val name: String = "",
    val date: Timestamp = Timestamp.now(),
    val nextDueDate: Timestamp? = null,
    val veterinarianId: String = "",
    val notes: String = ""
)
