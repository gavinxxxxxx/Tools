package me.gavin.rp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class RPTileService : TileService() {

    override fun onStartListening() {
        qsTile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    @SuppressLint("InlinedApi")
    override fun onClick() {
        if (isNotificationListenerEnabled(this)) {
            if (NotificationHelper.isNotificationEnabled(this)) {
                App.state = !App.state
                qsTile.state = if (App.state) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                qsTile.updateTile()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                } else {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra("app_package", packageName)
                        .putExtra("app_uid", applicationInfo.uid)
                }.let {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivityAndCollapse(it)
                }
            }
        } else {
            startActivityAndCollapse(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private val isActive: Boolean
        get() = isNotificationListenerEnabled(this)
                && NotificationHelper.isNotificationEnabled(this)
                && App.state

}