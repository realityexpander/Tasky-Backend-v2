package com.realityexpander.routing.responses

import com.realityexpander.data.models.Attendee
import kotlinx.serialization.Serializable

@Serializable
data class EventDTOResponse(
    val id: String,
    val title: String,
    val description: String?,
    val from: Long,
    val to: Long,
    val host: String,
    val remindAt: Long,
    val photos: List<PhotoDTO>,
    val attendees: List<Attendee>,
    val isUserEventCreator: Boolean
)
