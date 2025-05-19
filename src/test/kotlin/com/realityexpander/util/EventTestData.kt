package com.realityexpander.util

import com.realityexpander.routing.requests.CreateEventRequest
import com.realityexpander.routing.requests.UpdateEventRequest
import java.util.UUID

fun createEventRequest(): CreateEventRequest {
    return CreateEventRequest(
        id = UUID.randomUUID().toString(),
        title = "Test title",
        description = "Test description",
        from = 1L,
        to = 3L,
        remindAt = 5L,
        attendeeIds = listOf(
            "user1",
            "user2",
            "user3",
        )
    )
}

fun updateEventRequest(): UpdateEventRequest {
    return UpdateEventRequest(
        id = UUID.randomUUID().toString(),
        title = "Test title updated",
        description = "Test description updated",
        from = 3L,
        to = 5L,
        remindAt = 2L,
        attendeeIds = listOf(
            "user1",
            "user5",
        ),
        deletedPhotoKeys = emptyList(),
        isGoing = true
    )
}