package me.gavin.tools.notifier

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.gavin.tools.notifier.databinding.ActivityMainItemBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        packageManager.getInstallApps().let {
            recycler.adapter = AppInfoAdapter(this, it.toMutableList())
        }

        fab.setOnClickListener {
            doIfPermissionGrant4NotificationListener {
                doIfPermissionGrant4Notification {

                }
            }
        }
    }

}

class AppInfoAdapter(context: Context, list: MutableList<AppInfo>) :
        RecyclerAdapter<AppInfo, ActivityMainItemBinding>(context, list) {
    override val layoutId = R.layout.activity_main_item
    override fun onBind(holder: RecyclerHolder<ActivityMainItemBinding>, position: Int, t: AppInfo) {
        holder.binding.icon.setImageDrawable(t.icon)
        holder.binding.text.text = t.name
    }
}

class AppInfo(val name: String, val pkg: String, val icon: Drawable, val isSystem: Boolean, val checked: Boolean = false)

fun PackageManager.getInstallApps(): List<AppInfo> {
    return getInstalledPackages(0).map {
        val pkg = it.packageName
        val aif = it.applicationInfo
        val name = aif.loadLabel(this).toString()
        val icon = aif.loadIcon(this)
        val flags = aif.flags
        val isSystem = (flags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM
        AppInfo(name, pkg, icon, isSystem)
    }.filter { !it.isSystem }
}