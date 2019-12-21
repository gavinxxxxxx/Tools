package me.gavin.rp

import android.app.Application
import android.content.Context
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
        state = sp.getBoolean("state", false)
    }

    companion object {
        var app: App by Delegates.notNull()
        var state = false
            set(value) {
                if (field != value) {
                    app.sp.edit {
                        putBoolean("state", value)
                    }
                }
                field = value
            }
    }

}

private val Context.sp get() = getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)