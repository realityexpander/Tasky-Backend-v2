package com.realityexpander.routing.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateApiKeyRequest(
    val email: String,
    val validFrom: String
)