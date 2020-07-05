package me.gavin.tools.gesture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.view.View

interface Untouchable

class PathView(context: Context) : View(context), Untouchable {

    private val points = mutableListOf<PointF>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun notifyDataChange(list: List<Part>) {
        points.clear()
        points.addAll(list.map { PointF(it.x, it.y) })
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (points.size < 2) return

        points.forEach {
            canvas.drawPoint(it.x, it.y, paint)
        }

        for (i in 0 until points.lastIndex) {
            canvas.drawLine(
                points[i].x * width,
                points[i].y * height,
                points[i + 1].x * width,
                points[i + 1].y * height,
                paint
            )
        }
    }

}