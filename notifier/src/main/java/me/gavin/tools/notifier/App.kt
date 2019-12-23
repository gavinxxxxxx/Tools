package me.gavin.tools.notifier

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
        sp.getString("packages", "")!!
            .split(',')
            .filter { it.isNotBlank() }
            .let {
                packages.addAll(it)
            }
        state = sp.getBoolean("state", true)
    }

    companion object {
        var app: App by Delegates.notNull()
        val packages = mutableSetOf<String>()
        var state = false
            set(value) {
                if (field != value) {
                    app.sp.edit {
                        putBoolean("state", value)
                    }
                }
                field = value
            }

        fun notifyPackagesChange(list: Collection<String>) {
            packages.clear()
            packages.addAll(list)
            app.sp.edit {
                putString("packages", packages.joinToString(","))
            }
        }
    }

}

val Context.sp get() = getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)