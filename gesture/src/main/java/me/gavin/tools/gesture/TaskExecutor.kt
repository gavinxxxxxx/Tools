package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.view.View
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import me.gavin.base.takeOrElse
import java.util.concurrent.TimeUnit

class TaskExecutor(private val service: AccessibilityService, private val task: Task) : Disposable {

    private var disposable: Disposable? = null

    private val windowManager by lazy { service.getSystemService<WindowManager>()!! }

    private val points = mutableListOf<View>()

    fun execute() {
        println(Gson().toJson(task))
        disposable = Observable.timer(500, TimeUnit.MILLISECONDS)
                .flatMap {
                    task.events.map {
                        it.toObservable(service)
                    }.toTypedArray().let {
                        Observable.concatArray(*it)
                    }
                }
                .repeatWhen {
                    it.takeOrElse(task.repeat <= 0) {
                        it.zipWith<Any, Any>(Observable.range(0, task.repeat), BiFunction { _, _ -> Unit })
                    }.delay(task.repeatDelayExt, TimeUnit.MILLISECONDS)
                }
                .subscribe({

                }, {
                    it.printStackTrace()
                })
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