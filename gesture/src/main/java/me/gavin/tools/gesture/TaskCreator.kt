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
import kotlinx.android.synthetic.main.floating_click.view.*
import me.gavin.ext.layoutParams
import me.gavin.ext.textTrim
import me.gavin.util.getScreenHeight
import me.gavin.util.getScreenWidth
import kotlin.math.roundToInt

@SuppressLint("ClickableViewAccessibility")
class TaskCreator(private val service: AccessibilityService) {

    private val ww = getScreenWidth()
    private val wh = getScreenHeight()

    private val task = Task()
    private var taskExecutor: TaskExecutor? = null

    private val windowManager by lazy { service.getSystemService<WindowManager>()!! }
    private val widget by lazy {
        LayoutInflater.from(service).inflate(R.layout.floating_click, null).apply {
            ivPlay.setOnClickListener {
                taskExecutor?.dispose()
                if (it.isSelected) {
                    it.isSelected = false
                    arrayOf(ivAdd, ivRemove).forEach { it.isEnabled = true }
                    task.events.forEach { e ->
                        e.target!!.layoutParams<WindowManager.LayoutParams>().let {
                            it.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            windowManager.updateViewLayout(e.target, it)
                        }
                    }
                } else if (task.events.isNotEmpty()) {
                    it.isSelected = true
                    arrayOf(ivAdd, ivRemove).forEach { it.isEnabled = false }
                    task.events.forEach { e ->
                        e.target!!.layoutParams<WindowManager.LayoutParams>().let {
                            it.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            windowManager.updateViewLayout(e.target, it)
                        }
                    }
                    taskExecutor = TaskExecutor(service, task).also { it.execute() }
                }
//                taskExecutor.toggle()
            }
            ivAdd.setOnClickListener { selectEventType(it) }
            ivRemove.setOnClickListener { removeEvent() }

            val last = PointF()
            ivDrag.setOnTouchListener { _, e ->
                if (e.action == MotionEvent.ACTION_MOVE) {
                    layoutParams<WindowManager.LayoutParams>().let {
                        it.x = (it.x + e.rawX - last.x).roundToInt().coerceIn(0, getScreenWidth() - width)
                        it.y = (it.y + e.rawY - last.y).roundToInt().coerceIn(0, getScreenHeight() - height)
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
        task.events.forEach { windowManager.removeView(it.target) }
    }

    private fun selectEventType(view: View) {
        PopupMenu(service, view).apply {
            menuInflater.inflate(R.menu.event_type, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.click -> addClick()
                }
                true
            }
        }.show()
    }

    private fun addClick() {
        ImageView(service).apply {
            setImageResource(R.drawable.ic_adjust_black_24dp)
            setOnTouchListener { v, event ->
                v.layoutParams<WindowManager.LayoutParams>().apply {
                    x = event.rawX.roundToInt() - v.measuredWidth / 2
                    y = event.rawY.roundToInt() - v.measuredHeight / 2
                    windowManager.updateViewLayout(v, this)
                    task.events.find { it.target == v }?.run {
                        x0 = event.rawX / ww
                        y0 = event.rawY / wh
                    }
                }
                false
            }
            setOnLongClickListener {
                task.events.find { it.target == this }?.let { dialogg(it) }
                true
            }
        }.also {
            it.measure(View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.UNSPECIFIED))
            layoutParams4event.apply {
                x = getScreenWidth() / 2 - it.measuredWidth / 2
                y = getScreenHeight() / 2 - it.measuredHeight / 2
                windowManager.addView(it, this)
                task.events += Event(EVENT_CLICK, target = it).apply {
                    x0 = (x + it.measuredWidth / 2).toFloat() / ww
                    y0 = (y + it.measuredHeight / 2).toFloat() / wh
                }
            }
        }
    }

    private fun removeEvent() {
        task.events.lastOrNull()?.let {
            windowManager.removeView(it.target)
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
                    event.duration = root.etDelay.textTrim.toLongOrNull()
                }
                .create()
                .also {
                    it?.window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                }
                .show()
    }

}