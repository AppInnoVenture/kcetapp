package com.kea.pyp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            remoteMessage.notification?.let { notification ->
                sendNotification(notification.title ?: "Exam Update", notification.body ?: "")
            } ?: run {
                val title = remoteMessage.data["title"] ?: "Exam Update"
                val body = remoteMessage.data["body"]
                if (!body.isNullOrBlank()) sendNotification(title, body)
            }
        } catch (e: Exception) {
            // Ignore errors as per requirement
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        val channelId = "exam_updates_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Exam Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for KCET exam updates"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            putExtra("notification", "7")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            7777777,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    longArrayOf(0, 500, 250, 500)
                } else {
                    null
                }
            )
            .build()

        notificationManager.notify(2, notification) 
    }
}