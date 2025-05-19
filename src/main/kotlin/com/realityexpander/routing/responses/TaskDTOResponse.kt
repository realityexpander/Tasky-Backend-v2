package com.realityexpander.routing.responses

import kotlinx.serialization.Serializable

@Serializable
data class TaskDTOResponse(
    val id: String,
    val title: String,
    val description: String?,
    val time: Long,
    val remindAt: Long,
    val isDone: Boolean
)
