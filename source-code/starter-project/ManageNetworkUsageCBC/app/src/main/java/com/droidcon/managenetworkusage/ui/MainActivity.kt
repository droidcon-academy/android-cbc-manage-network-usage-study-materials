package com.droidcon.managenetworkusage.ui

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.droidcon.managenetworkusage.NetworkListener
import com.droidcon.managenetworkusage.ui.mainscreen.MainScreen
import com.droidcon.managenetworkusage.ui.mainscreen.MainScreenViewModel
import com.droidcon.managenetworkusage.ui.theme.ManageNetworkUsageCBCTheme

class MainActivity : ComponentActivity() {
    private lateinit var networkListener:NetworkListener
    private val mainScreenViewModel:MainScreenViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkListener=NetworkListener(connectivityManager)
        lifecycle.addObserver(networkListener)
        setContent {
            ManageNetworkUsageCBCTheme {
                MainScreen(viewModel = mainScreenViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(networkListener)
    }
}

