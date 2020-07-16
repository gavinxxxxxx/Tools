package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.view.View
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import java.util.concurrent.TimeUnit

class TaskExecutor(private val service: AccessibilityService, private val task: Task) : Disposable {

    private var disposable: Disposable? = null

    private val windowManager by lazy { service.getSystemService<WindowManager>()!! }

    private val points = mutableListOf<View>()

    fun execute() {
        println(Gson().toJson(task))
        disposable = task.events
                .mapIndexed { i, e ->
                    val wait = task.events.getOrNull(i - 1)?.durationExt ?: task.delay
                    val delay = e.delayExt
                    Observable.just(e).delay(wait + delay, TimeUnit.MILLISECONDS)
                }
                .toTypedArray()
                .let { Observable.concatArray(*it) }
                .repeatWhen {
                    val wait = task.events.lastOrNull()?.durationExt ?: 500L
                    val delay = task.repeatDelay
                    it.delay(wait + delay, TimeUnit.MILLISECONDS)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    service.execute(it)
                }
                .subscribeBy()
                .also { disposable?.dispose() }
    }

    fun cancel() {
        disposable?.dispose()
    }

    fun toggle() {
        if (isDisposed) execute() else cancel()
    }

    override fun isDisposed() = disposable?.isDisposed != false

    override fun dispose() {
        disposable?.dispose()
        points.forEach { windowManager.removeView(it) }
    }

}