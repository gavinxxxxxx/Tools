package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import me.gavin.util.getScreenHeight
import me.gavin.util.getScreenWidth
import kotlin.math.abs
import kotlin.math.roundToInt

fun AccessibilityService.tap(x: Float, y: Float, dx: Float = 0f, dy: Float = 0f) {
    val rx = ((getScreenWidth() * (x - abs(dx))).roundToInt()..(getScreenWidth() * (x + abs(dx))).roundToInt()).random().toFloat()
    val ry = ((getScreenHeight() * (y - abs(dy))).roundToInt()..(getScreenHeight() * (y + abs(dy))).roundToInt()).random().toFloat()
    "tap - $rx - $ry".let(::println)
    val path = Path().apply { moveTo(rx, ry) }
    GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 100, 50))
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

fun AccessibilityService.scroll(x1: Float, y1: Float, x2: Float, y2: Float, dx: Float = 0f, dy: Float = 0f) {
    val path = Path().apply {
        val rx1 = ((getScreenWidth() * (x1 - abs(dx))).roundToInt()..(getScreenWidth() * (x1 + abs(dx))).roundToInt()).random().toFloat()
        val ry1 = ((getScreenHeight() * (y1 - abs(dy))).roundToInt()..(getScreenHeight() * (y1 + abs(dy))).roundToInt()).random().toFloat()
        moveTo(rx1, ry1)
        val rx2 = ((getScreenWidth() * (x2 - abs(dx))).roundToInt()..(getScreenWidth() * (x2 + abs(dx))).roundToInt()).random().toFloat()
        val ry2 = ((getScreenHeight() * (y2 - abs(dy))).roundToInt()..(getScreenHeight() * (y2 + abs(dy))).roundToInt()).random().toFloat()
        "scroll - ($rx1,$ry1) -> ($rx2,$ry2)".let(::println)
        lineTo(rx2, ry2)
    }
    GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 20, 500))
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