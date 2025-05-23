package com.realityexpander.data.agenda

import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.realityexpander.data.models.Event
import com.realityexpander.domain.agenda.EventDataSource
import kotlinx.coroutines.flow.firstOrNull

class EventDataSourceMongo(
    db: MongoDatabase
): EventDataSource {

    private val events =
        db.getCollection("event", Event::class.java)

    override suspend fun insertEvent(event: Event): Boolean {
        return events.insertOne(event)
            .wasAcknowledged()
    }

    override suspend fun deleteEvent(eventId: String): Boolean {
        return events.deleteOne(
            eq("_id", eventId)
        ).wasAcknowledged()
    }

    override suspend fun getEventById(eventId: String): Event? {
        return events.find(
            eq("_id", eventId)
        ).firstOrNull()
    }

    override suspend fun updateEventById(eventId: String, event: Event): Boolean {
        return events.replaceOne(
            eq("_id", eventId), event
        ).wasAcknowledged()
    }

    override suspend fun isHost(userId: String, eventId: String): Boolean {
        return events.find(
            eq("_id", eventId)
        ).firstOrNull()?.host == userId
    }
}