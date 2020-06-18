package me.gavin.tools.notifier

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService

object NotificationHelper {

    fun isNotificationEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun notify(
        context: Context,
        channel: String,
        tag: String?,
        id: Int,
        title: String,
        content: String,
        intent: PendingIntent?
    ) {
        NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(
                NotificationCompat
                    .BigTextStyle()
                    .setBigContentTitle(title)
                    .setSummaryText(channel)
                    .bigText(content)
            )
            .setShowWhen(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 500, 500))
            // .setActions()
            .also {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createChannel(context, channel)
                }
            }
            .build()
            .let {
                NotificationManagerCompat.from(context).notify(tag, id, it)
            }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel(context: Context, channel: String) {
        context.getSystemService<NotificationManager>()?.apply {
            if (getNotificationChannel(channel) != null) return

            NotificationChannel(channel, channel, NotificationManager.IMPORTANCE_DEFAULT)
                .apply {
                    setBypassDnd(false)
                    enableLights(true)
                    lightColor = Color.GREEN
                    setShowBadge(true)
                    setSound(null, null)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 500, 500)
                }.let {
                    createNotificationChannel(it)
                }
        }
    }

    fun cancel(context: Context, id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }

}