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
    var taskRepeatDelay = sp.getString("taskRepeatDelay", "0")?.toLongOrNull() ?: 0
        set(value) {
            field = value
            sp.edit { putString("taskRepeatDelay", value.toString()) }
        }
    var taskRepeatOff = (sp.getString("taskRepeatOff", "0")?.toLongOrNull() ?: 0).coerceAtLeast(taskRepeatDelay)
        set(value) {
            field = value
            sp.edit { putString("taskRepeatOff", value.toString()) }
        }

    var eventDelay = sp.getString("eventDelay", "0")?.toLongOrNull() ?: 0
        set(value) {
            field = value
            sp.edit { putString("eventDelay", value.toString()) }
        }
    var eventDelayOff = (sp.getString("eventDelayOff", "0")?.toLongOrNull() ?: 0).coerceAtLeast(taskRepeatDelay)
        set(value) {
            field = value
            sp.edit { putString("eventDelayOff", value.toString()) }
        }
    var eventDurationOff = (sp.getString("eventDelayOff", "0")?.toFloatOrNull() ?: 1f).coerceAtLeast(1f)
        set(value) {
            field = value
            sp.edit { putString("eventDelayOff", value.toString()) }
        }
    var eventLocationOff = sp.getString("eventLocationOff", "0")?.toIntOrNull() ?: 0
        set(value) {
            field = value
            sp.edit { putString("eventLocationOff", value.toString()) }
        }
    var event2Offset2 = sp.getBoolean("event2Offset2", true)
        set(value) {
            field = value
            sp.edit { putBoolean("event2Offset2", value) }
        }
    var eventOffsetEnable = sp.getBoolean("eventOffsetEnable", false)
        set(value) {
            field = value
            sp.edit { putBoolean("eventOffsetEnable", value) }
        }

}