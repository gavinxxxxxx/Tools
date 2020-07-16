package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import me.gavin.util.print
import kotlin.math.abs
import kotlin.math.roundToInt

private fun getVal(v: Float, d: Float, s: Int): Float {
    return ((s * (v - abs(d))).roundToInt()..(s * (v + abs(d))).roundToInt()).random().toFloat()
}

fun AccessibilityService.dispatchGesture(list: List<Part>, dx: Float = 0f, dy: Float = 0f, duration: Long = 500) {
    if (list.isEmpty()) return
    val path = Path().apply {
        moveTo(getVal(list.first().x, dx, Ext.w), getVal(list.first().y, dy, Ext.h))
        for (i in 1..list.lastIndex) {
            lineTo(getVal(list[i].x, dx, Ext.w), getVal(list[i].y, dy, Ext.h))
        }
    }
    GestureDescription.Builder()
        .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
        .build()
        .let {
            dispatchGesture(it, object : AccessibilityService.GestureResultCallback() {
                override fun onCancelled(gestureDescription: GestureDescription) {
                    println("GestureResultCallback - onCancelled")
                }

                override fun onCompleted(gestureDescription: GestureDescription) {
                    super.onCompleted(gestureDescription)
                    println("GestureResultCallback - onCompleted")
                }
            }, null).let { "dispatchGesture - $it".print() }
        }
}

fun AccessibilityService.dispatchGestureV26(list: List<Part>, dx: Float = 0f, dy: Float = 0f, duration: Long = 500) {
    if (list.isEmpty()) return

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || list.size < 3) {
        dispatchGesture(list, dx, dy, duration)
        return
    }

    (1..list.lastIndex).map { i ->
        Path().apply {
            moveTo(getVal(list[i - 1].x, 0f, Ext.w), getVal(list[i - 1].y, 0f, Ext.h))
            lineTo(getVal(list[i].x, 0f, Ext.w), getVal(list[i].y, 0f, Ext.h))
        }
    }.let { paths ->
        var strokeDescription: GestureDescription.StrokeDescription? = null
        paths.mapIndexed { i, path ->
            strokeDescription?.let {
                it.continueStroke(path, 0, list[i + 1].time - list[i].time, i < paths.lastIndex).also {
                    strokeDescription = it
                }
            } ?: let {
                GestureDescription.StrokeDescription(path, 0, list[i + 1].time - list[i].time, i < list.lastIndex - 1).also {
                    strokeDescription = it
                }
            }
        }
    }.also {
        dododo(it, 0)
    }
}

private fun AccessibilityService.dododo(list: List<GestureDescription.StrokeDescription>, i: Int) {
    if (i > list.lastIndex) return
    GestureDescription.Builder()
        .addStroke(list[i])
        .build()
        .let {
            dispatchGesture(it, object : AccessibilityService.GestureResultCallback() {
                override fun onCancelled(gestureDescription: GestureDescription) {
                    println("GestureResultCallback - onCancelled")
                }

                override fun onCompleted(gestureDescription: GestureDescription) {
                    super.onCompleted(gestureDescription)
                    println("GestureResultCallback - onCompleted")
                    dododo(list, i + 1)
                }
            }, null).let { "dispatchGesture - $it".print() }
        }

}

