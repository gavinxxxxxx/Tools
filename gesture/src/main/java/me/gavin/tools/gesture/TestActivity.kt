package me.gavin.tools.gesture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.test.*
import me.gavin.util.*

class TestActivity : AppCompatActivity() {

    private val last = PointF()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)

        Switch01.isChecked = checkPermission4Floating()
        Switch01.setOnClickListener {
            doIfPermissionGrant4Floating {

            }
        }

        Switch02.isChecked = checkPermission4Accessibility<TaskAccessibilityService>()
        Switch02.setOnClickListener {
            doIfPermissionGrant4Accessibility<TaskAccessibilityService> {

            }
        }

        fab.setOnClickListener {

        }
    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        fab.translationX = event.x - fab.width / 2
//        fab.translationY = event.y - fab.height / 2 - 250
//        return super.onTouchEvent(event)
//    }

}

class Test(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    val paint = Paint().apply {
        strokeWidth = 5f
    }

    val points = mutableListOf<PointF>()

    val path = Path()

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            points.clear()
        }
        points += PointF(e.x, e.y)
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        points.forEach {
            canvas.drawPoint(it.x, it.y, paint)
        }

        path.reset()
        points.firstOrNull()?.let { path.moveTo(it.x, it.y) }
//        path.quadTo()
    }
}
