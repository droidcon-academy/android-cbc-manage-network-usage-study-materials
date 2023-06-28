package com.droidcon.managenetworkusage.api

import com.droidcon.managenetworkusage.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import java.io.IOException
import kotlin.coroutines.resume

class JokeRepositoryImpl : JokeRepository{

    private val jsonSerializationInstance = Json

    private val client:OkHttpClient
        get() = OkHttpClient()

    private fun String.getNetworkRequestFromUrl(): Request = Request.Builder().url(this).build()

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun getJoke(): Flow<Result<Joke?>> = flow{

        val request=BuildConfig.Joke_Api_Url.getNetworkRequestFromUrl()

        val jokeResponse=suspendCancellableCoroutine<Result<Joke?>> {

            client.newCall(request).enqueue(object: Callback {

                override fun onFailure(call: Call, e: IOException) {
                    it.resume(Result.failure(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body
                    if (responseBody == null) {
                        it.resume(Result.success(null))
                    } else {
                        val joke = jsonSerializationInstance.decodeFromStream<Joke>(responseBody.byteStream())
                        it.resume(Result.success(joke))
                    }
                    it.invokeOnCancellation { response.closeQuietly() }
                }
            })
        }
        emit(jokeResponse)
    }
}