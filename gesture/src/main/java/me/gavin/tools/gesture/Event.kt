package me.gavin.tools.gesture

import android.view.View

/**
 * action: click/scroll/back/home/recent/notification
 * x0&y0
 * x1&y1
 * delay
 * duration
 */
// const val EVENT_CLICK = "click"
// const val EVENT_SCROLL = "scroll"
const val EVENT_TOUCH = "touch"
const val EVENT_CATCH = "catch"
const val EVENT_BACK = "back"
const val EVENT_HOME = "home"
const val EVENT_RECENT = "recent"
const val EVENT_NOTIFICATION = "notification"

data class Task(var title: String = "", var intro: String? = null, val events: MutableList<Event> = arrayListOf()) {
    val delay = 0L
    val repeatDelay = 0L
}

data class Event(
        val action: String,
        var parts: MutableList<Part> = mutableListOf(),
        var dx: Float? = null,
        var dy: Float? = null,
        var delay: Long? = null,
        var duration: Long? = null,
        var targets: List<View>? = null
) {
    val isClick get() = parts.size == 1
    val isScroll get() = parts.size > 1
    val isScrollMulti get() = parts.size > 2

    val delayExt get() = delay ?: 100L
    val durationExt get() = duration ?: parts.lastOrNull()?.time ?: durationDefault
    val durationDefault get() = if (isClick) 50L else if (isScroll) 100L else 500L
}

data class Part(var x: Float, var y: Float, var time: Long = 0)

val Task.targets get() = events.flatMap { it.targets ?: emptyList() }
fun Task.findEventByView(target: View): Event? {
    return events.find { it.targets?.contains(target) == true }
}