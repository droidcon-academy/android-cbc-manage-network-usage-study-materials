package com.droidcon.managenetworkusage.api

import kotlinx.coroutines.flow.Flow

interface JokeRepository {
    suspend fun getJoke():Flow<Result<Joke?>>
}