package com.example.nexogo.repository

import com.example.nexogo.model.Patient
import com.example.nexogo.model.Pet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface PatientRepository {
    suspend fun createPatient(patient: Patient): Result<String>
    suspend fun getPatientById(patientId: String): Result<Patient>
    suspend fun getPatientByUserId(userId: String): Result<Patient>
    suspend fun getAllPatients(): Result<List<Patient>>
    suspend fun updatePatient(patient: Patient): Result<Unit>
    suspend fun deletePatient(patientId: String): Result<Unit>
    suspend fun searchPatients(query: String): Result<List<Patient>>
    suspend fun addPetToPatient(patientId: String, pet: Pet): Result<Unit>
    suspend fun updatePet(patientId: String, pet: Pet): Result<Unit>
    suspend fun removePetFromPatient(patientId: String, petId: String): Result<Unit>
}

@Singleton
class PatientRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PatientRepository {

    private val collection = firestore.collection("patients")

    override suspend fun createPatient(patient: Patient): Result<String> {
        return try {
            val docRef = collection.document()
            val patientWithId = patient.copy(id = docRef.id)
            docRef.set(patientWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPatientById(patientId: String): Result<Patient> {
        return try {
            val document = collection.document(patientId).get().await()
            val patient = document.toObject(Patient::class.java)
            patient?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Patient not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPatientByUserId(userId: String): Result<Patient> {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()
            
            val patient = snapshot.documents.firstOrNull()?.toObject(Patient::class.java)
            patient?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Patient not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllPatients(): Result<List<Patient>> {
        return try {
            val snapshot = collection
                .orderBy("ownerName")
                .get()
                .await()
            
            val patients = snapshot.documents.mapNotNull { 
                it.toObject(Patient::class.java) 
            }
            Result.success(patients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePatient(patient: Patient): Result<Unit> {
        return try {
            collection.document(patient.id).set(patient).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePatient(patientId: String): Result<Unit> {
        return try {
            collection.document(patientId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchPatients(query: String): Result<List<Patient>> {
        return try {
            val snapshot = collection
                .whereGreaterThanOrEqualTo("ownerName", query)
                .whereLessThanOrEqualTo("ownerName", query + "\uf8ff")
                .limit(20)
                .get()
                .await()
            
            val patients = snapshot.documents.mapNotNull { 
                it.toObject(Patient::class.java) 
            }
            Result.success(patients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addPetToPatient(patientId: String, pet: Pet): Result<Unit> {
        return try {
            val patientDoc = collection.document(patientId).get().await()
            val patient = patientDoc.toObject(Patient::class.java)
            
            patient?.let {
                val updatedPets = it.pets.toMutableList()
                updatedPets.add(pet)
                val updatedPatient = it.copy(pets = updatedPets)
                
                collection.document(patientId).set(updatedPatient).await()
                Result.success(Unit)
            } ?: Result.failure(Exception("Patient not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePet(patientId: String, pet: Pet): Result<Unit> {
        return try {
            val patientDoc = collection.document(patientId).get().await()
            val patient = patientDoc.toObject(Patient::class.java)
            
            patient?.let {
                val updatedPets = it.pets.map { existingPet ->
                    if (existingPet.id == pet.id) pet else existingPet
                }
                val updatedPatient = it.copy(pets = updatedPets)
                
                collection.document(patientId).set(updatedPatient).await()
                Result.success(Unit)
            } ?: Result.failure(Exception("Patient not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removePetFromPatient(patientId: String, petId: String): Result<Unit> {
        return try {
            val patientDoc = collection.document(patientId).get().await()
            val patient = patientDoc.toObject(Patient::class.java)
            
            patient?.let {
                val updatedPets = it.pets.filter { pet -> pet.id != petId }
                val updatedPatient = it.copy(pets = updatedPets)
                
                collection.document(patientId).set(updatedPatient).await()
                Result.success(Unit)
            } ?: Result.failure(Exception("Patient not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

