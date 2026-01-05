package com.example.nexogo.notifications

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.nexogo.MainActivity
import com.example.nexogo.R
import android.util.Log

class NexoGoMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "NexoGoMessagingService"
        private const val CHANNEL_ID = "nexogo_channel"
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Mensaje recibido: ${remoteMessage.from}")
        
        val title = remoteMessage.notification?.title ?: "NexoGo"
        val body = remoteMessage.notification?.body ?: "Tienes una actualización"
        
        // Crear intent para abrir la app en LoginScreen
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigateTo", "loginScreen")
            putExtra("notificationTitle", title)
            putExtra("notificationBody", body)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            System.currentTimeMillis().toInt(), 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        with(NotificationManagerCompat.from(this)) {
            try {
                notify(System.currentTimeMillis().toInt(), notification)
                Log.d(TAG, "Notificación enviada exitosamente")
            } catch (e: SecurityException) {
                Log.e(TAG, "Error al enviar notificación: ${e.message}")
            }
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")
        
        // Aquí podrías enviar el token al servidor si es necesario
        // Por ahora solo lo logueamos
    }
}
