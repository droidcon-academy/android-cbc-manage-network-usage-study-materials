package com.droidcon.managenetworkusage.ui.mainscreen

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidcon.managenetworkusage.api.JokeRepository
import com.droidcon.managenetworkusage.api.JokeRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.net.SocketException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class MainScreenViewModel constructor(private val jokeRepository: JokeRepository = JokeRepositoryImpl()) :
    ViewModel() {

    private val localMainScreenState = MutableStateFlow<MainScreenState>(Loading)

    val mainScreenState: StateFlow<MainScreenState>
        get() = localMainScreenState

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

    fun refresh() {
        getJokeOfTheDay()
    }

    private suspend fun fetchJoke() {
        jokeRepository.getJoke()
            .onStart { localMainScreenState.value = Loading }
            .collect { result ->
                result.fold(onSuccess = { joke ->
                    localMainScreenState.value = if (joke == null) Error("there was no joke found")
                    else MainScreenData(joke = joke, lastUpdated = getLastUpdatedTimeInString())
                }, onFailure = {
                    val errorMessage =
                        if (it is UnknownHostException || it is SocketException) "please check your internet connection and try again" else "an unknown error occurred please try again later"
                    localMainScreenState.value =
                        Error("Unable to fetch the current joke $errorMessage")
                })
            }
    }


    private fun getLastUpdatedTimeInString() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    } else {
        SimpleDateFormat.getDateTimeInstance().format(Date())
    }
}