package com.realityexpander.routing.requests

data class AddAttendeeRequest(
    val eventId: String,
    val attendeeEmail: String
)
