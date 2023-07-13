package com.droidcon.managenetworkusage

import android.net.ConnectivityManager
import androidx.lifecycle.DefaultLifecycleObserver

class NetworkListener constructor(private val connectivityManager: ConnectivityManager) :
    DefaultLifecycleObserver {

}