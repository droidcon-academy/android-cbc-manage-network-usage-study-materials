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
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class MainScreenViewModel constructor(private val jokeRepository:JokeRepository= JokeRepositoryImpl()):ViewModel() {
    private val localMainScreenState = MutableStateFlow<MainScreenState>(Loading)
    private val localRefreshDisplay = MutableStateFlow<Boolean>(true)

    val refreshDisplay:StateFlow<Boolean>
        get() = localRefreshDisplay
    private val localCurrentNetworkPreferenceSetting=MutableStateFlow<NetworkPreference>(WiFi)
    val currentNetworkPreferenceSetting:StateFlow<NetworkPreference>
        get() = localCurrentNetworkPreferenceSetting
    val mainScreenState:StateFlow<MainScreenState>
        get() = localMainScreenState

    fun getJokeOfTheDay(){
        viewModelScope.launch {
            jokeRepository.getJoke()
                .onStart { localMainScreenState.value = Loading }
                .collect{result->
               result.fold(onSuccess = {joke->
                   localMainScreenState.value=if (joke==null) Error("there was no joke found")
                   else MainScreenData(joke=joke, lastUpdated = getLastUpdatedTimeInString())
               },onFailure = {
                   localMainScreenState.value=Error("Unable to fetch the current joke because of the following error ${it.localizedMessage}")
               })
            }
        }
    }

    fun setCurrentUserNetworkPreference(preference:NetworkPreference){
        localCurrentNetworkPreferenceSetting.value = preference
    }

    fun doRefreshDisplay(value:Boolean){
        localRefreshDisplay.value = value
    }
    private fun getLastUpdatedTimeInString() =if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }else {
            SimpleDateFormat.getDateTimeInstance().format(Date())
        }

}