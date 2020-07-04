package me.gavin.tools.gesture

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import me.gavin.util.dp2px
import me.gavin.util.dp2pxF
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class CatchView(context: Context) : View(context) {

    var isMulti = false
    lateinit var callback: (Event?) -> Unit

    private val closeSize = 28f.dp2pxF()
    private val closePadding = 6f.dp2pxF()
    private val paint by lazy {
        Paint().also {
            it.color = Color.RED
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(0x40000000)

        if (isMulti) {
            canvas.drawRect(
                    width - closeSize + closePadding,
                    height - closeSize + closePadding,
                    width - closePadding,
                    height - closePadding,
                    paint)
        }
    }

    private val touchSlop by lazy { ViewConfiguration.get(context).scaledTouchSlop }
    private val parts = mutableListOf<Part>()
    private var moved = false

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                parts.clear()
                parts += Part(e.x, e.y)
                moved = false
            }
            MotionEvent.ACTION_MOVE -> {
                parts += Part(e.x, e.y, e.eventTime - e.downTime)
                if (abs(e.x - parts.first().x) > touchSlop
                        || abs(e.y - parts.first().y) > touchSlop) {
                    moved = true
                }
            }
            MotionEvent.ACTION_UP -> {
                parts += Part(e.x, e.y, e.eventTime - e.downTime)
                if (moved) { // 有移动
                    parts.forEach {
                        it.x /= Ext.w
                        it.y /= Ext.h
                    }
                    val event = Event(EVENT_TOUCH, parts.toMutableList())
                    callback.invoke(event)
                } else if (!isMulti // 未移动 & 非连续|非结束
                        || parts.first().x < width - closeSize || parts.first().y < height - closeSize) {
                    val part = Part(parts.first().x / Ext.w, parts.first().y / Ext.h, e.eventTime - e.downTime)
                    val event = Event(EVENT_TOUCH, mutableListOf(part))
                    callback.invoke(event)
                } else { // 结束连续
                    callback.invoke(null)
                }
            }
        }
        return true
    }

//    private inner class SingleTapDetector : GestureDetector.SimpleOnGestureListener() {
//        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
//            "onSingleTapConfirmed".print()
//            callback.invoke(Part(e.rawX, e.rawY))
//            return true
//        }
//
//        override fun onDoubleTap(e: MotionEvent): Boolean {
//            "onDoubleTap".print()
//            callback.invoke(Part(e.rawX, e.rawY))
//            return true
//        }
//
//        override fun onLongPress(e: MotionEvent) {
//            "onLongPress".print()
//            callback.invoke(Part(e.rawX, e.rawY))
//        }
//
//        override fun onScroll(e1: MotionEvent, e2: MotionEvent, dx: Float, dy: Float): Boolean {
//            "onScroll - $dx - $dy".print()
////            callback.invoke(Part(e1.rawX, e1.rawY))
//            return true
//        }
//    }

}