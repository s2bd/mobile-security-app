package com.dewanmukto.sechero

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.app.Service
import android.content.Intent
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {
    private val channelId = "SecHeroChannel"
    private val notificationId = 101

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notification = buildForegroundNotification()
        startForeground(notificationId, notification)
        return START_STICKY
    }

    private fun buildForegroundNotification(): Notification {
        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SecHero is protecting your device 🛡️")
            .setContentText("Your device is under protection.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .setOngoing(true)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SecHero Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?) = null
}
