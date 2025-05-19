package com.realityexpander.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Agenda(
    val events: List<Event>,
    val tasks: List<Task>,
    val reminders: List<Reminder>
)
