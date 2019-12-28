package me.gavin.rp

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import android.app.KeyguardManager
import androidx.core.content.getSystemService


class RPListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!App.state) return

        sbn.notification?.extras?.let {
            val title = it[Notification.EXTRA_TITLE] as? String
                    ?: it[Notification.EXTRA_TITLE_BIG] as? String ?: return
            val text = it[Notification.EXTRA_TEXT] as? String
                    ?: it[Notification.EXTRA_BIG_TEXT] as? String ?: return
            System.out.println(" ---------------------------------------------------------------- ")
            System.out.println(title)
            System.out.println(text)
            System.out.println(" ---------------------------------------------------------------- ")

            if ("com.tencent.mm" == sbn.packageName && "[微信红包]" in text
                    || "T50ZDvgrbcyD8keT" == title) {

                if (!isKeyguardLocked(this)) {
                    sbn.notification.contentIntent?.send()
                }

                NotificationHelper.notify(
                        this,
                        "@$title",
                        "@$text",
                        "@$text",
                        sbn.notification.contentIntent,
                        NotificationHelper.CHANNEL_ALERT
                )
            }
        }
    }

}

fun isKeyguardLocked(context: Context):Boolean {
    return context.getSystemService<KeyguardManager>()?.isKeyguardLocked ?: true
}

fun isNotificationListenerEnabled(context: Context): Boolean {
    return NotificationManagerCompat.getEnabledListenerPackages(context).let {
        context.packageName in it
    }
}

fun openNotificationListener(context: Context) {
    // Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS".let {
        context.startActivity(Intent(it))
    }
}