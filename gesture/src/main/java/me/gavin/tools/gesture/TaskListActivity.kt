package me.gavin.tools.gesture

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import me.gavin.base.BindingActivity
import me.gavin.base.BindingAdapter
import me.gavin.databinding.WidgetPagerBinding
import me.gavin.tools.gesture.databinding.TaskActivityBinding
import me.gavin.util.doIfPermissionGrant4Accessibility
import me.gavin.util.doIfPermissionGrant4Floating
import me.gavin.util.isServiceRunning
import me.gavin.widget.PagerViewModel
import org.jetbrains.anko.startActivity

class TaskListActivity : BindingActivity<TaskActivityBinding>() {

    private val list = mutableListOf<Task>()
    private val adapter by lazy {
        BindingAdapter(this, list, R.layout.task_item).apply {
            footers.add(footerPager)
            binding.recycler.adapter = this
        }
    }
    private val footerPager: WidgetPagerBinding by lazy {
        WidgetPagerBinding.inflate(layoutInflater).also { it.vm = footerPagerVM }
    }
    private val footerPagerVM: PagerViewModel by lazy {
        PagerViewModel(binding.recycler, list, headerLoadEnable = false) {
            listData(true)
        }
    }

    override val layoutId = R.layout.task_activity

    override fun afterCreate(savedInstanceState: Bundle?) {
        binding.fab.setOnClickListener { tryAddTask() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity<SettingsActivity>()
        return true
    }

    private fun tryAddTask() {
        isServiceRunning<TaskAccessibilityService>().let(::println)

        doIfPermissionGrant4Floating {
            doIfPermissionGrant4Accessibility<TaskAccessibilityService> {

            }
        }
    }

    private fun listData(isMore: Boolean) {

    }

}
