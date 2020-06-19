package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import io.reactivex.disposables.Disposable
import me.gavin.util.RxBus
import me.gavin.util.checkPermission4Floating
import me.gavin.util.log
import me.gavin.util.toast

class TaskAccessibilityService : AccessibilityService() {

    private val taskCreator by lazy { TaskCreator(this) }

    private var dispose: Disposable? = null

    override fun onCreate() {
        super.onCreate()
        dispose = RxBus.toObservable<String>()
                .doOnNext {
                    prepareCreateTask()
                }
                .subscribe({}, {})

        prepareCreateTask()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        if (intent.getBooleanExtra("withCreateTask", false)) {
//            prepareCreateTask()
//        }
        return super.onStartCommand(intent, flags, startId)
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
        taskCreator.destroy()
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        "onAccessibilityEvent - $event".log()
    }

}