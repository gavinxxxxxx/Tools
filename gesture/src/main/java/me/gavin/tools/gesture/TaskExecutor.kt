package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.core.content.getSystemService
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
        disposable = task.events
                .mapIndexed { i, e ->
                    val delay = e.delay ?: 500L
                    val duration = task.events.getOrNull(i - 1)?.let { it.duration ?: 500 } ?: 0
                    Observable.just(e).delay(delay + duration, TimeUnit.MILLISECONDS)
                }
                .toTypedArray()
                .let { Observable.concatArray(*it) }
                .repeat()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    println("$it - ${Looper.myLooper() == Looper.getMainLooper()}")
//                    tap(0.65f, 0.75f, 0.15f, 0.2f)
//                    scroll(0.65f, 0.75f, 0.15f, 0.2f)

//                    service.tap((v.layoutParams<WindowManager.LayoutParams>().x.toFloat() + v.width / 2) / getScreenWidth(),
//                            (v.layoutParams<WindowManager.LayoutParams>().y.toFloat() + v.height / 2) / getScreenHeight(), 0f, 0f)

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