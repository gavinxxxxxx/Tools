package me.gavin.tools.gesture

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.gavin.base.BindingActivity
import me.gavin.base.BindingAdapter
import me.gavin.databinding.WidgetPagerBinding
import me.gavin.net.subscribeE
import me.gavin.tools.gesture.databinding.TaskActivityBinding
import me.gavin.util.RxBus
import me.gavin.util.doIfPermissionGrant4Accessibility
import me.gavin.util.doIfPermissionGrant4Floating
import me.gavin.util.isServiceRunning
import me.gavin.widget.PagerViewModel
import me.jessyan.autosize.internal.CancelAdapt
import org.jetbrains.anko.startActivity

class TaskListActivity : BindingActivity<TaskActivityBinding>(), CancelAdapt {

    private val list = mutableListOf<Task>()
    private val adapter by lazy {
        BindingAdapter(this, list, R.layout.task_item) {
            tryAddTask2(Gson().run { fromJson(toJson(list[it]), Task::class.java) })
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

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START or ItemTouchHelper.END) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewHolder.adapterPosition.let {
                    val t = list[it]

                    AppDatabase.instance
                            .taskDao
                            .listEventByTaskId(t.id)
                            .doOnSuccess {
                                AppDatabase.instance.taskDao.delTask(listOf(t))
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSuccess {
                                it.map {
                                    it.event.apply { parts = it.parts.toMutableList() }
                                }.let {
                                    Snackbar.make(binding.recycler, "已删除", Snackbar.LENGTH_LONG)
                                            .setAction("撤销") {
                                                ioThread {
                                                    val list = listOf(t)
                                                    AppDatabase.instance.taskDao.insertTask(list).forEachIndexed { i, taskId ->
                                                        val events = list[i].events
                                                        events.forEach { it.taskId = taskId }
                                                        AppDatabase.instance.taskDao.insertEvent(events).forEachIndexed { ei, eventId ->
                                                            val parts = events[ei].parts
                                                            parts.forEach { it.eventId = eventId }
                                                            AppDatabase.instance.taskDao.insertPart(parts)
                                                        }
                                                    }
                                                }
                                            }
                                            .show()
                                }
                            }
                            .autoDisposable(scopeProvider)
                            .subscribeE()
                }
            }
        }).attachToRecyclerView(binding.recycler)

        AppDatabase.instance
                .taskDao
                .listTask()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    println(it)
                    list.clear()
                    list.addAll(it)
                    adapter.notifyDataSetChanged()
                }
                .autoDisposable(scopeProvider)
                .subscribeE()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity<SettingsActivity>()
        return true
    }

    private fun tryAddTask2(task: Task) {
        AppDatabase.instance
                .taskDao
                .listEventByTaskId(task.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    it.map {
                        it.event.apply { parts = it.parts.toMutableList() }
                    }.let {
                        task.events.addAll(it)
                        tryAddTask(task)
                    }
                }
                .autoDisposable(scopeProvider)
                .subscribeE()
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

val jsonWzry = """
        {
          "title": "某耀刷金币（示例）",
          "intro": "闯关模式 三分钟循环一次",
          "events": [
            {
              "action": 0,
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
              "action": 0,
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
              "action": 0,
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
      "title": "某宝消费券",
      "events": [
        {
          "action": 0,
          "delay": 29,
          "parts": [
            {
              "time": 57,
              "x": 0.35462964,
              "y": 0.4212121
            }
          ]
        },
        {
          "action": 0,
          "delay": 37,
          "parts": [
            {
              "time": 52,
              "x": 0.61851853,
              "y": 0.4034632
            }
          ]
        }
      ]
    }
""".trimIndent()
