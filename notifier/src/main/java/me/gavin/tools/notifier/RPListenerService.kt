package me.gavin.tools.notifier

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat

class RPListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
//        if (!App.state) return

        sbn.notification?.extras?.let {
            val title = it[Notification.EXTRA_TITLE] as? String
                    ?: it[Notification.EXTRA_TITLE_BIG] as? String ?: return
            val text = it[Notification.EXTRA_TEXT] as? String
                    ?: it[Notification.EXTRA_BIG_TEXT] as? String ?: return
            System.out.println(" ---------------------------------------------------------------- ")
            System.out.println(title)
            System.out.println(text)
            System.out.println(" ---------------------------------------------------------------- ")

//            if ("com.tencent.mm" == sbn.packageName && "[微信红包]" in text
//                    || BuildConfig.APPLICATION_ID == sbn.packageName && "测试通知" == title) {
            if (BuildConfig.APPLICATION_ID != sbn.packageName || !title.startsWith('@')) {
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

fun openNotificationListener(context: Context) {
    // Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS".let {
        context.startActivity(Intent(it))
    }
}

fun Context.getAppName(pkg: String): String {
    return packageManager.getPackageInfo(pkg, 0)
            .applicationInfo
            .labelRes
            .let {
                resources.getString(it)
            }
}