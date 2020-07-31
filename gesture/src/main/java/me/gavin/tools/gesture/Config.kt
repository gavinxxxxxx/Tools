package me.gavin.tools.gesture

import androidx.core.content.edit
import androidx.preference.PreferenceManager

object Config {

    private val sp by lazy { PreferenceManager.getDefaultSharedPreferences(App.app) }

    var taskRepeatTimes = sp.getString("taskRepeatTimes", "0")?.toIntOrNull() ?: 0
        set(value) {
            field = value
            sp.edit { putString("taskRepeatTimes", value.toString()) }
        }
    val taskRepeatTimesExt get() = "任务将执行 ${if (taskRepeatTimes > 0) taskRepeatTimes else "∞"} 次"
    var taskRepeatDelay = sp.getString("taskRepeatDelay", "0")?.toLongOrNull() ?: 0
        set(value) {
            field = value
            sp.edit { putString("taskRepeatDelay", value.toString()) }
        }
    var taskRepeatOff = (sp.getString("taskRepeatOff", "0")?.toLongOrNull() ?: 0)
        set(value) {
            field = value
            sp.edit { putString("taskRepeatOff", value.toString()) }
        }
    val taskRepeatDelayExt get() = "任务再执行时将延迟 $taskRepeatDelay ~ ${taskRepeatDelay + taskRepeatOff} ms"

    var eventDelay = sp.getString("eventDelay", "0")?.toLongOrNull() ?: 0
        set(value) {
            field = value
            sp.edit { putString("eventDelay", value.toString()) }
        }
    var eventDelayOff = (sp.getString("eventDelayOff", "0")?.toLongOrNull() ?: 0)
        set(value) {
            field = value
            sp.edit { putString("eventDelayOff", value.toString()) }
        }
    val eventDelayExt get() = "事件执行将延迟 $eventDelay ~ ${eventDelay + eventDelayOff} ms"
    var eventDurationOff = (sp.getString("eventDelayOff", "50")?.toIntOrNull() ?: 50)
        set(value) {
            field = value
            sp.edit { putString("eventDelayOff", value.toString()) }
        }
    val eventDurationOffExt
        get() = when {
            eventDurationOff == 100 -> "事件持续时长不偏移"
            eventDurationOff < 100 -> "事件持续时长将是原时长的 $eventDurationOff% ~ 100%"
            else -> "事件持续时长将是原时长的 100% ~ $eventDurationOff%"
        }
    var eventLocationOff = sp.getString("eventLocationOff", "2")?.toIntOrNull() ?: 2
        set(value) {
            field = value
            sp.edit { putString("eventLocationOff", value.toString()) }
        }
    val eventLocationOffExt get() = "事件位置将相对屏幕短边偏移 $eventLocationOff%"
    var event2OffsetEnable = sp.getBoolean("event2OffsetEnable", false)
        set(value) {
            field = value
            sp.edit { putBoolean("event2OffsetEnable", value) }
        }
    var event2Offset2 = sp.getBoolean("event2Offset2", true)
        set(value) {
            field = value
            sp.edit { putBoolean("event2Offset2", value) }
        }
    var event9OffsetEnable = sp.getBoolean("event9OffsetEnable", false)
        set(value) {
            field = value
            sp.edit { putBoolean("event9OffsetEnable", value) }
        }

}