package com.realityexpander.routing

import com.amazonaws.services.s3.AmazonS3
import com.realityexpander.data.models.Event
import com.realityexpander.domain.agenda.AttendeeDataSource
import com.realityexpander.domain.agenda.EventDataSource
import com.realityexpander.domain.user.UserDataSource
import com.realityexpander.domain.util.DispatcherProvider
import com.realityexpander.domain.util.ErrorMessage
import com.realityexpander.inject
import com.realityexpander.routing.principal.userId
import com.realityexpander.routing.requests.CreateEventRequest
import com.realityexpander.routing.requests.UpdateEventRequest
import com.realityexpander.routing.responses.AttendeeDTOResponse
import com.realityexpander.routing.responses.CheckForAttendeeResponse
import com.realityexpander.routing.responses.EventDTOResponse
import com.realityexpander.routing.util.MAX_FILE_SIZE
import com.realityexpander.routing.util.PayloadTooLargeException
import com.realityexpander.routing.util.save
import com.realityexpander.sdk.AWS_BUCKET_NAME
import com.realityexpander.sdk.deleteBucketObjects
import com.realityexpander.sdk.putS3Object
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.UnsupportedMediaTypeException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.util.*

fun Route.event() {
	val eventDataSource: EventDataSource by inject()
	val userDataSource: UserDataSource by inject()
	val attendeeDataSource: AttendeeDataSource by inject()
	val s3: AmazonS3 by inject()
	val dispatchers by inject<DispatcherProvider>()

	authenticate("jwt") {
		// Create event with photos
		post("event") {
			val multipart = try {
				call.receiveMultipart()
			} catch (e: IllegalStateException) {
				call.respond(HttpStatusCode.BadRequest)
				return@post
			} catch(e: UnsupportedMediaTypeException) {
				call.respond(HttpStatusCode.UnsupportedMediaType)
				return@post
			} catch (e: PayloadTooLargeException) {
				call.respond(HttpStatusCode.PayloadTooLarge)
				return@post
			} catch (e: Exception) {
				call.respond(HttpStatusCode.InternalServerError)
				return@post
			}

			var createEventRequest: CreateEventRequest? = null
			val streams = hashMapOf<String, InputStream>()
			val savedFileJobs = mutableListOf<Job>()

			var isInvalidPayload = false

			multipart.forEachPart { part ->
				if (isInvalidPayload) {
					return@forEachPart
				}
				when (part) {
					// Event data
					is PartData.FormItem -> {
						if (part.name == "create_event_request") {
							try {
								createEventRequest = Json.decodeFromString(part.value)
							} catch (e: SerializationException) {
								call.respond(
									HttpStatusCode.BadRequest,
									ErrorMessage(
										message = "The create_event_request body has invalid/unknown fields. Please " +
												"compare yours with the docs."
									)
								)
								isInvalidPayload = true
								return@forEachPart
							}
						}
					}

					// File data (Photos)
					is PartData.FileItem -> {
						part.name?.let { partData ->
							if (Regex("photo[0-9]*").matches(partData)) {
								savedFileJobs += coroutineScope {
									launch(dispatchers.io) {
										try {
											streams[UUID.randomUUID().toString()] = part.save(dispatchers.io)

										} catch (e: PayloadTooLargeException) {
											call.respond(
												HttpStatusCode.PayloadTooLarge,
												ErrorMessage(
													message = "The maximum allowed file size is $MAX_FILE_SIZE"
												)
											)
											isInvalidPayload = true
										} catch (e: Exception) {
											call.respond(
												HttpStatusCode.InternalServerError,
												ErrorMessage(
													message = "An error occurred while processing the file."
												)
											)
											isInvalidPayload = true
										}
										finally {
											if (isInvalidPayload) {
												streams.values.forEach { inputStream -> inputStream.close() }
												streams.clear()
											}
										}
									}
								}
							}
						}
					}

					else -> Unit
				}
			}
			if (isInvalidPayload) {
				call.respond(HttpStatusCode.PayloadTooLarge)
				return@post
			}

			savedFileJobs.joinAll()

			val jobs = streams.map { pair ->
				coroutineScope {
					launch(dispatchers.io) {
						s3.putS3Object(
							AWS_BUCKET_NAME,
							pair.key,
							pair.value // file
						)
					}
				}
			}
			jobs.joinAll()

			if (createEventRequest == null) {
				call.respond(HttpStatusCode.BadRequest, ErrorMessage("No event data attached"))
				return@post
			}

			val event = Event(
				_id = createEventRequest!!.id,
				title = createEventRequest!!.title,
				description = createEventRequest!!.description,
				from = createEventRequest!!.from,
				to = createEventRequest!!.to,
				photoKeys = streams.keys.toList(),
				attendeeIds = createEventRequest!!.attendeeIds + call.userId,
				host = call.userId
			)
			val wasAcknowledged = eventDataSource.insertEvent(event)
			if (!wasAcknowledged) {
				call.respond(HttpStatusCode.Conflict)
				return@post
			}
			if (event.attendeeIds.isNotEmpty()) {
				val wasAddingAttendeesAcknowledged = attendeeDataSource.createAttendeesForEvent(
					attendeeIds = event.attendeeIds,
					eventId = event._id,
					remindAtForOwner = createEventRequest!!.remindAt,
					remindAtForOthers = event.from - 1000L * 60L * 60L,
					eventOwnerId = call.userId
				)
				if (!wasAddingAttendeesAcknowledged) {
					call.respond(HttpStatusCode.Conflict, ErrorMessage("An error happened when adding the attendees."))
					return@post
				}
			}

			val attendees = attendeeDataSource.getAttendeesForEvent(event._id)
			val eventDtoResponse = EventDTOResponse(
				title = event.title,
				description = event.description,
				from = event.from,
				to = event.to,
				host = event.host,
				photos = event.getPhotos(s3, dispatchers.io),
				attendees = attendees,
				id = event._id,
				isUserEventCreator = call.userId == event.host,
				remindAt = attendees.find { it.userId == call.userId }?.remindAt ?: 0
			)

			call.respond(HttpStatusCode.OK, eventDtoResponse)
		}

		// Get event by ID
		get("event") {
			val eventId = call.parameters["eventId"]
			if (eventId == null) {
				call.respond(
					HttpStatusCode.BadRequest,
					ErrorMessage(
						message = "No event ID provided"
					)
				)
				return@get
			}

			val eventDeferred =
				coroutineScope {
					async { eventDataSource.getEventById(eventId) }
				}
			val attendees = coroutineScope {
				async { attendeeDataSource.getAttendeesForEvent(eventId) }
			}
			val event = eventDeferred.await()
			if (event == null) {
				call.respond(
					HttpStatusCode.BadRequest,
					ErrorMessage(
						message = "Event not found"
					)
				)
				return@get
			}
			if (!event.containsUser(call.userId)) {
				call.respond(HttpStatusCode.Forbidden)
				return@get
			}

			val eventDtoResponse = EventDTOResponse(
				title = event.title,
				description = event.description,
				from = event.from,
				to = event.to,
				host = event.host,
				remindAt = attendees.await().find { it.userId == call.userId }?.remindAt ?: 0,
				photos = event.getPhotos(s3, dispatchers.io),
				attendees = attendees.await(),
				id = event._id,
				isUserEventCreator = call.userId == event.host
			)

			call.respond(HttpStatusCode.OK, eventDtoResponse)
		}

		// Delete event
		delete("event") {
			val eventId = call.parameters["eventId"]
			if (eventId == null) {
				call.respond(
					HttpStatusCode.BadRequest,
					ErrorMessage(
						message = "No event ID provided"
					)
				)
				return@delete
			}
			val event = eventDataSource.getEventById(eventId)
			if (event == null) {
				call.respond(HttpStatusCode.BadGateway, ErrorMessage("Event not found"))
				return@delete
			}
			if (event.host != call.userId) {
				call.respond(HttpStatusCode.Forbidden)
				return@delete
			}

			coroutineScope {
				launch(dispatchers.io) {
					s3.deleteBucketObjects(AWS_BUCKET_NAME, event.photoKeys, dispatchers.io)
				}
			}

			val wasAcknowledged = eventDataSource.deleteEvent(eventId)
			if (!wasAcknowledged) {
				call.respond(HttpStatusCode.Conflict)
				return@delete
			}

			call.respond(HttpStatusCode.OK)
		}

		// Update event
		put("event") {
			val multipart = try {
				call.receiveMultipart()
			} catch (e: IllegalStateException) {
				call.respond(HttpStatusCode.BadRequest)
				return@put
			}
			var request: UpdateEventRequest? = null
			val streams = hashMapOf<String, InputStream>()

			var isInvalidPayload = false

			multipart.forEachPart { part ->
				if (isInvalidPayload) {
					return@forEachPart
				}
				when (part) {
					is PartData.FormItem -> {
						if (part.name == "update_event_request") {
							try {
								request = Json.decodeFromString(part.value)
							} catch (e: SerializationException) {
								call.respond(
									HttpStatusCode.BadRequest,
									ErrorMessage(
										message = "The update_event_request body has invalid/unknown fields. Please " +
												"compare yours with the docs."
									)
								)
								isInvalidPayload = true
								return@forEachPart
							}
						}
					}

					is PartData.FileItem -> {
						part.name?.let {
							if (Regex("photo[0-9]*").matches(it)) {
								try {
//									streams[UUID.randomUUID().toString()] = part.save(dispatchers.io)
									streams[UUID.randomUUID().toString()] = part.save(dispatchers.io)
								} catch (e: PayloadTooLargeException) {
									call.respond(
										HttpStatusCode.PayloadTooLarge,
										ErrorMessage(
											message = "The maximum allowed file size is $MAX_FILE_SIZE"
										)
									)
									isInvalidPayload = true
								}
							}
						}
					}

					else -> Unit
				}
			}
			if (isInvalidPayload) {
				return@put
			}

			if (request == null) {
				call.respond(HttpStatusCode.BadRequest, ErrorMessage("No event data attached"))
				return@put
			}

			val eventFromDb = eventDataSource.getEventById(request!!.id)
			if (eventFromDb == null) {
				call.respond(
					HttpStatusCode.Conflict,
					ErrorMessage(message = "Event not found")
				)
				return@put
			}

			if (eventFromDb.containsUser(call.userId) && call.userId != eventFromDb.host) {
				// Attendee updated isGoing or reminder type
				val eventDto = updateEventAsAttendee(
					request = request!!,
					userId = call.userId,
					eventFromDb = eventFromDb,
					attendeeDataSource = attendeeDataSource,
					eventDataSource = eventDataSource,
					s3 = s3,
					ioDispatcher = dispatchers.io
				)
				call.respond(HttpStatusCode.OK, eventDto)
				return@put
			}

			if (!eventFromDb.containsUser(call.userId)) {
				call.respond(HttpStatusCode.Forbidden)
				return@put
			}
			val newPhotoUrlsSize = (eventFromDb.photoKeys - request!!.deletedPhotoKeys.toSet()).size + streams.size
			if (newPhotoUrlsSize > 10) {
				call.respond(
					HttpStatusCode.BadRequest,
					ErrorMessage("You can't attach more than 10 photos to an event")
				)
				return@put
			}
			val deletedPhotoKeys = if (eventFromDb.host == call.userId) request!!.deletedPhotoKeys else emptyList()

			val deleteFilesJob =
				coroutineScope {
					launch(dispatchers.io) {
						s3.deleteBucketObjects(AWS_BUCKET_NAME, deletedPhotoKeys, dispatchers.io)
					}
				}

			val newPhotoKeys = if (eventFromDb.host != call.userId) {
				eventFromDb.photoKeys
			} else {
				streams.map { it.key }
			}

			streams.map { pair ->
				coroutineScope {
					launch(dispatchers.io) {
						s3.putS3Object(AWS_BUCKET_NAME, pair.key, pair.value)
					}
				}
			}.forEach { it.join() }

			val event = eventFromDb.copy(
				title = request!!.title,
				description = request!!.description,
				from = request!!.from,
				to = request!!.to,
				attendeeIds = request!!.attendeeIds,
				_id = request!!.id,
				photoKeys = if (eventFromDb.host != call.userId) {
					emptyList()
				} else {
					eventFromDb.photoKeys - deletedPhotoKeys.toSet() + newPhotoKeys
				},
			)
			val oldAttendeeIds = eventFromDb.attendeeIds
			val deletedAttendeeIds = oldAttendeeIds - request!!.attendeeIds.toSet()
			coroutineScope {
				launch {
					if (event.host == call.userId) {
						attendeeDataSource.deleteAttendeesForEvent(deletedAttendeeIds, event._id)
					}
				}
			}
			val updateIsGoingJob = coroutineScope {
				launch {
					attendeeDataSource.updateAttendeeInfoForEvent(
						eventId = eventFromDb._id,
						attendeeId = call.userId,
						isGoing = request!!.isGoing,
						remindAt = request!!.remindAt
					)
				}
			}
			if (request!!.attendeeIds.isNotEmpty() && call.userId == event.host) {
				val newAttendeeIds = request!!.attendeeIds - oldAttendeeIds.toSet()
				val wasAddingAttendeesAcknowledged = attendeeDataSource.createAttendeesForEvent(
					attendeeIds = newAttendeeIds,
					eventId = event._id,
					remindAtForOwner = request!!.remindAt,
					remindAtForOthers = event.from - 1000L * 60L * 60L,
					eventOwnerId = event.host
				)
				if (!wasAddingAttendeesAcknowledged) {
					call.respond(HttpStatusCode.Conflict, ErrorMessage("An error happened when adding the attendees."))
					return@put
				}
			}
			val wasAcknowledged = eventDataSource.updateEventById(request!!.id, event)
			if (!wasAcknowledged) {
				call.respond(HttpStatusCode.Conflict)
				return@put
			}

			val attendees = attendeeDataSource.getAttendeesForEvent(event._id)
			val eventDtoResponse = EventDTOResponse(
				title = event.title,
				description = event.description,
				from = event.from,
				to = event.to,
				host = event.host,
				remindAt = attendees.find { it.userId == call.userId }?.remindAt ?: 0,
				photos = event.getPhotos(s3, dispatchers.io),
				attendees = attendees,
				id = event._id,
				isUserEventCreator = call.userId == event.host
			)

			deleteFilesJob.join()
			updateIsGoingJob.join()
			call.respond(HttpStatusCode.OK, eventDtoResponse)
		}

		// Check if user is an attendee of the event
		get("attendee") {
			val email = call.parameters["email"]
			val eventId = call.parameters["eventId"]
			if (email == null) {
				call.respond(HttpStatusCode.BadRequest, ErrorMessage("Missing email parameter"))
				return@get
			}

			val user = userDataSource.getUserByEmail(email.trim().lowercase())
			if (user == null) {
				call.respond(
					HttpStatusCode.OK,
					CheckForAttendeeResponse(doesUserExist = false, attendee = null)
				)
				return@get
			}
			if (user._id == call.userId) {
				call.respond(HttpStatusCode.Conflict, ErrorMessage("You can't add yourself as attendee"))
				return@get
			}
			if (eventId == null) {
				// Event hasn't been created yet
				call.respond(
					HttpStatusCode.OK,
					CheckForAttendeeResponse(
						doesUserExist = true,
						attendee = AttendeeDTOResponse(
							email = user.email.trim().lowercase(),
							fullName = user.fullName,
							userId = user._id,
						)
					)
				)
				return@get
			}

			val attendee = attendeeDataSource.getAttendeeByEmailForEvent(email, eventId)
			call.respond(
				HttpStatusCode.OK,
				CheckForAttendeeResponse(
					doesUserExist = true,
					attendee = AttendeeDTOResponse(
						email = email.trim().lowercase(),
						fullName = attendee?.fullName ?: user.fullName,
						userId = attendee?.userId ?: user._id,
					)
				)
			)
		}

		// Delete attendee from event
		delete("attendee") {
			val eventId = call.parameters["eventId"]
			if (eventId == null) {
				call.respond(HttpStatusCode.BadRequest, ErrorMessage("No eventId parameter provided"))
				return@delete
			}

			val event = eventDataSource.getEventById(eventId)
			if (event == null) {
				call.respond(HttpStatusCode.Conflict, ErrorMessage("Event not found"))
				return@delete
			}

			val wasUpdateAcknowledged = coroutineScope {
				async {
					eventDataSource.updateEventById(
						eventId,
						event.copy(
							attendeeIds = event.attendeeIds - call.userId
						)
					)
				}
			}
			val wasAcknowledged = coroutineScope {
				async { attendeeDataSource.deleteAttendeesForEvent(listOf(call.userId), eventId) }
			}

			if (!wasUpdateAcknowledged.await()) {
				call.respond(HttpStatusCode.Conflict, ErrorMessage("Couldn't update event"))
				return@delete
			}
			if (!wasAcknowledged.await()) {
				call.respond(HttpStatusCode.Conflict, ErrorMessage("Couldn't delete attendee"))
				return@delete
			}

			call.respond(HttpStatusCode.OK)
		}
	}
}

private suspend fun updateEventAsAttendee(
	request: UpdateEventRequest,
	userId: String,
	eventFromDb: Event,
	attendeeDataSource: AttendeeDataSource,
	eventDataSource: EventDataSource,
	s3: AmazonS3,
	ioDispatcher: CoroutineDispatcher
): EventDTOResponse {
	attendeeDataSource.updateAttendeeInfoForEvent(
		eventId = eventFromDb._id,
		attendeeId = userId,
		isGoing = request.isGoing,
		remindAt = request.remindAt
	)
	val event = eventFromDb.copy(
		title = request.title,
		description = request.description,
		from = request.from,
		to = request.to,
		attendeeIds = request.attendeeIds,
		_id = request.id,
		photoKeys = eventFromDb.photoKeys,
	)
	eventDataSource.updateEventById(
		eventId = eventFromDb._id,
		event = event
	)
	val updatedEvent = eventDataSource.getEventById(eventFromDb._id)
	val attendees = attendeeDataSource.getAttendeesForEvent(eventFromDb._id)

	return EventDTOResponse(
		title = updatedEvent!!.title,
		description = updatedEvent.description,
		from = updatedEvent.from,
		to = updatedEvent.to,
		host = updatedEvent.host,
		remindAt = attendees.find { it.userId == userId }?.remindAt ?: 0,
		photos = event.getPhotos(s3, ioDispatcher),
		attendees = attendees,
		id = updatedEvent._id,
		isUserEventCreator = userId == updatedEvent.host
	)
}
