package me.gavin.rp

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService

object NotificationHelper {

    const val CHANNEL_DEFAULT = "default"
    const val CHANNEL_ALERT = "alert"

    @SuppressLint("InlinedApi")
    fun openSetting(context: Context, channel: String? = null) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                if (channel != null) {
                    Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_CHANNEL_ID, channel)
                } else {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                }.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            else -> {
                Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                        .putExtra("app_package", context.packageName)
                        .putExtra("app_uid", context.applicationInfo.uid)
            }
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).let {
            context.startActivity(it)
        }
    }

    fun notify(
            context: Context,
            title: String,
            content: String,
            ticker: String,
            intent: PendingIntent?,
            channel: String = CHANNEL_DEFAULT,
            id: Int = System.currentTimeMillis().toInt()) {
        NotificationManagerCompat.from(context).notify(id, newNotification(context, channel, title, content, ticker, intent))
    }

    private fun newNotification(
            context: Context,
            channelId: String,
            title: String,
            content: String,
            ticker: String,
            pi: PendingIntent?
    ): Notification {
        return NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_adb)
                .setContentTitle(title)
                .setContentText(content)
                .setTicker(ticker)
                .setShowWhen(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(longArrayOf(0, 500, 500, 500))
                // .setActions()
                .also {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        createChannel(context, channelId)
                    }
                }
                .build()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel(context: Context, channelId: String) {
        context.getSystemService<NotificationManager>()?.apply {
            if (getNotificationChannel(channelId) != null) return

            when (channelId) {
                CHANNEL_ALERT -> {
                    NotificationChannel(channelId, "提示", NotificationManager.IMPORTANCE_HIGH).apply {
                        setBypassDnd(true)
                        enableLights(true)
                        lightColor = Color.GREEN
                        setShowBadge(true)
                        setSound(Uri.parse("android.resource://" + App.app.packageName + "/" + R.raw.alert_25s),
                                AudioAttributes.Builder()
                                        .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                                        .build())
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 500, 500, 500)
                    }
                }
                else -> {
                    NotificationChannel(channelId, "默认", NotificationManager.IMPORTANCE_DEFAULT).apply {
                        setBypassDnd(false)
                        enableLights(true)
                        lightColor = Color.GREEN
                        setShowBadge(true)
                        setSound(null, null)
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 500, 500, 500)
                    }
                }
            }.also {
                createNotificationChannel(it)
            }
        }
    }
}