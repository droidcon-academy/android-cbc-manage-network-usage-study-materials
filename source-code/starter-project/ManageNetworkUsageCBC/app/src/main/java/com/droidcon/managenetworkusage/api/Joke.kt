package com.droidcon.managenetworkusage.api

import kotlinx.serialization.Serializable

@Serializable
data class Joke(
    val punchline: String,
    val id: Int,
    val type: String,
    val setup: String
)
