package com.realityexpander.routing.responses

import com.realityexpander.data.models.Attendee
import com.realityexpander.data.serializer.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class EventResponse(
    val id: String,
    val title: String,
    val description: String?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val from: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val to: LocalDateTime,
    val host: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val remindAt: LocalDateTime,
    val photoUrls: List<String>,
    val attendees: List<Attendee>,
)
