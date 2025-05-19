package com.realityexpander.routing.responses

@kotlinx.serialization.Serializable
data class AccessTokenResponse(
    val accessToken: String,
    val expirationTimestamp: Long
)
