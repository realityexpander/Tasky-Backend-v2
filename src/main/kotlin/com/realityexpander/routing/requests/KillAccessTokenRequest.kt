package com.realityexpander.routing.requests

import kotlinx.serialization.Serializable

@Serializable
data class KillAccessTokenRequest(
    val accessToken: String,
)
