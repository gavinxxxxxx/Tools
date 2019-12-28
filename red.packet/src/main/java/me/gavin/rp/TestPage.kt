package me.gavin.rp

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.test.*

class TestPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)

        btnSetting.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:$packageName")))
        }

        btnListener.setOnClickListener {
            openNotificationListener(this)
        }
        btnNotify.setOnClickListener {
            NotificationHelper.openSetting(this)
        }
        btnAS.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        btnTest.setOnClickListener {
            NotificationHelper.notify(
                    this,
                    "@测试通知",
                    "@测试通知",
                    "@测试通知",
                    null,
                    NotificationHelper.CHANNEL_ALERT)
        }

        btnTest2.setOnClickListener {
            NotificationHelper.notify(
                    this,
                    "T50ZDvgrbcyD8keT",
                    "测试监听",
                    "测试监听",
                    PendingIntent.getActivity(this, 0, Intent(Settings.ACTION_APPLICATION_SETTINGS), PendingIntent.FLAG_UPDATE_CURRENT),
                    NotificationHelper.CHANNEL_DEFAULT)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        btnListener.text = "通知监听：${isNotificationListenerEnabled(this)}"
        btnNotify.text = "通知权限：${NotificationHelper.isNotificationEnabled(this)}"
        btnAS.text = "辅助功能：${checkPermission4Accessibility<RPAccessibilityService>()}"
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
