package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.PointF
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.content.getSystemService
import kotlinx.android.synthetic.main.add_dialog.view.*
import kotlinx.android.synthetic.main.add_dialog.view.etDelay
import kotlinx.android.synthetic.main.floating_widget.view.*
import kotlinx.android.synthetic.main.task_dialog.view.*
import me.gavin.ext.layoutParams
import me.gavin.ext.textTrim
import me.gavin.ext.toIntOr0
import me.gavin.ext.toLongOr0
import me.gavin.util.getScreenWidth
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
            ivSave.setOnClickListener { taskDialog(true) }
            ivClose.setOnClickListener { destroy() }

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

    fun showFloatingWindow(t: Task? = null) {
        if (widget.parent == null) {
            windowManager.addView(widget, layoutParams4widget)
        }
        t?.let { changeTask(it) } ?: let {
            task.id = 0L
            task.title = ""
            task.intro = ""
            task.repeat = Config.taskRepeatTimes
            task.repeatDelay = Config.taskRepeatDelay
            task.repeatDelayOff = Config.taskRepeatOff
        }
    }

    private fun changeTask(t: Task) {
        taskExecutor?.dispose()
        widget.ivPlay.isSelected = false
        task.targets.forEach { windowManager.removeView(it) }
        task.events.clear()
        t.events.forEach {
            when {
                it.isClick -> addClick(it)
                it.isScroll -> addScroll(it)
                else -> task.events.add(it)
            }
        }
        task.id = t.id
        task.title = t.title
        task.intro = t.intro
        task.repeat = t.repeat
        task.repeatDelay = t.repeatDelay
        task.repeatDelayOff = t.repeatDelayOff
        task.time = t.time
    }

    fun onOrientationChange(isLandscape: Boolean) {
        task.events.forEach { event ->
            event.targets?.filter { it !is Untouchable }?.forEach { v ->
                v.layoutParams<WindowManager.LayoutParams>().let { lp ->
                    (v.tag as? Part)?.let { part ->
                        lp.x = (part.x * Ext.w).roundToInt() - v.measuredWidth / 2
                        lp.y = (part.y * Ext.h).roundToInt() - v.measuredHeight / 2
                        windowManager.updateViewLayout(v, lp)
                    }
                }
            }
        }
    }

    fun destroy() {
        taskExecutor?.dispose()
        windowManager.removeView(widget)
        task.targets.forEach { windowManager.removeView(it) }
        task.events.clear()
    }

    private fun selectEventType(view: View) {
        PopupMenu(service, view).apply {
            menuInflater.inflate(R.menu.event_type, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.touch -> addCatch(false)
                    R.id.touchMulti -> addCatch(true)
                    R.id.back -> addKey(AccessibilityService.GLOBAL_ACTION_BACK)
                    R.id.home -> addKey(AccessibilityService.GLOBAL_ACTION_HOME)
                    R.id.recent -> addKey(AccessibilityService.GLOBAL_ACTION_RECENTS)
                    R.id.notifications -> addKey(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
                    R.id.quickSettings -> addKey(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
                    R.id.powerDialog -> addKey(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG, duration = 5000)
                    R.id.splitScreen -> addKey(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
                    R.id.lockScreen -> addKey(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN, duration = 5000)
                    R.id.screenshot -> addKey(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
                }
                true
            }
        }.show()
    }

    private fun addCatch(multi: Boolean) {
        CatchView(service).apply {
            isMulti = multi
            callback = {
                println("callback - $it")
                it?.let {
                    if (it.isClick) addClick(it)
                    else addScroll(it)
                    if (!multi) {
                        windowManager.removeView(this)
                    }
                } ?: let {
                    windowManager.removeView(this)
                }
            }
        }.also {
            layoutParams4event.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                windowManager.addView(it, this)
            }
        }
    }

    private fun addClick(event: Event) {
        ImageView(service).apply {
            setImageResource(R.drawable.ic_adjust_black_24dp)
            setOnTouchListener { v, e ->
                v.layoutParams<WindowManager.LayoutParams>().apply {
                    x = e.rawX.roundToInt() - v.measuredWidth / 2
                    y = e.rawY.roundToInt() - v.measuredHeight / 2
                    windowManager.updateViewLayout(v, this)
                    (tag as? Part)?.let {
                        it.x = e.rawX / Ext.w
                        it.y = e.rawY / Ext.h
                    }
                }
                false
            }
            setOnLongClickListener {
                dialogg(event)
                true
            }
        }.also {
            it.measure(
                View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.UNSPECIFIED)
            )
            layoutParams4event.apply {
                val part = event.parts.first().also { part -> it.tag = part }
                x = (part.x * Ext.w).roundToInt() - it.measuredWidth / 2
                y = (part.y * Ext.h).roundToInt() - it.measuredHeight / 2
                windowManager.addView(it, this)
            }
            event.targets = listOf(it)
            task.events.add(event)
        }
    }

    private fun addScroll(event: Event) {
        listOf(0, event.parts.lastIndex).map { i ->
            ImageView(service).apply {
                setImageResource(R.drawable.ic_adjust_black_24dp)
                if (!event.isScroll9) {
                    setOnTouchListener { v, e ->
                        v.layoutParams<WindowManager.LayoutParams>().apply {
                            x = e.rawX.roundToInt() - v.measuredWidth / 2
                            y = e.rawY.roundToInt() - v.measuredHeight / 2
                            windowManager.updateViewLayout(v, this)
                            (tag as? Part)?.let {
                                it.x = e.rawX / Ext.w
                                it.y = e.rawY / Ext.h
                            }
                            event.targets?.forEach {
                                (it as? PathView)?.notifyDataChange(event.parts)
                            }
                        }
                        false
                    }
                }
                setOnLongClickListener {
                    dialogg(event)
                    true
                }
            }.also {
                it.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        getScreenWidth(),
                        View.MeasureSpec.UNSPECIFIED
                    ),
                    View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.UNSPECIFIED)
                )
                layoutParams4event.run {
                    val part = event.parts[i].also { part -> it.tag = part }
                    x = (part.x * Ext.w).roundToInt() - it.measuredWidth / 2
                    y = (part.y * Ext.h).roundToInt() - it.measuredHeight / 2
                    windowManager.addView(it, this)
                }
            }
        }.also {
            val pathView = PathView(service)
            layoutParams4event.run {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                windowManager.addView(pathView, this)
            }
            pathView.notifyDataChange(event.parts)
            val targets = it.mapTo(ArrayList<View>()) { it }.also { it += pathView }
            event.targets = targets
            task.events.add(event)
        }
    }

    private fun addKey(action: Int, delay: Long = 500, duration: Long = 500) {
        task.events.add(Event(action, delay = delay, duration = duration))
    }

    private fun removeEvent() {
        task.events.lastOrNull()?.let {
            it.targets?.forEach { windowManager.removeView(it) }
            task.events.remove(it)
        }
    }

    private fun dialogg(event: Event) {
        val root = LayoutInflater.from(service).inflate(R.layout.add_dialog, null)
        root.etDelay.setText(event.delay?.toString())
        root.etDuration.setText(event.duration?.toString())
        AlertDialog.Builder(service)
                .setTitle("设置")
                .setView(root)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定") { _, _ ->
                    event.delay = root.etDelay.textTrim.toLongOrNull()
                    event.duration = root.etDuration.textTrim.toLongOrNull()
                }
                .setNeutralButton("任务设置") { _, _ -> taskDialog(false) }
                .create()
                .also {
                    it.window?.setType(layoutParamsType)
                }
                .show()
    }


    private fun taskDialog(withSave: Boolean) {
        val root = LayoutInflater.from(service).inflate(R.layout.task_dialog, null)
        root.etTitle.setText(task.title)
        root.etIntro.setText(task.intro)
        root.etTimes.setText(task.repeat.toString())
        root.etDelay.setText(task.repeatDelay.toString())
        root.etDelayOff.setText(task.repeatDelayOff.toString())
        AlertDialog.Builder(service)
                .setTitle("设置")
                .setView(root)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定") { _, _ ->
                    task.title = root.etTitle.textTrim
                    task.intro = root.etIntro.textTrim
                    task.repeat = root.etTimes.toIntOr0
                    task.repeatDelay = root.etDelay.toLongOr0
                    task.repeatDelayOff = root.etDelayOff.toLongOr0
                    if (withSave) saveTask2()
                }
                .also {
                    if (withSave) {
                        it.setNeutralButton("另存") { _, _ ->
                            task.id = 0L
                            task.title = root.etTitle.textTrim
                            task.intro = root.etIntro.textTrim
                            task.repeat = root.etTimes.toIntOr0
                            task.repeatDelay = root.etDelay.toLongOr0
                            task.repeatDelayOff = root.etDelay.toLongOr0
                            saveTask2()
                        }
                    }
                }
                .create()
                .also {
                    it.window?.setType(layoutParamsType)
                }
                .show()
    }

    private fun saveTask2() {
        ioThread {
            if (task.id != 0L) {
                AppDatabase.instance.taskDao.delTask(listOf(task))
            }
            val list = listOf(task)
            AppDatabase.instance.taskDao.insertTask(list).forEachIndexed { i, taskId ->
                val events = list[i].events
                events.forEach { it.taskId = taskId }
                AppDatabase.instance.taskDao.insertEvent(events).forEachIndexed { ei, eventId ->
                    val parts = events[ei].parts
                    parts.forEach { it.eventId = eventId }
                    AppDatabase.instance.taskDao.insertPart(parts)
                }
                task.id = taskId
            }
        }
    }

}

class TaskCreatorAS
class TaskCreatorRoot
class TaskExecutorAS
class TaskExecutorRoot