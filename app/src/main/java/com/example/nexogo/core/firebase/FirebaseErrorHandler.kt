package com.example.nexogo.core.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException

object FirebaseErrorHandler {
    private const val TAG = "FirebaseErrorHandler"

    fun handleError(e: Exception, action: String = "unknown") {
        when (e) {
            is FirebaseFirestoreException -> {
                when (e.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                        Log.e(TAG, "🚫 PERMISSION_DENIED en acción: $action → ${e.message}")
                    FirebaseFirestoreException.Code.UNAVAILABLE ->
                        Log.e(TAG, "⚠️ Servicio no disponible temporalmente → ${e.message}")
                    else ->
                        Log.e(TAG, "❌ Error Firestore ($action): ${e.code} - ${e.message}")
                }
            }
            else -> Log.e(TAG, "❌ Error general ($action): ${e.localizedMessage}")
        }
    }
}