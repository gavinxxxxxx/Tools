package me.gavin.tools.gesture

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

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
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // https://stackoverflow.com/questions/32477726/android-no-getedittext-method-in-edittextpreference-with-preference-support-li
        findPreference<EditTextPreference>("taskRepeatTimes")?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                newValue.toString().toIntOrNull()?.let {
                    Config.taskRepeatTimes = it
                }
                false
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        super.onDisplayPreferenceDialog(preference)
    }
}