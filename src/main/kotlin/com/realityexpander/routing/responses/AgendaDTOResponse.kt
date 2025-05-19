package com.realityexpander.routing.responses

import kotlinx.serialization.Serializable

@Serializable
data class AgendaDTOResponse(
    val events: List<EventDTOResponse>,
    val tasks: List<TaskDTOResponse>,
    val reminders: List<ReminderDTOResponse>
)
