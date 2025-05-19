package com.realityexpander.routing.responses

import kotlinx.serialization.Serializable

@Serializable
data class AttendeeDTOResponse(
    val userId: String,
    val email: String,
    val fullName: String,
)
