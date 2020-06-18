package me.gavin.tools.notifier

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import me.gavin.base.cast
import me.gavin.base.takeOrElse

class RPListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!App.state) return

        sbn.notification?.extras?.let { t ->
            val title = t[Notification.EXTRA_TITLE_BIG]
                .cast<String?>()
                .let {
                    it.takeOrElse({ it.isNullOrBlank() }) {
                        t[Notification.EXTRA_TITLE].cast()
                    }.orEmpty()
                }
            val text = t[Notification.EXTRA_BIG_TEXT]
                .cast<String?>()
                .let {
                    it.takeOrElse({ it.isNullOrBlank() }) {
                        t[Notification.EXTRA_TEXT].cast()
                    }.orEmpty()
                }
            println(" ---------------------------------------------------------------- ")
            println(title)
            println(text)
            println(" ---------------------------------------------------------------- ")

            if (sbn.packageName in App.packages || title == "T50ZDvgrbcyD8keT") {
                NotificationHelper.notify(
                    context = this,
                    channel = getAppName(sbn.packageName),
                    tag = sbn.tag,
                    id = sbn.id,
                    title = title,
                    content = text,
                    intent = sbn.notification.contentIntent
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