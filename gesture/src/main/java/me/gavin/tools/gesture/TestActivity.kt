package me.gavin.tools.gesture

import android.graphics.PointF
import android.os.Bundle
import android.view.MotionEvent
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

        Switch02.isChecked = checkPermission4Accessibility<IAccessibilityService>()
        Switch02.setOnClickListener {
            doIfPermissionGrant4Accessibility<IAccessibilityService> {

            }
        }

        fab.setOnClickListener {

        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        fab.translationX = event.x - fab.width / 2
        fab.translationY = event.y - fab.height / 2 - 250
        return super.onTouchEvent(event)
    }

}