package com.realityexpander.routing.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpirationTimestamp: Long,
    val fullName: String,
)
