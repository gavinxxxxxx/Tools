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
import kotlinx.android.synthetic.main.floating_click.view.*
import me.gavin.ext.layoutParams
import me.gavin.ext.textTrim
import me.gavin.util.dp2px
import me.gavin.util.getScreenHeight
import me.gavin.util.getScreenWidth
import kotlin.math.min
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
//                taskExecutor.toggle()
            }
            ivAdd.setOnClickListener { selectEventType(it) }
            ivRemove.setOnClickListener { removeEvent() }
            ivClose.setOnClickListener { }

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
        task.targets.forEach { windowManager.removeView(it) }
    }

    private fun selectEventType(view: View) {
        PopupMenu(service, view).apply {
            menuInflater.inflate(R.menu.event_type, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.click -> addClick()
                    R.id.scroll -> addScroll()
                    R.id.catching -> addCatching()
                    R.id.back -> addKey(EVENT_BACK)
                    R.id.home -> addKey(EVENT_HOME)
                    R.id.recent -> addKey(EVENT_RECENT)
                    R.id.notification -> addKey(EVENT_NOTIFICATION)
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
                    task.findEventByView(v)?.run {
                        x0 = event.rawX / ww
                        y0 = event.rawY / wh
                    }
                }
                false
            }
            setOnLongClickListener {
                task.findEventByView(it)?.let { dialogg(it) }
                true
            }
        }.also {
            it.measure(View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.UNSPECIFIED))
            layoutParams4event.apply {
                x = getScreenWidth() / 2 - it.measuredWidth / 2
                y = getScreenHeight() / 2 - it.measuredHeight / 2
                windowManager.addView(it, this)
                task.events += Event(EVENT_CLICK, targets = listOf(it)).apply {
                    x0 = (x + it.measuredWidth / 2).toFloat() / ww
                    y0 = (y + it.measuredHeight / 2).toFloat() / wh
                }
            }
        }
    }

    private fun addScroll() {
        (0..1).map { i ->
            ImageView(service).apply {
                setImageResource(R.drawable.ic_adjust_black_24dp)
                setOnTouchListener { v, event ->
                    v.layoutParams<WindowManager.LayoutParams>().apply {
                        x = event.rawX.roundToInt() - v.measuredWidth / 2
                        y = event.rawY.roundToInt() - v.measuredHeight / 2
                        windowManager.updateViewLayout(v, this)
                        task.findEventByView(v)?.run {
                            if (i == 0) {
                                x0 = event.rawX / ww
                                y0 = event.rawY / wh
                            } else if (i == 1) {
                                x1 = event.rawX / ww
                                y1 = event.rawY / wh
                            }
                            targets?.find { it is PathView }?.let {
                                (it as PathView).notifyDataChange(x0!! to y0!!, x1!! to y1!!)
                            }
                        }
                    }
                    false
                }
                setOnLongClickListener {
                    task.findEventByView(it)?.let { dialogg(it) }
                    true
                }
            }.let {
                it.measure(View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(getScreenWidth(), View.MeasureSpec.UNSPECIFIED))
                layoutParams4event.run {
                    x = getScreenWidth() / 2 - it.measuredWidth / 2
                    y = getScreenHeight() / 4 * (i + 1)
                    windowManager.addView(it, this)
                    it to PointF(x + it.measuredWidth * 0.5f, y + it.measuredHeight * 0.5f)
                }
            }
        }.also {
            val pathView = PathView(service)
            layoutParams4event.run {
                x = 0
                y = 0
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                windowManager.addView(pathView, this)
            }
            val targets = it.mapTo(ArrayList<View>()) { it.first }.also { it += pathView }
            task.events += Event(EVENT_SCROLL, targets = targets).apply {
                x0 = it.first().second.x / ww
                y0 = it.first().second.y / wh
                x1 = it.last().second.x / ww
                y1 = it.last().second.y / wh
                pathView.notifyDataChange(x0!! to y0!!, x1!! to y1!!)
            }
        }
    }

    private fun addCatching() {
        FrameLayout(service).apply {
            setBackgroundColor(0x40000000)
            val events = mutableListOf<MotionEvent>()
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    events.clear()
                }
                events += event
                if (event.action == MotionEvent.ACTION_UP) {

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