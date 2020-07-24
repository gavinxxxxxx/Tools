package me.gavin.tools.gesture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import me.gavin.util.print
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private fun getVal(v: Float, d: Float, s: Int): Float {
    return ((s * (v - abs(d))).roundToInt()..(s * (v + abs(d))).roundToInt()).random().toFloat()
}

fun Event.toObservable(service: AccessibilityService): Observable<*> {
    return when {
        action != ACTION_CATCH -> {
            Observable.timer(delayExt, TimeUnit.MILLISECONDS)
                    .doOnNext { service.performGlobalAction(action) }
                    .delay(durationExt, TimeUnit.MILLISECONDS)
        }
        parts.isEmpty() -> {
            Observable.timer(delayExt, TimeUnit.MILLISECONDS)
                    .delay(durationExt, TimeUnit.MILLISECONDS)
        }
        parts.size > 2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            event2observableV26(service)
        }
        else -> {
            event2observable(service)
        }
    }
}

fun Event.event2observable(service: AccessibilityService): Observable<*> {
    return Observable.timer(delayExt, TimeUnit.MILLISECONDS)
            .map {
                Path().apply {
                    moveTo(getVal(parts.first().x, 0f, Ext.w), getVal(parts.first().y, 0f, Ext.h))
                    for (i in 1..parts.lastIndex) {
                        lineTo(getVal(parts[i].x, 0f, Ext.w), getVal(parts[i].y, 0f, Ext.h))
                    }
                }
            }
            .map { GestureDescription.StrokeDescription(it, 0, durationExt) }
            .flatMap {
                GestureDescription.Builder()
                        .addStroke(it)
                        .build()
                        .gd2Observable(service)
                        .onErrorReturn { Unit }
                        .zipWith<Any, Any>(
                                Observable.timer(it.duration, TimeUnit.MILLISECONDS),
                                BiFunction { _, _ -> Unit }
                        )
            }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Event.event2observableV26(service: AccessibilityService): Observable<*> {
    val timeScale = durationExt.toFloat() / parts.last().time
    return (1..parts.lastIndex).map { i ->
        Path().apply {
            moveTo(getVal(parts[i - 1].x, 0f, Ext.w), getVal(parts[i - 1].y, 0f, Ext.h))
            lineTo(getVal(parts[i].x, 0f, Ext.w), getVal(parts[i].y, 0f, Ext.h))
        }
    }.let { paths ->
        var strokeDescription: GestureDescription.StrokeDescription? = null
        paths.mapIndexed { i, path ->
            val duration = ((parts[i + 1].time - parts[i].time) * timeScale).roundToLong()
            strokeDescription?.let {
                it.continueStroke(path, 0, duration, i < paths.lastIndex).also {
                    strokeDescription = it
                }
            } ?: let {
                GestureDescription.StrokeDescription(path, 0, duration, i < parts.lastIndex - 1).also {
                    strokeDescription = it
                }
            }
        }
    }.map {
        GestureDescription.Builder()
                .addStroke(it)
                .build()
                .gd2Observable(service)
                .onErrorReturn { Unit }
                .zipWith<Any, Any>(
                        Observable.timer(it.duration, TimeUnit.MILLISECONDS),
                        BiFunction { _, _ -> Unit }
                )
    }.let { ls ->
        Observable.timer(delayExt, TimeUnit.MILLISECONDS)
                .flatMap { Observable.concatArray(*ls.toTypedArray()) }
    }
}

fun GestureDescription.gd2Observable(service: AccessibilityService) = Observable.create<Any> { emitter ->
    service.dispatchGesture(this, object : AccessibilityService.GestureResultCallback() {
        override fun onCancelled(gestureDescription: GestureDescription) {
            super.onCancelled(gestureDescription)
            emitter.onError(IllegalStateException("onCancelled - "))
        }

        override fun onCompleted(gestureDescription: GestureDescription) {
            super.onCompleted(gestureDescription)
            emitter.onNext(gestureDescription)
            emitter.onComplete()
        }
    }, null).let {
        // if (!it) emitter.onError(IllegalStateException())
        "dispatchGesture - $it - ${Looper.myLooper() == Looper.getMainLooper()}".print()
    }
}/*.subscribeOn(AndroidSchedulers.mainThread())*/

