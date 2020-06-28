package me.gavin.tools.gesture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.View
import org.jetbrains.anko.withAlpha

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
        canvas.drawColor(Color.RED.withAlpha(0x40))

        points.forEach {
            canvas.drawPoint(it.x, it.y, paint)
        }

        points.map { listOf(it.x * width, it.y * height) }
                .flatMap {
                    it
                }.let {
                    canvas.drawLines(it.toFloatArray(), paint)
                }
    }

}