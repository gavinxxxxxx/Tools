package me.gavin.rp

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit
import kotlin.properties.Delegates

/**
 * 描述：
 *
 * @author CoderPig on 2018/04/12 11:43.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
        state = sp.getBoolean("自动抢红包", false)
    }

    companion object {
        var app: App by Delegates.notNull()
        var state = false
            set(value) {
                if (field != value) {
                    field = value
                    app.sp.edit { putBoolean("自动抢红包", value) }
                }
            }
    }

}

private val Context.sp get() = PreferenceManager.getDefaultSharedPreferences(this)