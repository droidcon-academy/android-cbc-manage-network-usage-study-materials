# Code snippets for each change

## Manifest

```
 <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
// inside the settings activity node in the manifest file
<activity
            android:name=".SettingsActivity"
            android:exported="false"
            android:theme="@style/Theme.ManageNetworkUsageCBC.SettingsTheme"
            android:label="@string/title_activity_settings">
            <intent-filter>
                <action android:name="android.intent.action.MANAGE_NETWORK_USAGE" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>

```

## NetworkListener.kt

```kotlin
// networkRequest
private val networkRequest = NetworkRequest.Builder()
    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
    .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
    .build()

private val localCurrentConnectedNetwork = MutableStateFlow<NetworkConnectionType>(NoConnection)
val currentConnectedNetwork: StateFlow<NetworkConnectionType>
    get() = localCurrentConnectedNetwork

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
            } else NoConnection
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        localCurrentConnectedNetwork.value = NoConnection
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

  private val localCurrentConnectedNetwork = MutableStateFlow<NetworkConnectionType>(NoConnection)

  private val localCurrentNetworkPreferenceSetting =
        MutableStateFlow<NetworkPreference>(NoNetworkPreference)


  fun setCurrentNetworkConnectionType(connectionType: NetworkConnectionType) {
        localCurrentConnectedNetwork.value = connectionType
    }


   fun setCurrentUserNetworkPreference(preference: NetworkPreference) {
        localCurrentNetworkPreferenceSetting.value = preference
 }

 fun getJokeOfTheDay(){
     viewModelScope.launch {
         if (localCurrentConnectedNetwork.value is WiFiConnection
             && localCurrentNetworkPreferenceSetting.value is WiFiNetwork) {
             fetchJoke()
         } else if ((localCurrentConnectedNetwork.value is WiFiConnection
                     || localCurrentConnectedNetwork.value is CellularConnection)
             && localCurrentNetworkPreferenceSetting.value is AnyNetwork) {
             fetchJoke()
         } else {
             localMainScreenState.value =
                 Error("Cannot sync your home feed because current's device network connection does not fulfill the app's network requirements.")
         }
     }
    }

```
## SettingsActivity.kt

```kotlin
// on resume method of the SettingsFragment
 override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(
                onSharedPreferenceChangeListener
            )
        }

// on pause method of the SettingsFragment
override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(
                onSharedPreferenceChangeListener
            )
  }


// inside OnSharedPreferenceChangeListener{} method
  val setUserNetworkPreference =sharedPreferences.getString(key, wifiNetwork)
                if (setUserNetworkPreference != null) {
                    val setNetworkPreference = if (setUserNetworkPreference.contains(wifiNetwork)) {
                        WiFiNetwork
                    }else if (setUserNetworkPreference.contains(anyNetwork)){
                        AnyNetwork
                    }else NoNetworkPreference
                    mainScreenViewModel.setCurrentUserNetworkPreference(setNetworkPreference)
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
    override fun onStart() {
        super.onStart()
        val defaultApplicationSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val currentUserNetwork = defaultApplicationSharedPrefs.getString(syncFeedKey, wifiNetwork)

        currentUserNetwork?.let { setNetwork ->
            val networkPreference = if (setNetwork.contentEquals(wifiNetwork)) WiFiNetwork
            else if (setNetwork.contentEquals(anyNetwork)) AnyNetwork
            else NoNetworkPreference
            mainScreenViewModel.setCurrentUserNetworkPreference(networkPreference)
        }
    }

```
