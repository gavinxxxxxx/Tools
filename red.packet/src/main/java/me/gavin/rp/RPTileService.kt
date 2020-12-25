package me.gavin.rp

import android.annotation.SuppressLint
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class RPTileService : TileService() {

    override fun onStartListening() {
        qsTile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    @SuppressLint("InlinedApi")
    override fun onClick() {
        doIfPermissionGrant4NotificationListener {
            doIfPermissionGrant4Notification {
                doIfPermissionGrant4Accessibility<RPAccessibilityService> {
                    App.state = !App.state
                    qsTile.state = if (App.state) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                    qsTile.updateTile()
                }
            }
        }
    }

    private val isActive: Boolean
        get() = App.state
                && checkPermission4NotificationListener()
                && checkPermission4Notification()
                && checkPermission4Accessibility<RPAccessibilityService>()
}