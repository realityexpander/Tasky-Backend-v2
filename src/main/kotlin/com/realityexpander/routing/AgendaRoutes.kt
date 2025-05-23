package com.realityexpander.routing

import com.amazonaws.services.s3.AmazonS3
import com.realityexpander.domain.agenda.*
import com.realityexpander.domain.util.DispatcherProvider
import com.realityexpander.domain.util.ErrorMessage
import com.realityexpander.inject
import com.realityexpander.routing.mappers.toAgendaDto
import com.realityexpander.routing.principal.userId
import com.realityexpander.routing.requests.SyncAgendaRequest
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun Route.agenda() {
	val eventDataSource: EventDataSource by inject()
	val taskDataSource: TaskDataSource by inject()
	val reminderDataSource: ReminderDataSource by inject()
	val agendaDataSource: AgendaDataSource by inject()
	val attendeeDataSource: AttendeeDataSource by inject()
	val s3: AmazonS3 by inject()
	val dispatchers by inject<DispatcherProvider>()

	// Gets the agenda for the given date
	authenticate("jwt") {
		get("agenda") {
			val time = call.parameters["time"]?.toLongOrNull()?.let {
				try {
					ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.of("UTC"))
				} catch (e: Exception) {
					call.respond(
						HttpStatusCode.BadRequest,
						ErrorMessage("The time in an invalid format")
					)
					return@get
				}
			} ?: kotlin.run {
				try {
					LocalDateTime.now().atZone(ZoneId.of("UTC"))
				} catch (e: Exception) {
					call.respond(
						HttpStatusCode.BadRequest,
						ErrorMessage("The time is in an invalid format")
					)
					return@get
				}
			}

			val agenda = agendaDataSource.getAgenda(call.userId, time.toLocalDate())
			val agendaDto = agenda.toAgendaDto(
				callerUserId = call.userId,
				attendeeDataSource = attendeeDataSource,
				s3 = s3,
				ioDispatcher = dispatchers.io
			)

			call.respond(HttpStatusCode.OK, agendaDto)
		}
	}

	// Syncs the agenda with the server by deleting the events, tasks, and reminders
	authenticate("jwt") {
		post("syncAgenda") {
			val request = call.receiveNullable<SyncAgendaRequest>() ?: kotlin.run {
				call.respond(HttpStatusCode.BadRequest,
					ErrorMessage("Missing or invalid body"))
				return@post
			}

			val eventJob = coroutineScope {
				launch {
					// Check if the user is the host of the event before deleting
					val userId = call.userId
					request.deletedEventIds.forEach { deletedEventId ->
						// Check if the event exists
						if(eventDataSource.getEventById(deletedEventId) == null) {
							println("Event with id $deletedEventId not found")
							return@forEach
						}
						// Check if the user is the host of the event
						if (eventDataSource.isHost(userId, deletedEventId)) {
							eventDataSource.deleteEvent(deletedEventId)
						} else {
							call.respond(HttpStatusCode.Forbidden,
								ErrorMessage("You are not the host of this event"))
							return@launch
						}
					}
				}
			}
			val taskJob = coroutineScope {
				launch {
					request.deletedTaskIds.forEach { id ->
						taskDataSource.deleteTask(id, call.userId)
					}
				}
			}
			val reminderJob = coroutineScope {
				launch {
					request.deletedReminderIds.forEach { id ->
						reminderDataSource.deleteReminder(id, call.userId) }
				}
			}

			eventJob.join()
			taskJob.join()
			reminderJob.join()

			call.respond(HttpStatusCode.OK)
		}
	}

	// Gets the full agenda for the user including other users' events that the user is attending
	authenticate("jwt") {
		get("fullAgenda") {
			val agenda = agendaDataSource.getFullAgenda(call.userId)

			call.respond(
				HttpStatusCode.OK,
				agenda.toAgendaDto(
					callerUserId = call.userId,
					attendeeDataSource = attendeeDataSource,
					s3 = s3,
					ioDispatcher = dispatchers.io
				)
			)
		}
	}
}