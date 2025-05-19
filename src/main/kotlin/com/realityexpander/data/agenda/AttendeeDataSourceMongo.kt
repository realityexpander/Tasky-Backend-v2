package com.realityexpander.data.agenda

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.set
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.realityexpander.data.models.Attendee
import com.realityexpander.data.models.User
import com.realityexpander.domain.agenda.AttendeeDataSource
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.supervisorScope

class AttendeeDataSourceMongo(
    db: MongoDatabase
) : AttendeeDataSource {

    private val attendees = db.getCollection<Attendee>("attendee", Attendee::class.java)
    private val users = db.getCollection("user", User::class.java)

    override suspend fun getAttendeeByEmailForEvent(email: String, eventId: String): Attendee? {
        return attendees.find(
            and(
                eq("email", email),
                eq("eventId", eventId)
            )
        ).firstOrNull()
    }

    override suspend fun getAttendeesForEvent(eventId: String): List<Attendee> {
        return attendees.find(eq("eventId", eventId)).toList()
    }

    override suspend fun createAttendeesForEvent(
        attendeeIds: List<String>,
        eventId: String,
        remindAtForOwner: Long,
        remindAtForOthers: Long,
        eventOwnerId: String
    ): Boolean {
        return supervisorScope {
            users.find(`in`("_id", attendeeIds)).toList()
                .map { user ->
                    Attendee(
                        email = user.email,
                        fullName = user.fullName,
                        userId = user._id.toString(),
                        eventId = eventId,
                        isGoing = true,
                        remindAt = if(user._id.toString() == eventOwnerId) remindAtForOwner else remindAtForOthers
                    )
                }
                .let {
                    if(it.isNotEmpty()) {
                        attendees.insertMany(it).wasAcknowledged()
                    } else true
                }
        }
    }

    override suspend fun deleteAttendeesForEvent(attendeeIds: List<String>, eventId: String): Boolean {
        return attendees.deleteMany(
            and(
                eq("eventId", eventId),
                `in`("userId", attendeeIds)
            )
        ).wasAcknowledged()
    }

    override suspend fun updateAttendeeInfoForEvent(
        eventId: String,
        attendeeId: String,
        isGoing: Boolean,
        remindAt: Long
    ): Boolean {
        return attendees.updateOne(
            and(
                eq("eventId",eventId),
                eq("userId", attendeeId)
            ),
            listOf(
                set("isGoing", isGoing),
                set("remindAt", remindAt),
            )
        ).wasAcknowledged()
    }
}