package me.gavin.tools.gesture

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import me.gavin.ext.layoutParams
import me.gavin.util.print
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class ScrollView(context: Context, val windowManager: WindowManager) : View(context) {

    val sPoint = PointF(200f, 200f)
    val ePoint = PointF(400f, 400f)

    val radius = 20f

    var l = (min(sPoint.x, ePoint.x) - radius).roundToInt()
    var t = (min(sPoint.y, ePoint.y) - radius).roundToInt()
    val w get() = (abs(sPoint.x - ePoint.x) + radius * 2f).roundToInt()
    val h get() = (abs(sPoint.y - ePoint.y) + radius * 2f).roundToInt()

    private val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 3f
    }

    private var target: PointF? = null

    private val lp by lazy { layoutParams<WindowManager.LayoutParams>() }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                if (isInTouch(event.rawX, event.rawY, ePoint.x, ePoint.y, radius)) {
//                    target = ePoint
//                } else if (isInTouch(event.rawX, event.rawY, sPoint.x, sPoint.y, radius)) {
//                    target = sPoint
//                } else {
//                    target = null
//                }
////                target = ePoint
//            }
//            MotionEvent.ACTION_MOVE -> {
//                target?.set(event.rawX, event.rawY)
//                l = (min(sPoint.x, ePoint.x) - radius).roundToInt()
//                t = (min(sPoint.y, ePoint.y) - radius).roundToInt()
////                requestLayout()
//
//                layoutParams<WindowManager.LayoutParams>().apply {
//                    x = l
//                    y = t
//                    width = w
//                    height = h
//                    windowManager.updateViewLayout(this@ScrollView, this)
//                }
//            }
//        }
//        return true
//    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(sPoint.x - lp.x, sPoint.y - lp.y, radius, paint.apply {
            style = Paint.Style.STROKE
        })
        canvas.drawCircle(ePoint.x - lp.x, ePoint.y - lp.y, radius, paint.apply {
            style = Paint.Style.FILL_AND_STROKE
        })
    }
}

fun isInTouch(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float): Boolean {
    return (x1 - x2).pow(2) + (y1 - y2).pow(2) < radius.pow(2)
}