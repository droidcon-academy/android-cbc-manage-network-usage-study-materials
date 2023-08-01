# Code snippets for each change

## Manifest

```
 <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
// inside the settings activity node in the manifest file

 <activity android:name=".SettingsActivity"
            android:exported="false"
            android:theme="@style/Theme.ManageNetworkUsageCBC.SettingsTheme"
            android:label="@string/title_activity_settings">
            <intent-filter>
                <action android:name="android.intent.action.MANAGE_NETWORK_USAGE"/>
                <category android:name="android.intent.category.DEFAULT"/>
            </intent-filter>
        </activity>
```

## NetworkListener.kt

```kotlin
// networkRequest
private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
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

     private val localCurrentDeviceNetwork = MutableStateFlow<NetworkConnectionType>(NoConnection)

    private val localCurrentUserNetworkPreference =
        MutableStateFlow<NetworkPreference>(NoNetworkPreference)

    fun setCurrentUserNetworkPreference(newNetworkPreference: NetworkPreference) {
        localCurrentUserNetworkPreference.value = newNetworkPreference
    }

    fun setCurrentDeviceNetwork(newDeviceNetwork: NetworkConnectionType) {
        localCurrentDeviceNetwork.value = newDeviceNetwork
    }

   fun getJokeOfTheDay() {
        viewModelScope.launch {
            if (localCurrentUserNetworkPreference.value is AnyNetwork
                && (localCurrentDeviceNetwork.value is WiFiConnection
                        || localCurrentDeviceNetwork.value is CellularConnection)
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
## SettingsFragment.kt

```kotlin
  private val mainScreenViewModel: MainScreenViewModel by viewModels({ requireActivity() })

// inside OnSharedPreferenceChangeListener{} method
 private val onSharedPreferenceChangeListener =
            OnSharedPreferenceChangeListener { sharedPreferences, key ->
                val setUserNetworkPreference = sharedPreferences
                    .getString(key, wifiNetwork)
                if (setUserNetworkPreference != null) {
                    val setNetworkPreference = if (setUserNetworkPreference.contains(wifiNetwork)) {
                        WiFiNetwork
                    } else if (setUserNetworkPreference.contains(anyNetwork)) {
                        AnyNetwork
                    } else NoNetworkPreference
                    mainScreenViewModel.setCurrentUserNetworkPreference(setNetworkPreference)
                }

            }

        override fun onPause() {
            super.onPause()
            preferenceScreen.sharedPreferences
                ?.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        }

        override fun onResume() {
            super.onResume()
            preferenceScreen.sharedPreferences
                ?.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        }

```
## MainActivity.kt

```kotlin
// inside the activity's onCreate method

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                networkListener.currentDeviceNetwork.collectLatest {currentNetwork->
                    mainScreenViewModel.setCurrentDeviceNetwork(currentNetwork)
                }
            }
        }

// inside the activity's onStart method
    override fun onStart() {
        super.onStart()
        val defaultSharedPreference = PreferenceManager.getDefaultSharedPreferences(this)
        val currentUserNetwork=defaultSharedPreference.getString(syncFeedKey, wifiNetwork)
        currentUserNetwork?.let {setNetwork->
            val networkPreference=if (setNetwork.contains(anyNetwork)) AnyNetwork
            else if (setNetwork.contains(wifiNetwork)) WiFiNetwork
            else NoNetworkPreference
            mainScreenViewModel.setCurrentUserNetworkPreference(networkPreference)
        }
    }

```
