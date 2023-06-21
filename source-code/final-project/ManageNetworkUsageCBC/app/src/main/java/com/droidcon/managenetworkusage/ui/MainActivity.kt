package com.droidcon.managenetworkusage.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import com.droidcon.managenetworkusage.NetworkListener
import com.droidcon.managenetworkusage.ui.mainscreen.AnyNetwork
import com.droidcon.managenetworkusage.ui.mainscreen.CellularConnection
import com.droidcon.managenetworkusage.ui.mainscreen.MainScreen
import com.droidcon.managenetworkusage.ui.mainscreen.MainScreenViewModel
import com.droidcon.managenetworkusage.ui.mainscreen.NetworkConnectionType
import com.droidcon.managenetworkusage.ui.mainscreen.NetworkPreference
import com.droidcon.managenetworkusage.ui.mainscreen.NoNetworkPreference
import com.droidcon.managenetworkusage.ui.mainscreen.NoConnection
import com.droidcon.managenetworkusage.ui.mainscreen.WiFiNetwork
import com.droidcon.managenetworkusage.ui.mainscreen.WiFiConnection
import com.droidcon.managenetworkusage.ui.theme.ManageNetworkUsageCBCTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var networkListener:NetworkListener
    private val mainScreenViewModel:MainScreenViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkListener=NetworkListener(connectivityManager)
        lifecycle.addObserver(networkListener)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED){
                networkListener.currentConnectedNetwork.collectLatest {connectionType->
                    // get and set the device's current network connection
                    mainScreenViewModel.setCurrentNetworkConnectionType(connectionType)
                }
            }
        }

        setContent {
            ManageNetworkUsageCBCTheme {
                MainScreen(viewModel = mainScreenViewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val defaultApplicationSharedPrefs= PreferenceManager.getDefaultSharedPreferences(this)
        val currentNetworkPreference=defaultApplicationSharedPrefs.getNetworkPreferenceFromSharedPreference()
        mainScreenViewModel.setCurrentUserNetworkPreference(currentNetworkPreference)

    }


    companion object{
        private const val syncFeedKey="syncFeed"
        private const val anyNetwork="Any network"
        private const val wifiNetwork="Wifi"
        fun SharedPreferences.getNetworkPreferenceFromSharedPreference():NetworkPreference{
            val userNetworkPreference=getString(syncFeedKey, wifiNetwork)
            return if (userNetworkPreference.contentEquals(wifiNetwork)) WiFiNetwork
            else if (userNetworkPreference.contentEquals(anyNetwork) ) AnyNetwork
            else NoNetworkPreference
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // everything initialized/added as an observer must always be removed so remove it here
        lifecycle.removeObserver(networkListener)
    }
}

