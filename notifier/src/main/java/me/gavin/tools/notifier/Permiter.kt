package me.gavin.tools.notifier

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService


inline fun <reified T : Service> Context.isServiceRunning(): Boolean {
    val className = T::class.java.name
    return getSystemService<ActivityManager>()!!
            .getRunningServices(100)
            .any {
                it.service.className == className
            }
}

fun Context.doIfPermissionGrant4Floating(block: () -> Unit) {
    if (checkPermission4Floating()) {
        block.invoke()
    } else {
//        "该功能需要悬浮窗权限".toast()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:$packageName")))
        } else {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    .setData(Uri.parse("package:$packageName")))
        }
    }
}

fun Context.checkPermission4Floating(): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        runCatching {
            val appOps = getSystemService<AppOpsManager>()!!
            val checkOp = appOps.javaClass.getMethod("checkOp", Int::class.java, Int::class.java, String::class.java)
            checkOp.isAccessible = true
            val result = checkOp.invoke(appOps, 24, Binder.getCallingUid(), packageName)
            result == AppOpsManager.MODE_ALLOWED
        }.getOrDefault(false)
    } else {
        Settings.canDrawOverlays(this)
    }
}

inline fun <reified T : AccessibilityService> Context.doIfPermissionGrant4Accessibility(block: () -> Unit) {
    if (checkPermission4Accessibility<T>()) {
        block.invoke()
    } else {
//        "该功能需要开启辅助功能权限".toast()
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
}

inline fun <reified T : AccessibilityService> Context.checkPermission4Accessibility(): Boolean {
    val className = T::class.java.name
    return runCatching {
        Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0)
                .takeIf { it == 1 }
                ?.let { Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) }
                ?.split(":")
                ?.any { it.equals("$packageName/$className", ignoreCase = true) }
                ?: false
    }.getOrDefault(false)
}

fun Context.doIfPermissionGrant4Notification(channelId: String? = null, block: () -> Unit) {
    if (checkPermission4Notification(channelId)) {
        block.invoke()
    } else {
//        "该功能需要通知权限".toast()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                if (channelId != null) {
                    Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                } else {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                }.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            else -> {
                Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                        .putExtra("app_package", packageName)
                        .putExtra("app_uid", applicationInfo.uid)
            }
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).let {
            startActivity(it)
        }
    }
}

fun Context.checkPermission4Notification(channelId: String?): Boolean {
    return NotificationManagerCompat.from(this).areNotificationsEnabled()
}

@SuppressLint("InlinedApi")
fun Context.doIfPermissionGrant4NotificationListener(block: () -> Unit) {
    if (checkPermission4NotificationListener()) {
        block.invoke()
    } else {
//        "该功能需要通知监听权限".toast()
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }
}

fun Context.checkPermission4NotificationListener(): Boolean {
    return packageName in NotificationManagerCompat.getEnabledListenerPackages(this)
}