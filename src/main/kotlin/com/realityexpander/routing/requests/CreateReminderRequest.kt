package com.realityexpander.routing.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateReminderRequest(
    val id: String,
    val title: String,
    val description: String?,
    val time: Long,
    val remindAt: Long
)
