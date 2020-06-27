package me.gavin.tools.gesture

import android.view.View

/**
 * action: click/scroll/back/home/recent/notification
 * x0&y0
 * x1&y1
 * delay
 * duration
 */
const val EVENT_CLICK = "click"
const val EVENT_SCROLL = "scroll"
const val EVENT_BACK = "back"
const val EVENT_HOME = "home"
const val EVENT_RECENT = "recent"
const val EVENT_NOTIFICATION = "notification"

data class Event(
        val action: String,
        var x0: Float? = null,
        var y0: Float? = null,
        var x1: Float? = null,
        var y1: Float? = null,
        var dx: Float? = null,
        var dy: Float? = null,
        var delay: Long? = null,
        var duration: Long? = null,
        var targets: List<View>? = null
)

data class Sub(val x: Float, val y: Float, val duration: Long)

data class Task(var title: String = "", var intro: String? = null, val events: MutableList<Event> = arrayListOf())

val Task.targets get() = events.flatMap { it.targets ?: emptyList() }
fun Task.findEventByView(target: View): Event? {
    return events.find { it.targets?.contains(target) == true }
}