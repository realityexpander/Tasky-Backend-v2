package com.realityexpander.fakes

import com.realityexpander.data.models.Event
import com.realityexpander.domain.agenda.EventDataSource

class EventDataSourceFake: EventDataSource {

    val events = mutableListOf<Event>()

    override suspend fun insertEvent(event: Event): Boolean {
        return events.add(event)
    }

    override suspend fun deleteEvent(eventId: String): Boolean {
        return events.removeIf { it._id == eventId }
    }

    override suspend fun getEventById(eventId: String): Event? {
        return events.find { it._id == eventId }
    }

    override suspend fun updateEventById(eventId: String, event: Event): Boolean {
        val existingEvent = events.find { it._id == eventId }
        val index = events.indexOf(existingEvent)
        events[index] = event

        return true
    }

    override suspend fun isHost(userId: String, eventId: String): Boolean {
        return events.find { it._id == eventId }?.host == userId
    }

}