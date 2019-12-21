package me.gavin.tools.gesture

data class Event(
        val action: Int,
        val x: Float,
        val y: Float,
        val x2: Float,
        val y2: Float,
        val delay: Long,
        val duration: Long
)

data class Task(var title: String,
                var intro: String,
                val events: MutableList<Event> = arrayListOf())