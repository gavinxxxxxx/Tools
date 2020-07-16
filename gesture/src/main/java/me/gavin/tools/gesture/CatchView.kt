package me.gavin.tools.gesture

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
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
                paint
            )
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
                if (!moved && (abs(e.x - parts.first().x) > touchSlop || abs(e.y - parts.first().y) > touchSlop)) {
                    moved = true
                }
                if (moved) {
                    parts += Part(e.x, e.y, e.eventTime - e.downTime)
                }
            }
            MotionEvent.ACTION_UP -> {
                parts += Part(e.x, e.y, e.eventTime - e.downTime)
                if (isMulti) {
                    if (moved) {
                        parts.forEach {
                            it.x /= Ext.w
                            it.y /= Ext.h
                        }
                        val event = Event(ACTION_CATCH).also { it.parts = parts.toMutableList() }
                        callback.invoke(event)
                    } else if (parts.first().x < width - closeSize || parts.first().y < height - closeSize) {
                        val part = Part(
                            parts.first().x / Ext.w,
                            parts.first().y / Ext.h,
                            e.eventTime - e.downTime
                        )
                        val event = Event(ACTION_CATCH).also { it.parts = mutableListOf(part) }
                        callback.invoke(event)
                    } else {
                        callback.invoke(null)
                    }
                } else {
                    if (moved) {
                        val start = Part(
                            parts.first().x / Ext.w,
                            parts.first().y / Ext.h,
                            e.eventTime - e.downTime
                        )
                        val end = Part(
                            parts.last().x / Ext.w,
                            parts.last().y / Ext.h,
                            e.eventTime - e.downTime
                        )
                        val event = Event(ACTION_CATCH).also { it.parts = mutableListOf(start, end) }
                        callback.invoke(event)
                    } else {
                        val part = Part(
                            parts.first().x / Ext.w,
                            parts.first().y / Ext.h,
                            e.eventTime - e.downTime
                        )
                        val event = Event(ACTION_CATCH).also { it.parts = mutableListOf(part) }
                        callback.invoke(event)
                    }
                }
            }
        }
        return true
    }

}