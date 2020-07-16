package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import me.gavin.util.print
import kotlin.math.abs
import kotlin.math.roundToInt

private fun getVal(v: Float, d: Float, s: Int): Float {
    return ((s * (v - abs(d))).roundToInt()..(s * (v + abs(d))).roundToInt()).random().toFloat()
}

fun AccessibilityService.touch(list: List<Part>, dx: Float = 0f, dy: Float = 0f, duration: Long = 500) {
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