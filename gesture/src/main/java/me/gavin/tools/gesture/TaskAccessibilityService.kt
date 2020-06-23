package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import me.gavin.util.checkPermission4Floating
import me.gavin.util.log
import me.gavin.util.toast
import java.lang.ref.WeakReference

class TaskAccessibilityService : AccessibilityService() {

    private val taskCreator by lazy { TaskCreator(this) }

    override fun onCreate() {
        super.onCreate()
        App.taskService = WeakReference(this)
        prepareCreateTask()
    }

    private fun prepareCreateTask() {
        if (checkPermission4Floating()) {
            taskCreator.showFloatingWindow()
        } else {
            "该功能需要悬浮窗权限".toast()
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        App.taskService = null
        taskCreator.destroy()
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        "onAccessibilityEvent - $event".log()
    }

}