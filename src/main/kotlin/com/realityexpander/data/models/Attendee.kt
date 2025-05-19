package com.realityexpander.data.models

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Attendee(
    val email: String,
    val fullName: String,
    val userId: String,
    val eventId: String,
    val isGoing: Boolean,
    val remindAt: Long,
    val createdAt: String = Instant.now().toString(),
)
