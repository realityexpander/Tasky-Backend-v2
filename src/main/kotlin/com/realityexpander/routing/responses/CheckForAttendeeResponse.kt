package com.realityexpander.routing.responses

import kotlinx.serialization.Serializable

@Serializable
data class CheckForAttendeeResponse(
    val doesUserExist: Boolean,
    val attendee: AttendeeDTOResponse?
)