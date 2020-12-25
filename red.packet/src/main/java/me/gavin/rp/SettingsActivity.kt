package me.gavin.rp

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commitAllowingStateLoss()
    }

}

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<TwoStatePreference>("通知监听")?.run {
            setOnPreferenceChangeListener { _, _ -> false }
            setOnPreferenceClickListener {
                openNotificationListener(requireContext())
                true
            }
        }
        findPreference<TwoStatePreference>("通知")?.run {
            setOnPreferenceChangeListener { _, _ -> false }
            setOnPreferenceClickListener {
                NotificationHelper.openSetting(requireContext())
                true
            }
        }
        findPreference<TwoStatePreference>("辅助功能")?.run {
            setOnPreferenceChangeListener { _, _ -> false }
            setOnPreferenceClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                true
            }
        }
        findPreference<TwoStatePreference>("自动抢红包")?.run {
            setOnPreferenceChangeListener { _, newValue ->
                App.state = newValue as Boolean
                false
            }
        }

        findPreference<Preference>("测试通知")?.run {
            setOnPreferenceClickListener {
                NotificationHelper.notify(
                    requireContext(),
                    "@测试通知",
                    "@测试通知",
                    "@测试通知",
                    null,
                    NotificationHelper.CHANNEL_ALERT
                )
                true
            }
        }
        findPreference<Preference>("测试监听")?.run {
            setOnPreferenceClickListener {
                NotificationHelper.notify(
                    requireContext(),
                    "T50ZDvgrbcyD8keT",
                    "测试监听",
                    "测试监听",
                    PendingIntent.getActivity(
                        requireContext(),
                        0,
                        Intent(Settings.ACTION_APPLICATION_SETTINGS),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    ),
                    NotificationHelper.CHANNEL_DEFAULT
                )
                true
            }
        }
        findPreference<Preference>("应用详情")?.run {
            setOnPreferenceClickListener {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:${requireContext().packageName}")))
                true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        findPreference<TwoStatePreference>("通知监听")?.run {
            isChecked = requireContext().checkPermission4NotificationListener()
        }
        findPreference<TwoStatePreference>("通知")?.run {
            isChecked = requireContext().checkPermission4Notification()
        }
        findPreference<TwoStatePreference>("辅助功能")?.run {
            isChecked = requireContext().checkPermission4Accessibility<RPAccessibilityService>()
        }
    }
}