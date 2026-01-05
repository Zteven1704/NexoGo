package com.example.nexogo.core.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object SafeFirestoreOperations {

    private val db = FirebaseFirestore.getInstance()
    private const val TAG = "SafeFirestoreOps"

    suspend fun safeSet(
        collection: String,
        documentId: String,
        data: Any
    ): Boolean {
        return try {
            db.collection(collection).document(documentId).set(data).await()
            Log.i(TAG, "✅ [OK] Guardado en $collection/$documentId")
            true
        } catch (e: Exception) {
            FirebaseErrorHandler.handleError(e, "set($collection)")
            false
        }
    }

    suspend fun <T> safeGetCollection(
        collection: String,
        clazz: Class<T>
    ): List<T> {
        return try {
            val snapshot = db.collection(collection).get().await()
            snapshot.toObjects(clazz)
        } catch (e: Exception) {
            FirebaseErrorHandler.handleError(e, "get($collection)")
            emptyList()
        }
    }

    suspend fun safeDelete(collection: String, documentId: String): Boolean {
        return try {
            db.collection(collection).document(documentId).delete().await()
            Log.i(TAG, "🗑️ [OK] Eliminado $collection/$documentId")
            true
        } catch (e: Exception) {
            FirebaseErrorHandler.handleError(e, "delete($collection)")
            false
        }
    }
}