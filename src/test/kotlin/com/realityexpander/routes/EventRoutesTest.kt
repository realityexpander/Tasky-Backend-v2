package com.realityexpander.routes

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.amazonaws.services.s3.AmazonS3
import com.realityexpander.domain.agenda.AttendeeDataSource
import com.realityexpander.domain.agenda.EventDataSource
import com.realityexpander.routing.requests.CreateEventRequest
import com.realityexpander.routing.requests.UpdateEventRequest
import com.realityexpander.routing.responses.EventDTOResponse
import com.realityexpander.util.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.koin.test.inject
import java.util.*
import kotlin.test.Test

class EventRoutesTest : RootTest() {

	private val s3 by inject<AmazonS3>()
	private val events by inject<EventDataSource>()
	private val attendees by inject<AttendeeDataSource>()

	@Test
	fun `Create event without body responds error Bad Request`() = testApplication {
		setupTestApplication()

		val client = setupClient()
		val response = client.post("/event") {
			headers.append(HttpHeaders.ContentType, "multipart/form-data; boundary=----WebKitFormBoundary")
			createFullyAuthenticatedToken("user1")
		}

		assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
	}

	@Test
	fun `Create event without headers responds error Unsupported Media Type`() = testApplication {
		setupTestApplication()

		val client = setupClient()
		val response = client.post("/event") {
			createFullyAuthenticatedToken("user1")
		}

		assertThat(response.status).isEqualTo(HttpStatusCode.UnsupportedMediaType)
	}

	@Test
	fun `Create event with wrong body responds error Bad Request`() = testApplication {
		setupTestApplication()
		val json = Json.encodeToString(
			EventDTOResponse(
				title = "Test event",
				description = "Test description",
				from = System.currentTimeMillis(),
				to = System.currentTimeMillis(),
				host = "abc",
				remindAt = System.currentTimeMillis(),
				photos = emptyList(),
				attendees = emptyList(),
				id = UUID.randomUUID().toString(),
				isUserEventCreator = true
			)
		)

		val response = client.submitFormWithBinaryData(
			url = "/event",
			formData = formData {
				append("create_event_request", json, Headers.build {
					append(HttpHeaders.ContentDisposition, "form-data; name=\"create_event_request\"")
				})
			}
		) {
			createFullyAuthenticatedToken("user1")
		}

		assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
	}

	@Test
	fun `Update event without body responds error Bad Request`() = testApplication {
		setupTestApplication()

		val response = client.put("/event") {
			headers.append(HttpHeaders.ContentType, "multipart/form-data; boundary=----WebKitFormBoundary")
			createFullyAuthenticatedToken("user1")
		}

		assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
	}

	@Test
	fun `Create event with photos is success`() = testApplication {
		setupTestApplication()

		runTest {
			val id = UUID.randomUUID().toString()
			val response = createEvent(
				request = createEventRequest().copy(id = id),
				photoCount = 2
			)

			scheduler.advanceUntilIdle()

			assertThat(response.status).isEqualTo(HttpStatusCode.OK)

			val eventFromDb = events.getEventById(id)
			assertThat(eventFromDb).isNotNull()
			assertThat(eventFromDb?.host).isEqualTo("user1")

			val attendees = attendees.getAttendeesForEvent(id)
			assertThat(attendees.map { it.userId }).containsOnly(
				"user1",
				"user2",
				"user3",
			)

			coVerify(exactly = 2) {
				s3.putObject(any())
			}
		}
	}

	@Test
	fun `Create event without photos is success`() = testApplication {
		setupTestApplication()

		runTest {
			val id = UUID.randomUUID().toString()
			val response = createEvent(
				request = createEventRequest().copy(id = id),
				photoCount = 0
			)

			scheduler.advanceUntilIdle()

			assertThat(response.status).isEqualTo(HttpStatusCode.OK)

			val eventFromDb = events.getEventById(id)
			assertThat(eventFromDb).isNotNull()
			assertThat(eventFromDb?.host).isEqualTo("user1")

			val attendees = attendees.getAttendeesForEvent(id)
			assertThat(attendees.map { it.userId }).containsOnly(
				"user1",
				"user2",
				"user3",
			)

			coVerify(exactly = 0) {
				s3.putObject(any())
			}
		}
	}

	@Test
	fun `Create and update event with photos is success`() = testApplication {
		setupTestApplication()

		runTest {
			val id = UUID.randomUUID().toString()
			val initialPhotoCount = 5
			val createResponse = createEvent(
				request = createEventRequest().copy(id = id),
				photoCount = initialPhotoCount
			)
			val eventDtoResponse = createResponse.getBody<EventDTOResponse>()

			val deletedPhotoKeys = eventDtoResponse.photos.take(initialPhotoCount - 1).map { it.key }
			val updateRequest = updateEventRequest().copy(
				id = id,
				deletedPhotoKeys = eventDtoResponse.photos.take(initialPhotoCount - 1).map { it.key }
			)
			val newPhotoCount = 2
			val updateResponse = updateEvent(
				request = updateRequest,
				newPhotoCount = newPhotoCount
			)

			scheduler.advanceUntilIdle()

			assertThat(updateResponse.status).isEqualTo(HttpStatusCode.OK)

			val eventFromDb = events.getEventById(id)
			assertThat(eventFromDb?.host).isEqualTo("user1")
			assertThat(eventFromDb?.from).isEqualTo(updateRequest.from)
			assertThat(eventFromDb?.to).isEqualTo(updateRequest.to)
			assertThat(eventFromDb?.attendeeIds).isEqualTo(updateRequest.attendeeIds)
			assertThat(eventFromDb?.title).isEqualTo(updateRequest.title)
			assertThat(eventFromDb?.description).isEqualTo(updateRequest.description)

			deletedPhotoKeys.forEach {
				assertThat(eventFromDb?.photoKeys?.contains(it)).isEqualTo(false)
			}
			assertThat(eventFromDb?.photoKeys?.contains(eventDtoResponse.photos.last().key)).isEqualTo(true)

			assertThat(eventFromDb?.photoKeys?.size).isEqualTo(
				initialPhotoCount - deletedPhotoKeys.size + newPhotoCount
			)

			val attendees = attendees.getAttendeesForEvent(eventDtoResponse.id)
			assertThat(attendees.map { it.userId }).containsOnly(
				*updateRequest.attendeeIds.toTypedArray()
			)

			coVerify(exactly = 7) {
				s3.putObject(any())
			}
		}
	}

	@Test
	fun `Get event is success`() = testApplication {
		setupTestApplication()

		runTest {
			val event = testEvents[0]
			val response = client.get("/event") {
				parameter("eventId", event._id)
				createFullyAuthenticatedToken("user1")
			}

			val body = response.getBody<EventDTOResponse>()

			scheduler.advanceUntilIdle()

			assertThat(response.status).isEqualTo(HttpStatusCode.OK)

			assertThat(body.attendees.map { it.userId }).isEqualTo(event.attendeeIds)
			assertThat(body.title).isEqualTo(event.title)
			assertThat(body.description).isEqualTo(event.description)
			assertThat(body.from).isEqualTo(event.from)
			assertThat(body.to).isEqualTo(event.to)
			assertThat(body.photos.map { it.key }).isEqualTo(event.photoKeys)
		}
	}

	@Test
	fun `Get event with missing ID, responds error Bad Request`() = testApplication {
		setupTestApplication()

		val response = client.get("/event") {
			createFullyAuthenticatedToken("user1")
		}

		assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
	}

	@Test
	fun `Delete event with missing ID, responds error Bad Request`() = testApplication {
		setupTestApplication()

		val response = client.delete("/event") {
			createFullyAuthenticatedToken("user1")
		}

		assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
	}

	@Test
	fun `Delete event`() = testApplication {
		setupTestApplication()

		runTest {
			val event = testEvents[0].copy(
				photoKeys = listOf("test-key")
			)
			events.updateEventById(event._id, event)

			val response = client.delete("/event") {
				parameter("eventId", event._id)
				createFullyAuthenticatedToken("user1")
			}

			scheduler.advanceUntilIdle()

			assertThat(response.status).isEqualTo(HttpStatusCode.OK)

			val eventFromDb = events.getEventById(event._id)
			assertThat(eventFromDb).isNull()

			coVerify(exactly = 1) {
				s3.deleteObjects(any())
			}
		}
	}

	private suspend fun ApplicationTestBuilder.createEvent(
		request: CreateEventRequest,
		photoCount: Int
	): HttpResponse {
		val bytes = (1..photoCount).map {
			"abc".encodeToByteArray()
		}

		val json = Json.encodeToString(request)
		return client.submitFormWithBinaryData(
			url = "/event",
			formData = formData {
				bytes.forEachIndexed { index, bytes ->
					append("photo$index", bytes, Headers.build {
						append(HttpHeaders.ContentType, "image/jpeg")
						append(HttpHeaders.ContentDisposition, "filename=photo$index.jpg")
					})
				}
				append("create_event_request", json, Headers.build {
					append(HttpHeaders.ContentType, "text/plain")
					append(HttpHeaders.ContentDisposition, "form-data; name=\"create_event_request\"")
				})
			}
		) {
			method = HttpMethod.Post
			createFullyAuthenticatedToken("user1")
		}
	}

	private suspend fun ApplicationTestBuilder.updateEvent(
		request: UpdateEventRequest,
		newPhotoCount: Int
	): HttpResponse {
		val bytes = (1..newPhotoCount).map {
			"abc".encodeToByteArray() // simulate image bytes
		}

		val json = Json.encodeToString(request)
		return client.submitFormWithBinaryData(
			url = "/event",
			formData = formData {
				bytes.forEachIndexed { index, bytes ->
					append("photo$index", bytes, Headers.build {
						append(HttpHeaders.ContentType, "image/jpeg")
						append(HttpHeaders.ContentDisposition, "filename=photo$index.jpg")
					})
				}
				append("update_event_request", json, Headers.build {
					append(HttpHeaders.ContentType, "text/plain")
					append(HttpHeaders.ContentDisposition, "form-data; name=\"update_event_request\"")
				})
			}
		) {
			method = HttpMethod.Put
			createFullyAuthenticatedToken("user1")
		}
	}
}