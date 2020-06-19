package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.PixelFormat
import android.os.Build
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.getSystemService
import androidx.core.view.GravityCompat
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import me.gavin.ext.layoutParams
import me.gavin.util.getScreenHeight
import me.gavin.util.getScreenWidth
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class TaskExecutor(private val service: AccessibilityService) : Disposable {

    private var disposable: Disposable? = null

    private val windowManager by lazy { service.getSystemService<WindowManager>()!! }
    private val layoutParams
        get() = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.RGBA_8888
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = GravityCompat.START or Gravity.TOP

//            x = instanceHolder().get<SharedPreferences>().getInt("quote_floating_x", 300)
//            y = instanceHolder().get<SharedPreferences>().getInt("quote_floating_y", 300)
            x = getScreenWidth() / 2
            y = getScreenHeight() / 2
        }
    private val points = mutableListOf<View>()
    private val task = Task()

    @SuppressLint("ClickableViewAccessibility")
    fun addEvent() {
        AppCompatImageView(service).apply {
            setImageResource(R.drawable.ic_adjust_black_24dp)
            setOnTouchListener { v, event ->
                v.layoutParams<WindowManager.LayoutParams>().let {
                    it.x = event.rawX.roundToInt() - v.width / 2
                    it.y = event.rawY.roundToInt() - v.height / 2
                    windowManager.updateViewLayout(v, it)
                }
                false
            }
            setOnLongClickListener {
                dialogg()
                true
            }
        }.also {
            points.add(it)
            windowManager.addView(it, layoutParams)
        }
    }

    fun removeEvent() {
        points.lastOrNull()?.let {
            windowManager.removeView(it)
            points.remove(it)
        }
    }

    fun execute() {
        if (points.isEmpty()) return
        points.forEach { v ->
            v.layoutParams<WindowManager.LayoutParams>().let {
                it.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                windowManager.updateViewLayout(v, it)
            }
        }

        play()
    }

    private fun play(index: Int = 0) {
        val event = task.events[index]
        disposable = Single.timer(3000L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    println("$index - ${Looper.myLooper() == Looper.getMainLooper()}")
//                    tap(0.65f, 0.75f, 0.15f, 0.2f)
//                    scroll(0.65f, 0.75f, 0.15f, 0.2f)
                    println("$index - $event")
//                    service.tap((v.layoutParams<WindowManager.LayoutParams>().x.toFloat() + v.width / 2) / getScreenWidth(),
//                            (v.layoutParams<WindowManager.LayoutParams>().y.toFloat() + v.height / 2) / getScreenHeight(), 0f, 0f)

                    service.execute(event)

                    play(if (index < task.events.lastIndex) index + 1 else 0)
                }
                .subscribe()
                .also {
                    disposable?.dispose()
                }
    }

    fun cancel() {
        disposable?.dispose()
        points.forEach { v ->
            v.layoutParams<WindowManager.LayoutParams>().let {
                it.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                windowManager.updateViewLayout(v, it)
            }
        }
    }

    fun toggle() {
        if (isDisposed) execute() else cancel()
    }

    override fun isDisposed() = disposable?.isDisposed != false

    override fun dispose() {
        disposable?.dispose()
        points.forEach { windowManager.removeView(it) }
    }

    private fun dialogg() {
        val root = LayoutInflater.from(service).inflate(R.layout.add_dialog, null)
        AlertDialog.Builder(service)
                .setTitle("设置")
                .setView(root)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定") { _, _ ->

                }
                .create()
                .also {
                    it?.window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
                }
                .show()
    }

}

private fun AccessibilityService.execute(e: Event) {
    when (e.action) {
        EVENT_CLICK -> tap(e.x0, e.y0, 0.1f, 0.1f)
        EVENT_SCROLL -> scroll(e.x0, e.y0, e.x1, e.y1, 0.1f, 0.1f)
        EVENT_BACK -> back()
        EVENT_HOME -> home()
        EVENT_RECENT -> recent()
        EVENT_NOTIFICATION -> notification()
    }
}