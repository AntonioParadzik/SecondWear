package hr.ferit.antonioparadzik.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Build
import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.app.NotificationCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import hr.ferit.antonioparadzik.MainActivity
import hr.ferit.antonioparadzik.R
import kotlin.random.Random

class SecondWearMessagingService: FirebaseMessagingService() {

    private val random = Random
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("FCM", "Refreshed token: $token")
        // Update Firestore or server with the new token
        updateTokenInFirestore(token)
    }


    private fun updateTokenInFirestore(token: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Firebase.firestore.collection("users").document(user.uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("Firestore", "FCM token updated successfully for user ${user.uid}")
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error updating FCM token for user ${user.uid}", exception)
                }
        }
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if the message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            // Display notification or process it further
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    private fun sendNotification(message: RemoteMessage.Notification) {

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, FLAG_IMMUTABLE
        )

        val channelId = CHANNEL_ID

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, CHANNEL_NAME, IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        manager.notify(random.nextInt(), notificationBuilder.build())
    }

    companion object {
        const val CHANNEL_NAME = "FCM notification channel"
        const val CHANNEL_ID = "default_channel_id"
    }

}