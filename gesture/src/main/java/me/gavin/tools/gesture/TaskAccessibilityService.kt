package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.content.res.Configuration
import android.view.accessibility.AccessibilityEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import me.gavin.util.RxBus
import me.gavin.util.checkPermission4Floating
import me.gavin.util.log
import me.gavin.util.toast
import java.lang.ref.WeakReference

class TaskAccessibilityService : AccessibilityService() {

    private val taskCreator by lazy { TaskCreator(this) }
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        App.taskService = WeakReference(this)
//        prepareCreateTask()

        RxBus.toObservable<StateToggle>()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                if (it.state) {
                    taskCreator.showFloatingWindow(it.task)
                } else {
                    taskCreator.destroy()
                }
            }
            .subscribeBy()
            .addTo(compositeDisposable)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        taskCreator.onOrientationChange(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
    }

//    private fun prepareCreateTask() {
//        if (checkPermission4Floating()) {
//            taskCreator.showFloatingWindow()
//        } else {
//            "该功能需要悬浮窗权限".toast()
//            stopSelf()
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
        taskCreator.destroy()
        App.taskService = null
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        "onAccessibilityEvent - $event".log()
    }

}

class StateToggle(val state: Boolean, val task: Task? = null)