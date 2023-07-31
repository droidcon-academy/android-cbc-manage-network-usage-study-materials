# Solution for Coding challenge

The question was to enable the application to work with VPN connection, in addition to
working with wifi and cellullar connections.


## MainScreenViewModel.kt

```kotlin

    fun getJokeOfTheDay() {
        viewModelScope.launch {
            if (localCurrentUserNetworkPreference.value is AnyNetwork
                && (localCurrentDeviceNetwork.value is WiFiConnection
                        || localCurrentDeviceNetwork.value is CellularConnection
             || localCurrentDeviceNetwork.value is VPNConnection)
            ) {
                fetchJoke()
            } else if (localCurrentDeviceNetwork.value is WiFiConnection &&
                localCurrentUserNetworkPreference.value is WiFiNetwork
            ) {
                fetchJoke()
            } else {
                localMainScreenState.value = Error(
                    "The application cannot fetch the joke from the api because" +
                            "the network requirements are not fulfilled"
                )
            }

        }
    }
```

## MainScreenData.kt


```kotlin

object VPNConnection:NetworkConnectionType

```

## NetworkListener.kt

```kotlin
// network request
private val networkRequest = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
    .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
    .build()
// network callback

private val networkCallback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        localCurrentConnectedNetwork.value =
            if (connectivityManager.getNetworkCapabilities(network)
                    ?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
            ) {
                CellularConnection
            } else if (connectivityManager.getNetworkCapabilities(network)
                    ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            ) {
                WiFiConnection
            } else if (connectivityManager.getNetworkCapabilities(network)
                    ?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
            ) {
                VpnConnection
            } else NoConnection
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        localCurrentConnectedNetwork.value = NoConnection
    }
}

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
                }else if (connectivityManager
                        .getNetworkCapabilities(network)
                        ?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
                ){ 
                  VPNConnection
                 }else NoConnection
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            localCurrentDeviceNetwork.value = NoConnection
        }
    }

```