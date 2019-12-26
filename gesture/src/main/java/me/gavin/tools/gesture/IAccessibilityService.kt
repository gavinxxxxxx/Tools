package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.app.AlertDialog
import android.graphics.PixelFormat
import android.os.Build
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.getSystemService
import androidx.core.view.GravityCompat
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.floating_click.view.*
import me.gavin.util.*
import me.gavin.ext.layoutParams
import me.gavin.ext.onClick
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class IAccessibilityService : AccessibilityService() {

    private var w = 0
    private var h = 0

    override fun onCreate() {
        super.onCreate()
        w = getScreenWidth()
        h = getScreenHeight()

        showFloatingWindow()
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(widget)
        points.forEach { windowManager.removeView(it) }
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        "onAccessibilityEvent - $event".log()
    }

    private val windowManager by lazy { applicationContext.getSystemService<WindowManager>()!! }
    private val layoutParams by lazy {
        WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    // WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN // 可覆盖状态栏
            format = PixelFormat.RGBA_8888
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = GravityCompat.START or Gravity.TOP

//            x = instanceHolder().get<SharedPreferences>().getInt("quote_floating_x", 300)
//            y = instanceHolder().get<SharedPreferences>().getInt("quote_floating_y", 300)
            x = 0
            y = getStatusHeight()
        }
    }
    private val widget by lazy {
        LayoutInflater.from(this).inflate(R.layout.floating_click, null).apply {
            ivPlay.setOnClickListener {
                if (disposable?.isDisposed == false) {
                    disposable?.dispose()
                    points.forEach { v ->
                        v.layoutParams<WindowManager.LayoutParams>().let {
                            it.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            windowManager.updateViewLayout(v, it)
                        }
                    }
                } else {
                    points.takeIf { it.isNotEmpty() }?.let {
                        points.forEach { v ->
                            v.layoutParams<WindowManager.LayoutParams>().let {
                                it.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                windowManager.updateViewLayout(v, it)
                            }
                        }
                        play()
                    }
                }
            }
            ivAdd.onClick { addView() }
            ivRemove.onClick {
                points.lastOrNull()?.let {
                    points.remove(it)
                    windowManager.removeView(it)
                }
            }
        }
        // setOnTouchListener(OnTouchListener())
    }
    private val points = mutableListOf<View>()

    private fun showFloatingWindow() {
        if (checkPermission4Floating()) {
            if (widget.parent == null) {
                windowManager.addView(widget, layoutParams)
            }
//            initShowType()
        } else {
            "该功能需要悬浮窗权限".toast()
            stopSelf()
        }
    }

    private fun addView() {
        AppCompatImageView(this).apply {
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
            windowManager.addView(it, lp)
        }
    }

    private val lp
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
            x = w / 2
            y = h / 2
        }

    private var disposable: Disposable? = null

    private fun play(index: Int = 0) {
        val v = points[index]
        disposable = Single.timer(1000L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    println("$index - ${Looper.myLooper() == Looper.getMainLooper()}")
//                    tap(0.65f, 0.75f, 0.15f, 0.2f)
//                    scroll(0.65f, 0.75f, 0.15f, 0.2f)
                    println("$index - ${v.x} - ${v.y}")
                    tap((v.layoutParams<WindowManager.LayoutParams>().x.toFloat() + v.width / 2) / getScreenWidth(),
                            (v.layoutParams<WindowManager.LayoutParams>().y.toFloat() + v.height / 2) / getScreenHeight(), 0f, 0f)

                    play(if (index < points.lastIndex) index + 1 else 0)
                }
                .subscribe()
                .also {
                    disposable?.dispose()
                }
    }

    private fun dialogg() {
        val root = LayoutInflater.from(this).inflate(R.layout.add_dialog, null)
        AlertDialog.Builder(this)
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