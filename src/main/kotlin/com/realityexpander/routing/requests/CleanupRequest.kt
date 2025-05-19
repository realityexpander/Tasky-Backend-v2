package com.realityexpander.routing.requests

import kotlinx.serialization.Serializable

@Serializable
data class CleanupRequest(
    val cleanUpBefore: String  // ISO-8601 date string (yyyy-MM-dd'T'HH:mm:ss.SSSZ)
)
