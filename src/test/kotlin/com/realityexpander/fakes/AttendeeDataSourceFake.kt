package com.realityexpander.fakes

import com.realityexpander.data.models.Attendee
import com.realityexpander.domain.agenda.AttendeeDataSource

class AttendeeDataSourceFake(
    private val userDataSourceFake: UserDataSourceFake
): AttendeeDataSource {

    val attendees = mutableListOf<Attendee>()

    override suspend fun getAttendeeByEmailForEvent(email: String, eventId: String): Attendee? {
        return attendees.find { it.email == email && it.eventId == eventId }
    }

    override suspend fun getAttendeesForEvent(eventId: String): List<Attendee> {
        return attendees.filter { it.eventId == eventId }
    }

    override suspend fun createAttendeesForEvent(
        attendeeIds: List<String>,
        eventId: String,
        remindAtForOwner: Long,
        remindAtForOthers: Long,
        eventOwnerId: String
    ): Boolean {
        attendeeIds.forEach { id ->
            val user = userDataSourceFake.getUserById(id) ?: return@forEach
            val attendee = Attendee(
                email = user.email,
                fullName = user.fullName,
                userId = user._id,
                eventId = eventId,
                isGoing = true,
                remindAt = if(user._id == eventOwnerId) remindAtForOwner else remindAtForOthers
            )
            attendees.add(attendee)
        }
        return true
    }

    override suspend fun deleteAttendeesForEvent(attendeeIds: List<String>, eventId: String): Boolean {
        return attendees.removeIf {
            it.eventId == eventId && it.userId in attendeeIds
        }
    }

    override suspend fun updateAttendeeInfoForEvent(
        eventId: String,
        attendeeId: String,
        isGoing: Boolean,
        remindAt: Long
    ): Boolean {
        val existingAttendee = attendees.find { it.userId == attendeeId && it.eventId == eventId }
        val index = attendees.indexOf(existingAttendee)
        attendees[index] = attendees[index].copy(
            isGoing = isGoing,
            remindAt = remindAt
        )

        return true
    }
}