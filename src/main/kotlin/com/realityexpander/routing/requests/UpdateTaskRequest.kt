package com.realityexpander.routing.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTaskRequest(
    val id: String,
    val title: String,
    val time: Long,
    val description: String?,
    val remindAt: Long,
    val isDone: Boolean,
)
