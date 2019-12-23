package me.gavin.tools.notifier

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat


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

            if (sbn.packageName in App.packages || title == "T50ZDvgrbcyD8keT") {
                NotificationHelper.notify(
                    this,
                    "@$title",
                    "@$text",
                    getAppName(sbn.packageName),
                    sbn.notification.contentIntent,
                    NotificationHelper.CHANNEL_ALERT
                )
                cancelNotification(sbn.key)
            }
        }
    }

}

fun isNotificationListenerEnabled(context: Context): Boolean {
    return NotificationManagerCompat.getEnabledListenerPackages(context).let {
        context.packageName in it
    }
}

fun Context.getAppName(pkg: String): String {
    return packageManager.let {
        it.getApplicationInfo(pkg, PackageManager.GET_META_DATA).loadLabel(it).toString()
    }
}

inline fun <reified T : NotificationListenerService> Context.toggleNotificationListenerService() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        NotificationListenerService.requestRebind(ComponentName(this, T::class.java))
    } else {
        packageManager.setComponentEnabledSetting(
            ComponentName(this, T::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            ComponentName(this, T::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
        )
    }
}