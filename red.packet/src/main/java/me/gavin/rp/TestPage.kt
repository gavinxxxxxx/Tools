package me.gavin.rp

import android.annotation.SuppressLint
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

        btnTest.setOnClickListener {
            NotificationHelper.notify(
                    this,
                    "@测试通知",
                    "@测试",
                    "@测试",
                    null,
                    NotificationHelper.CHANNEL_ALERT)
        }

        btnTest2.setOnClickListener {
            NotificationHelper.notify(
                    this,
                    "测试通知",
                    "测试",
                    "测试",
                    null,
                    NotificationHelper.CHANNEL_DEFAULT)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        btnListener.text = "通知监听：${isNotificationListenerEnabled(this)}"
        btnNotify.text = "通知权限：${NotificationHelper.isNotificationEnabled(this)}"
    }

}
