package me.gavin.tools.gesture

import androidx.core.content.edit
import androidx.preference.PreferenceManager

object Config {

    private val sp by lazy { PreferenceManager.getDefaultSharedPreferences(App.app) }

    var taskDelay = sp.getLong("taskDelay", 0)
        set(value) {
            field = value
            sp.edit { putLong("taskDelay", value) }
        }
    var taskDelayZ = sp.getLong("taskDelayZ", 0)
        set(value) {
            field = value
            sp.edit { putLong("taskDelayZ", value) }
        }
    var taskRepeatTimes = sp.getInt("taskRepeatTimes", 0)
//        set(value) {
//            field = value
//            sp.edit { putInt("taskRepeatTimes", value) }
//        }
    var taskRepeatDelay = sp.getLong("taskRepeatDelay", 0)
        set(value) {
            field = value
            sp.edit { putLong("taskRepeatDelay", value) }
        }
    var taskRepeatDelayZ = sp.getLong("taskRepeatDelayZ", 0)
        set(value) {
            field = value
            sp.edit { putLong("taskRepeatDelayZ", value) }
        }

    var eventDelay = sp.getLong("eventDelay", 0)
        set(value) {
            field = value
            sp.edit { putLong("eventDelay", value) }
        }
    var eventDelayZ = sp.getLong("eventDelayZ", 0)
        set(value) {
            field = value
            sp.edit { putLong("eventDelayZ", value) }
        }
    var eventOffsetLimit = sp.getInt("eventOffsetLimit", 0)
        set(value) {
            field = value
            sp.edit { putInt("eventOffsetLimit", value) }
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