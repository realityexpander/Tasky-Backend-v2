package com.realityexpander.domain.agenda

import com.realityexpander.data.models.Event

interface EventDataSource {
    suspend fun insertEvent(event: Event): Boolean
    suspend fun deleteEvent(eventId: String): Boolean
    suspend fun getEventById(eventId: String): Event?
    suspend fun updateEventById(eventId: String, event: Event): Boolean
    suspend fun isHost(userId: String, eventId: String): Boolean
}