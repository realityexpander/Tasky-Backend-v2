package com.realityexpander.routing.mappers

import com.amazonaws.services.s3.AmazonS3
import com.realityexpander.data.models.Agenda
import com.realityexpander.domain.agenda.AttendeeDataSource
import com.realityexpander.routing.responses.AgendaDTOResponse
import com.realityexpander.routing.responses.EventDTOResponse
import com.realityexpander.routing.responses.ReminderDTOResponse
import com.realityexpander.routing.responses.TaskDTOResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

suspend fun Agenda.toAgendaDto(
	callerUserId: String,
	attendeeDataSource: AttendeeDataSource,
	s3: AmazonS3,
	ioDispatcher: CoroutineDispatcher
): AgendaDTOResponse {
	return withContext(ioDispatcher) {
		AgendaDTOResponse(
			events = this@toAgendaDto.events.map { event ->
				val attendees =
					async {
						attendeeDataSource.getAttendeesForEvent(
							event._id)
						}

						event to attendees
					}
					.map { eventAttendeesPair ->
						val event = eventAttendeesPair.first
						val attendees = eventAttendeesPair.second

						EventDTOResponse(
							title = event.title,
							description = event.description,
							from = event.from,
							to = event.to,
							host = event.host,
							remindAt = attendees.await().find {
									it.userId == callerUserId
								}?.remindAt ?: 0,
							id = event._id.toString(),
							photos = event.getPhotos(s3, ioDispatcher),
							attendees = attendees.await(),
							isUserEventCreator = callerUserId == event.host
						)
				},
			tasks = this@toAgendaDto.tasks.map {
				TaskDTOResponse(
					title = it.title,
					description = it.description,
					time = it.time,
					remindAt = it.remindAt,
					id = it._id.toString(),
					isDone = it.isDone
				)
			},
			reminders = this@toAgendaDto.reminders.map {
				ReminderDTOResponse(
					title = it.title,
					description = it.description,
					time = it.time,
					remindAt = it.remindAt,
					id = it._id.toString()
				)
			}
		)
	}
}