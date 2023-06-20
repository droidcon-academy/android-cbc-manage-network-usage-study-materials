package com.droidcon.managenetworkusage

import android.net.ConnectivityManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class NetworkListener constructor(private val connectivityManager: ConnectivityManager):DefaultLifecycleObserver{

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        // to be implemented
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        // to be implemented
    }
}