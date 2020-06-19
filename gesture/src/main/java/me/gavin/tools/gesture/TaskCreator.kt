package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.content.getSystemService
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.floating_click.view.*
import me.gavin.ext.onClick
import me.gavin.util.getStatusHeight

class TaskCreator(service: AccessibilityService) {

    private val windowManager by lazy { service.getSystemService<WindowManager>()!! }
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
        LayoutInflater.from(service).inflate(R.layout.floating_click, null).apply {
            ivPlay.setOnClickListener { taskExecutor.toggle() }
            ivAdd.onClick { taskExecutor.addEvent() }
            ivRemove.onClick { taskExecutor.removeEvent() }
        }
        // setOnTouchListener(OnTouchListener())
    }

    private val taskExecutor by lazy { TaskExecutor(service) }

    fun showFloatingWindow() {
        if (widget.parent == null) {
            windowManager.addView(widget, layoutParams)
        }
    }

    fun destroy() {
        windowManager.removeView(widget)
        taskExecutor.dispose()
    }

}