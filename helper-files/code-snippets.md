# Code snippets for each change

## Manifest

```
 <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

```

## NetworkListener.kt

```kotlin
// networkRequest
private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

// device's current connection type
private val localCurrentConnectedNetwork= MutableStateFlow<NetworkConnectionType>(NoConnection)

val currentConnectedNetwork:StateFlow<NetworkConnectionType> 
   get() = localCurrentConnectedNetwork

// network callback
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
  // lifecycle owner's onStart method 
  override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        connectivityManager.registerNetworkCallback(networkRequest,networkCallback)
    }
 // lifecycle owner's onStop method 
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

```

## MainScreenViewModel.kt

```kotlin
 fun getJokeOfTheDay(){
        viewModelScope.launch {
            if (currentNetworkConnectionType is WiFiConnection && currentNetworkPreferenceSetting is WiFiNetwork){
                fetchJoke()
            }else if ( (currentNetworkConnectionType is WiFiConnection || currentNetworkConnectionType is CellularConnection) && currentNetworkPreferenceSetting is AnyNetwork){
                fetchJoke()
            }else{
                localMainScreenState.value=Error("Cannot sync your home feed because current's device network connection does not fulfill the app's network requirements.")
            }
        }
    }

```
## SettingsActivity.kt

```kotlin
 if (key!=null && sharedPreferences!=null){
                    val setValue=sharedPreferences.getString(key, wifiNetwork)
                    val networkPreference=if (setValue!!.contains(wifiNetwork)){
                        WiFiNetwork
                    }else if (setValue.contains(anyNetwork)){
                        AnyNetwork
                    }else NoNetworkPreference
                    mainScreenViewModel.setCurrentUserNetworkPreference(networkPreference)
                }
```
## MainActivity.kt

```kotlin
// inside the activity's onCreate method
lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED){
                networkListener.currentConnectedNetwork.collectLatest {connectionType->
                    // get and set the device's current network connection
                    mainScreenViewModel.setCurrentNetworkConnectionType(connectionType)
                }
            }
        }

// inside the activity's onStart method
  val defaultApplicationSharedPrefs= PreferenceManager.getDefaultSharedPreferences(this)
        val currentNetworkPreference=defaultApplicationSharedPrefs.getNetworkPreferenceFromSharedPreference()
        mainScreenViewModel.setCurrentUserNetworkPreference(currentNetworkPreference)

```