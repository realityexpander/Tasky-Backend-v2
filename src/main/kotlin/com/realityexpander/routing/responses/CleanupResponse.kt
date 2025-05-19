package com.realityexpander.routing.responses

import kotlinx.serialization.Serializable

@Serializable
data class CleanupResponse(
    val deletedCount: Long
)