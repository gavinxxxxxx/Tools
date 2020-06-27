package me.gavin.tools.gesture

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View

class CatchingView(context: Context) : View(context) {

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(0x40000000)
    }

}