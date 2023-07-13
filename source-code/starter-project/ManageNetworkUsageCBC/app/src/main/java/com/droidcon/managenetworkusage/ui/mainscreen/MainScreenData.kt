package com.droidcon.managenetworkusage.ui.mainscreen

import com.droidcon.managenetworkusage.api.Joke


sealed interface MainScreenState
object Loading : MainScreenState
data class MainScreenData(
    val joke: Joke?,
    val lastUpdated: String,
) : MainScreenState

@JvmInline
value class Error(val errorString: String) : MainScreenState
