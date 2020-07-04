package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.PointF
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.content.getSystemService
import androidx.core.view.GravityCompat
import androidx.core.view.plusAssign
import androidx.core.view.setPadding
import kotlinx.android.synthetic.main.add_dialog.view.*
import kotlinx.android.synthetic.main.floating_widget.view.*
import me.gavin.ext.layoutParams
import me.gavin.ext.textTrim
import me.gavin.util.dp2px
import kotlin.math.roundToInt

@SuppressLint("ClickableViewAccessibility")
class TaskCreator(private val service: AccessibilityService) {

    private val task = Task()
    private var taskExecutor: TaskExecutor? = null

    private val windowManager by lazy { service.getSystemService<WindowManager>()!! }
    private val widget by lazy {
        LayoutInflater.from(service).inflate(R.layout.floating_widget, null).apply {
            ivPlay.setOnClickListener {
                taskExecutor?.dispose()
                if (it.isSelected) {
                    it.isSelected = false
                    arrayOf(ivAdd, ivRemove, ivClose).forEach { it.isEnabled = true }
                    task.targets.filter { it !is Untouchable }.forEach { v ->
                        v.layoutParams<WindowManager.LayoutParams>().let {
                            it.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            windowManager.updateViewLayout(v, it)
                        }
                    }
                } else if (task.events.isNotEmpty()) {
                    it.isSelected = true
                    arrayOf(ivAdd, ivRemove, ivClose).forEach { it.isEnabled = false }
                    task.targets.filter { it !is Untouchable }.forEach { v ->
                        v.layoutParams<WindowManager.LayoutParams>().let {
                            it.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            windowManager.updateViewLayout(v, it)
                        }
                    }
                    taskExecutor = TaskExecutor(service, task).also { it.execute() }
                }
            }
            ivAdd.setOnClickListener { selectEventType(it) }
            ivRemove.setOnClickListener { removeEvent() }
            ivClose.setOnClickListener { }

            val last = PointF()
            ivDrag.setOnTouchListener { _, e ->
                if (e.action == MotionEvent.ACTION_MOVE) {
                    layoutParams<WindowManager.LayoutParams>().let {
                        it.x = (it.x + e.rawX - last.x).roundToInt().coerceIn(0, Ext.w - width)
                        it.y = (it.y + e.rawY - last.y).roundToInt().coerceIn(0, Ext.h - height)
                        windowManager.updateViewLayout(this, it)
                    }
                }
                last.set(e.rawX, e.rawY)
                true
            }
        }
    }

    fun showFloatingWindow() {
        if (widget.parent == null) {
            windowManager.addView(widget, layoutParams4widget)
        }
    }

    fun destroy() {
        taskExecutor?.dispose()
        windowManager.removeView(widget)
        task.targets.forEach { windowManager.removeView(it) }
    }

    private fun selectEventType(view: View) {
        PopupMenu(service, view).apply {
            menuInflater.inflate(R.menu.event_type, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.touch -> addCatch(false)
                    R.id.touchMulti -> addCatch(true)
                    R.id.back -> addKey(EVENT_BACK)
                    R.id.home -> addKey(EVENT_HOME)
                    R.id.recent -> addKey(EVENT_RECENT)
                    R.id.notification -> addKey(EVENT_NOTIFICATION)
                }
                true
            }
        }.show()
    }

    private fun addCatch(multi: Boolean) {
        CatchView(service).apply {
            isMulti = multi
            callback = {
                it?.let {
                    task.events += it
                    println("task - ${task.events.last().parts.size} - $task")
                    if (!multi) {
                        windowManager.removeView(this)
                    }
                } ?: let {
                    windowManager.removeView(this)
                }
            }
        }.also {
            layoutParams4event.apply {
                x = 0
                y = 0
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                windowManager.addView(it, this)
            }
        }
    }

    private fun addCatching() {
        FrameLayout(service).apply {
            setBackgroundColor(0x40000000)
            var paths: MutableList<Part>? = null
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    paths = mutableListOf()
                }
                paths!!.add(Part(event.rawX / Ext.w, event.rawY / Ext.h, event.eventTime - event.downTime))
                if (event.action == MotionEvent.ACTION_UP) {
                    task.events += Event(EVENT_CATCH, parts = paths!!, targets = null).apply {

                    }
                    println("task - ${task.events.firstOrNull()?.parts?.size} - $task")
                }
                false
            }
            this += ImageView(service).also {
                it.setImageResource(R.drawable.ic_baseline_close_24)
                it.setPadding(4f.dp2px())
                it.layoutParams = FrameLayout.LayoutParams(32f.dp2px(), 32f.dp2px()).apply {
                    gravity = GravityCompat.END or Gravity.BOTTOM
                }
                it.setOnClickListener {
                    windowManager.removeView(this)
                }
            }
        }.also {
            layoutParams4event.apply {
                x = 0
                y = 0
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                windowManager.addView(it, this)
            }
        }
    }

    private fun addKey(event: String) {
        task.events += Event(event, delay = 500, duration = 500)
    }

    private fun removeEvent() {
        task.events.lastOrNull()?.let {
            it.targets?.forEach { windowManager.removeView(it) }
            task.events.remove(it)
        }
    }

    private fun dialogg(event: Event) {
        val root = LayoutInflater.from(service).inflate(R.layout.add_dialog, null)
        event.delay?.let { root.etDelay.setText(it.toString()) }
        event.duration?.let { root.etDuration.setText(it.toString()) }
        AlertDialog.Builder(service)
                .setTitle("设置")
                .setView(root)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定") { _, _ ->
                    event.delay = root.etDelay.textTrim.toLongOrNull()
                    event.duration = root.etDuration.textTrim.toLongOrNull()
                }
                .create()
                .also {
                    it.window?.setType(layoutParamsType)
                }
                .show()
    }

}

class TaskCreatorAS
class TaskCreatorRoot
class TaskExecutorAS
class TaskExecutorRoot