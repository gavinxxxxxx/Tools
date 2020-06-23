package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import me.gavin.util.getScreenHeight
import me.gavin.util.getScreenWidth
import kotlin.math.abs
import kotlin.math.roundToInt

private val w get() = getScreenWidth()
private val h get() = getScreenHeight()

private fun getVal(v: Float, d: Float, s: Int): Float {
    return ((s * (v - abs(d))).roundToInt()..(s * (v + abs(d))).roundToInt()).random().toFloat()
}

fun AccessibilityService.tap(x: Float, y: Float, dx: Float = 0f, dy: Float = 0f, duration: Long = 50) {
    val rx = getVal(x, dx, w)
    val ry = getVal(y, dy, h)
    "tap - $rx - $ry".let(::println)
    val path = Path().apply { moveTo(rx, ry) }
    GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
            .let {
                dispatchGesture(it, null, null)
//                dispatchGesture(it, object : AccessibilityService.GestureResultCallback() {
//                    override fun onCancelled(gestureDescription: GestureDescription) {
//                        super.onCancelled(gestureDescription)
//                        println("GestureResultCallback - onCancelled - ")
//                    }
//
//                    override fun onCompleted(gestureDescription: GestureDescription) {
//                        super.onCompleted(gestureDescription)
//                        println("GestureResultCallback - onCompleted - ")
//                    }
//                }, null)
            }
}

fun AccessibilityService.scroll(x0: Float, y0: Float, x1: Float, y1: Float, dx: Float = 0f, dy: Float = 0f, duration: Long = 500) {
    val path = Path().apply {
        val rx0 = getVal(x0, dx, w)
        val ry0 = getVal(y0, dy, h)
        moveTo(rx0, ry0)
        val rx1 = getVal(x1, dx, w)
        val ry1 = getVal(y1, dy, h)
        "scroll - ($rx0,$ry0) -> ($rx1,$ry1)".let(::println)
        lineTo(rx1, ry1)
    }
    GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
            .let {
                dispatchGesture(it, object : AccessibilityService.GestureResultCallback() {
                    override fun onCancelled(gestureDescription: GestureDescription) {
                        super.onCancelled(gestureDescription)
                        println("GestureResultCallback - onCancelled - ")
                    }

                    override fun onCompleted(gestureDescription: GestureDescription) {
                        super.onCompleted(gestureDescription)
                        println("GestureResultCallback - onCompleted - ")
                    }
                }, null)
            }
}

fun AccessibilityService.back() {
    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
}

fun AccessibilityService.home() {
    performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
}

fun AccessibilityService.recent() {
    performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
}

fun AccessibilityService.notification() {
    performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
}