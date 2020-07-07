package me.gavin.tools.gesture

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.gson.Gson
import me.gavin.base.BindingActivity
import me.gavin.base.BindingAdapter
import me.gavin.databinding.WidgetPagerBinding
import me.gavin.tools.gesture.databinding.TaskActivityBinding
import me.gavin.util.*
import me.gavin.widget.PagerViewModel
import org.jetbrains.anko.startActivity

class TaskListActivity : BindingActivity<TaskActivityBinding>() {

    private val list = mutableListOf<Task>()
    private val adapter by lazy {
        BindingAdapter(this, list, R.layout.task_item) {
            tryAddTask(Gson().run { fromJson(toJson(list[it]), Task::class.java) })
        }.apply {
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

        list.clear()
        val test = Gson().fromJson(jsonTest, Task::class.java)
        list += test
        val wzry = Gson().fromJson(jsonWzry, Task::class.java)
        list += wzry
        val zfb = Gson().fromJson(jsonZfbxfq, Task::class.java)
        list += zfb
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity<SettingsActivity>()
        return true
    }

    private fun tryAddTask(task: Task? = null) {
        isServiceRunning<TaskAccessibilityService>().let(::println)

        doIfPermissionGrant4Floating {
            doIfPermissionGrant4Accessibility<TaskAccessibilityService> {
                // startService(Intent(this, TaskAccessibilityService::class.java))
                RxBus.post(StateToggle(true, task))
            }
        }
    }

    private fun listData(isMore: Boolean) {

    }

}

val jsonTest = """
        {
          "title": "测试",
          "intro": "",
          "events": [
            {
              "action": "touch",
              "parts": [
                {
                  "x": 0.35,
                  "y": 0.65,
                  "time": 0
                },
                {
                  "x": 0.65,
                  "y": 0.35,
                  "time": 2020
                }
              ]
            }
          ]
        }
    """.trimIndent()
val jsonWzry = """
        {
          "title": "王者荣耀刷金币",
          "intro": "",
          "events": [
            {
              "action": "touch",
              "delay": 2000,
              "parts": [
                {
                  "x": 0.7,
                  "y": 0.81,
                  "time": 100
                }
              ]
            },
            {
              "action": "touch",
              "delay": 180000,
              "parts": [
                {
                  "x": 0.5,
                  "y": 0.9,
                  "time": 100
                }
              ]
            },
            {
              "action": "touch",
              "delay": 2000,
              "parts": [
                {
                  "x": 0.83,
                  "y": 0.92,
                  "time": 100
                }
              ]
            }
          ]
        }
    """.trimIndent()
val jsonZfbxfq = """
    {
      "title": "支付宝消费券",
      "events": [
        {
          "action": "touch",
          "delay": 59,
          "parts": [
            {
              "time": 23,
              "x": 0.35462964,
              "y": 0.4212121
            }
          ]
        },
        {
          "action": "touch",
          "delay": 77,
          "parts": [
            {
              "time": 54,
              "x": 0.61851853,
              "y": 0.4034632
            }
          ]
        }
      ]
    }
""".trimIndent()
