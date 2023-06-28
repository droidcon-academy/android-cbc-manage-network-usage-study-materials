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

class MainScreenViewModel constructor(private val jokeRepository:JokeRepository= JokeRepositoryImpl()):ViewModel() {

    private val localMainScreenState = MutableStateFlow<MainScreenState>(Loading)
    private val localCurrentConnectedNetwork = MutableStateFlow<NetworkConnectionType>(NoConnection)


    private val localCurrentNetworkPreferenceSetting=MutableStateFlow<NetworkPreference>(NoNetworkPreference)
    private val currentNetworkPreferenceSetting:NetworkPreference
        get() = localCurrentNetworkPreferenceSetting.value

    private val currentNetworkConnectionType:NetworkConnectionType
        get() = localCurrentConnectedNetwork.value

    val mainScreenState:StateFlow<MainScreenState>
        get() = localMainScreenState

    fun getJokeOfTheDay() {
        viewModelScope.launch {
            fetchJoke()
        }
    }

    fun refresh(){
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

    fun setCurrentUserNetworkPreference(preference:NetworkPreference) {
        localCurrentNetworkPreferenceSetting.value = preference
    }
    fun setCurrentNetworkConnectionType(connectionType:NetworkConnectionType){
        localCurrentConnectedNetwork.value = connectionType
    }


    private fun getLastUpdatedTimeInString() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } else {
            SimpleDateFormat.getDateTimeInstance().format(Date())
        }
}