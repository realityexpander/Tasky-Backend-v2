package com.realityexpander.domain.agenda

import com.realityexpander.data.models.Attendee

interface AttendeeDataSource {
    suspend fun getAttendeeByEmailForEvent(email: String, eventId: String): Attendee?
    suspend fun getAttendeesForEvent(eventId: String): List<Attendee>
    suspend fun createAttendeesForEvent(
        attendeeIds: List<String>,
        eventId: String,
        remindAtForOwner: Long,
        remindAtForOthers: Long,
        eventOwnerId: String
    ): Boolean
    suspend fun deleteAttendeesForEvent(attendeeIds: List<String>, eventId: String): Boolean
    suspend fun updateAttendeeInfoForEvent(eventId: String, attendeeId: String, isGoing: Boolean, remindAt: Long): Boolean
}