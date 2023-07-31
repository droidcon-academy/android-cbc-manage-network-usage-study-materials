package com.droidcon.managenetworkusage

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceFragmentCompat
import com.droidcon.managenetworkusage.ui.mainscreen.AnyNetwork
import com.droidcon.managenetworkusage.ui.mainscreen.MainScreenViewModel
import com.droidcon.managenetworkusage.ui.mainscreen.NoNetworkPreference
import com.droidcon.managenetworkusage.ui.mainscreen.WiFiNetwork

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setHomeButtonEnabled(true)

    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private val mainScreenViewModel: MainScreenViewModel by viewModels({ requireActivity() })

        private val onSharedPreferenceChangeListener =
            OnSharedPreferenceChangeListener { sharedPreferences, key ->
                val setUserNetworkPreference =sharedPreferences.getString(key, wifiNetwork)
                if (setUserNetworkPreference != null) {
                    val setNetworkPreference = if (setUserNetworkPreference.contains(wifiNetwork)) {
                        WiFiNetwork
                    }else if (setUserNetworkPreference.contains(anyNetwork)){
                        AnyNetwork
                    }else NoNetworkPreference
                    mainScreenViewModel.setCurrentUserNetworkPreference(setNetworkPreference)
                }
            }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        companion object {
            private const val anyNetwork = "Any network"
            private const val wifiNetwork = "Wifi"
        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(
                onSharedPreferenceChangeListener
            )
        }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(
                onSharedPreferenceChangeListener
            )
        }


    }
}