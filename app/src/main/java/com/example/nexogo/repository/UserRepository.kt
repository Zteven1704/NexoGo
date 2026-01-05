package com.example.nexogo.repository

import com.example.nexogo.core.models.User
import com.example.nexogo.core.models.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    suspend fun getAllUsers(): Result<List<User>>
    suspend fun getUsersByRole(role: UserRole): Result<List<User>>
    suspend fun getPendingProfessionals(): Result<List<User>>
    suspend fun approveUser(userId: String): Result<Unit>
    suspend fun rejectUser(userId: String): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun searchUsers(query: String): Result<List<User>>
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users")
                .orderBy("name")
                .get()
                .await()
            
            val users = snapshot.documents.mapNotNull { 
                it.toObject(User::class.java) 
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUsersByRole(role: UserRole): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("role", role.name)
                .whereEqualTo("isActive", true)
                .orderBy("name")
                .get()
                .await()
            
            val users = snapshot.documents.mapNotNull { 
                it.toObject(User::class.java) 
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingProfessionals(): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("isProfessional", true)
                .whereEqualTo("isApproved", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val users = snapshot.documents.mapNotNull { 
                it.toObject(User::class.java) 
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveUser(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("isApproved", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectUser(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("isApproved", false, "isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .limit(20)
                .get()
                .await()
            
            val users = snapshot.documents.mapNotNull { 
                it.toObject(User::class.java) 
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

