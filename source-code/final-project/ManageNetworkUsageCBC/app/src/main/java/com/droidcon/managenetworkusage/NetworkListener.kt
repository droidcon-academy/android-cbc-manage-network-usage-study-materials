package com.droidcon.managenetworkusage

import android.net.ConnectivityManager
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

/**
 * A network observer that notifies the application whenever the user's device's network connection changes
 *
 */
class NetworkListener constructor(private val connectivityManager: ConnectivityManager):DefaultLifecycleObserver{
    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()
    private val localCurrentConnectedNetwork= MutableStateFlow<NetworkConnectionType>(NoConnection)
    val currentConnectedNetwork:StateFlow<NetworkConnectionType>
        get() = localCurrentConnectedNetwork

    private val networkCallback = object :ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            localCurrentConnectedNetwork.value=if(connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)==true){
                CellularConnection
            }
            else if (connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)==true){
                WiFiConnection
            }
            else NoConnection
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            localCurrentConnectedNetwork.value=NoConnection
        }
    }


    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        connectivityManager.registerNetworkCallback(networkRequest,networkCallback)
    }
    // unregister the network callback to prevent unnecessary listening of network changes
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}