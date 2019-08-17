package com.valerasetrakov.hiddencamerarecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationManager(context: Context): ContextWrapper(context) {

    companion object {
        const val CHANNEL_ID = "location_channel_id"
    }

    private val notificationManagerCompat = NotificationManagerCompat.from(context).apply {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL_ID, "Location channel", NotificationManager.IMPORTANCE_HIGH)
            createNotificationChannel(channel)
        }
    }

    fun createLocationServiceNotification (): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Location Service")
            .setContentText("Service for update phone's location")
        return notificationBuilder.build()
    }
}