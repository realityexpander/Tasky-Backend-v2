package com.realityexpander.routing.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    val id: String,
    val title: String,
    val description: String?,
    val remindAt: Long,
    val isDone: Boolean,
    val time: Long
)
