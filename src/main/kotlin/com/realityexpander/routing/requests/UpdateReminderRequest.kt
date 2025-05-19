package com.realityexpander.routing.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateReminderRequest(
    val id: String,
    val title: String,
    val time: Long,
    val description: String?,
    val remindAt: Long
)
