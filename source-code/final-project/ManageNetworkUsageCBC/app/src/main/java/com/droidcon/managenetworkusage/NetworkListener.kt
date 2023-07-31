package com.droidcon.managenetworkusage

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.droidcon.managenetworkusage.ui.mainscreen.CellularConnection
import com.droidcon.managenetworkusage.ui.mainscreen.NetworkConnectionType
import com.droidcon.managenetworkusage.ui.mainscreen.NoConnection
import com.droidcon.managenetworkusage.ui.mainscreen.WiFiConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NetworkListener constructor(private val connectivityManager: ConnectivityManager) :
    DefaultLifecycleObserver {
    private val networkRequest = NetworkRequest
        .Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()
    private val localCurrentDeviceNetwork = MutableStateFlow<NetworkConnectionType>(NoConnection)
    val currentDeviceNetwork: StateFlow<NetworkConnectionType>
        get() = localCurrentDeviceNetwork
    private val networkCallback = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            localCurrentDeviceNetwork.value =
                if (connectivityManager.getNetworkCapabilities(network)
                        ?.hasTransport(
                            NetworkCapabilities
                                .TRANSPORT_CELLULAR
                        ) == true
                ) {
                    CellularConnection
                } else if (connectivityManager
                        .getNetworkCapabilities(network)
                        ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                ) {
                    WiFiConnection
                } else NoConnection
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            localCurrentDeviceNetwork.value = NoConnection
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}