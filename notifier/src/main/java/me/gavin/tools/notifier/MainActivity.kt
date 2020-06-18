package me.gavin.tools.notifier

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.gavin.base.BindingAdapter
import java.text.Collator
import java.util.*

class MainActivity : AppCompatActivity() {

    private var isInEdit = false

    private val list = mutableListOf<AppInfo>()
    private val adapter by lazy {
        BindingAdapter(this, list, R.layout.activity_main_item).also {
            recycler.adapter = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { onFabClick() }

        getData()
    }

    private fun onFabClick() {
        doIfPermissionGrant4NotificationListener {
            doIfPermissionGrant4Notification {
                if (!isInEdit) {
                    isInEdit = true
                    fab.setImageResource(R.drawable.ic_done_black_24dp)
                    getData()
                } else {
                    list.filter { it.checked == true }
                        .map { it.pkg }
                        .let {
                            App.notifyPackagesChange(it)
                        }
                    isInEdit = false
                    App.state = true
                    fab.setImageResource(R.drawable.ic_add_black_24dp)
                    getData()
                }
            }
        }
    }

    private fun getData() {
        packageManager.getInstallApps()
            .let {
                list.clear()
                list.addAll(it)
                adapter.notifyDataSetChanged()
            }
    }

    private fun PackageManager.getInstallApps(): List<AppInfo> {
        return getInstalledPackages(0)
            .map {
                val pkg = it.packageName
                val ai = it.applicationInfo
                val name = ai.loadLabel(this).toString()
                val icon = ai.loadIcon(this)
                val flags = ai.flags
                val isSystem = (flags and ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM
                val checked = if (isInEdit) pkg in App.packages else null
                AppInfo(pkg, name, icon, isSystem, checked)
            }
            .filter { !it.isSystem }
            .filter { isInEdit || it.pkg in App.packages }
            .sortedWith(Comparator { o1, o2 ->
                Collator.getInstance(Locale.CHINESE).compare(o1.name, o2.name)
            })
    }
}

class AppInfo(
    val pkg: String,
    val name: String,
    val icon: Drawable,
    val isSystem: Boolean,
    var checked: Boolean? = null
) {
    val checkBoxVisibleExt get() = if (checked != null) View.VISIBLE else View.GONE
}