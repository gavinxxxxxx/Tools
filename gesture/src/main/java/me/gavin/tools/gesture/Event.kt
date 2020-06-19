package me.gavin.tools.gesture

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

data class Event( // 点击/滑动/主页/返回/通知
        val action: String,
        val x0: Float,
        val y0: Float,
        val x1: Float,
        val y1: Float,
        val delay: Long,
        val duration: Long
)

data class Task(var title: String = "", var intro: String? = null, val events: MutableList<Event> = arrayListOf())