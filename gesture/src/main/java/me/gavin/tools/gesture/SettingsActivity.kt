package me.gavin.tools.gesture

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import kotlinx.android.synthetic.main.task_delay.view.*
import me.gavin.ext.toLongOr0

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_holder)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.holder, SettingsFragment())
            .commit()
    }
}

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // https://stackoverflow.com/questions/32477726/android-no-getedittext-method-in-edittextpreference-with-preference-support-li
        findPreference<EditTextPreference>("taskRepeatTimes")?.apply {
            summary = Config.taskRepeatTimesExt
            text = Config.taskRepeatTimes.takeIf { it > 0 }?.toString()
            setOnPreferenceChangeListener { _, newValue ->
                (newValue.toString().toIntOrNull() ?: 0).let {
                    Config.taskRepeatTimes = it
                    summary = Config.taskRepeatTimesExt
                    text = Config.taskRepeatTimes.takeIf { it > 0 }?.toString()
                }
                false
            }
        }
        findPreference<Preference>("taskRepeatDelay")?.apply {
            summary = Config.taskRepeatDelayExt
            setOnPreferenceClickListener {
                val root = LayoutInflater.from(activity).inflate(R.layout.task_delay, null)
                root.etDelay.setText(Config.taskRepeatDelay.toString())
                root.etDelayOff.setText(Config.taskRepeatOff.toString())
                AlertDialog.Builder(activity)
                    .setTitle(it.title)
                    .setView(root)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        Config.taskRepeatDelay = root.etDelay.toLongOr0
                        Config.taskRepeatOff = root.etDelayOff.toLongOr0
                        summary = Config.taskRepeatDelayExt
                    }
                    .create()
                    .show()
                true
            }
        }

        findPreference<Preference>("eventDelay")?.apply {
            summary = Config.eventDelayExt
            setOnPreferenceClickListener {
                val root = LayoutInflater.from(activity).inflate(R.layout.task_delay, null)
                root.etDelay.setText(Config.eventDelay.toString())
                root.etDelayOff.setText(Config.eventDelayOff.toString())
                AlertDialog.Builder(activity)
                    .setTitle(it.title)
                    .setView(root)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        Config.eventDelay = root.etDelay.toLongOr0
                        Config.eventDelayOff = root.etDelayOff.toLongOr0
                        summary = Config.eventDelayExt
                    }
                    .create()
                    .show()
                true
            }
        }
        findPreference<EditTextPreference>("eventDurationOff")?.apply {
            summary = Config.eventDurationOffExt
            text = Config.eventDurationOff.toString()
            setOnPreferenceChangeListener { _, newValue ->
                (newValue.toString().toIntOrNull() ?: 0).let {
                    Config.eventDurationOff = it
                    summary = Config.eventDurationOffExt
                    text = Config.eventDurationOff.toString()
                }
                false
            }
        }
        findPreference<EditTextPreference>("eventLocationOff")?.apply {
            summary = Config.eventLocationOffExt
            text = Config.eventLocationOff.takeIf { it > 0 }?.toString()
            setOnPreferenceChangeListener { _, newValue ->
                (newValue.toString().toIntOrNull() ?: 0).let {
                    Config.eventLocationOff = it
                    summary = Config.eventLocationOffExt
                    text = Config.eventLocationOff.takeIf { it > 0 }?.toString()
                }
                false
            }
        }
        findPreference<SwitchPreferenceCompat>("event2OffsetEnable")?.apply {
            isChecked = Config.event2OffsetEnable
            setOnPreferenceChangeListener { _, newValue ->
                (newValue.toString().toBoolean()).let {
                    Config.event2OffsetEnable = it
                    isChecked = Config.event2OffsetEnable
                }
                false
            }
        }
        findPreference<SwitchPreferenceCompat>("event2Offset2")?.apply {
            isChecked = Config.event2Offset2
            setOnPreferenceChangeListener { _, newValue ->
                (newValue.toString().toBoolean()).let {
                    Config.event2Offset2 = it
                    isChecked = Config.event2Offset2
                }
                false
            }
        }
        findPreference<SwitchPreferenceCompat>("event9OffsetEnable")?.apply {
            isChecked = Config.event9OffsetEnable
            setOnPreferenceChangeListener { _, newValue ->
                (newValue.toString().toBoolean()).let {
                    Config.event9OffsetEnable = it
                    isChecked = Config.event9OffsetEnable
                }
                false
            }
        }
    }

}
