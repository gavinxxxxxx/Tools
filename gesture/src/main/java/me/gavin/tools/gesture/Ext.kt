package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.GravityCompat
import me.gavin.util.*

object Ext {
    val w get() = getScreenRealWidth()
    val h get() = getScreenRealHeight()
}

val layoutParamsType
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        WindowManager.LayoutParams.TYPE_PHONE
    }

val layoutParams4widget
    get() = WindowManager.LayoutParams().apply {
        type = layoutParamsType
        flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN // 可覆盖状态栏区域 TODO 可选项
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES // 可覆盖刘海区域
        }
        format = PixelFormat.RGBA_8888
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        gravity = GravityCompat.START or Gravity.TOP
        y = Ext.h / 4
    }

val layoutParams4event
    get() = WindowManager.LayoutParams().apply {
        type = layoutParamsType
        flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN // 可覆盖状态栏区域 TODO 可选项
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES // 可覆盖刘海区域
        }
        format = PixelFormat.RGBA_8888
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        gravity = GravityCompat.START or Gravity.TOP
    }