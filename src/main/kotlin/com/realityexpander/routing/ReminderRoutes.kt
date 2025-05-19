package com.realityexpander.routing

import com.realityexpander.data.models.Reminder
import com.realityexpander.domain.agenda.ReminderDataSource
import com.realityexpander.domain.util.ErrorMessage
import com.realityexpander.inject
import com.realityexpander.routing.principal.userId
import com.realityexpander.routing.requests.CreateReminderRequest
import com.realityexpander.routing.requests.UpdateReminderRequest
import com.realityexpander.routing.responses.ReminderDTOResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reminder() {
	val reminderDataSource: ReminderDataSource by inject()

	authenticate("jwt") {
		// Create a new reminder
		post("reminder") {
			val request = call.receiveNullable<CreateReminderRequest>() ?: kotlin.run {
				call.respond(HttpStatusCode.BadRequest)
				return@post
			}

			val reminder = Reminder(
				title = request.title,
				description = request.description,
				userId = call.userId,
				time = request.time,
				remindAt = request.remindAt,
				_id = request.id
			)
			val wasAcknowledged = reminderDataSource.insertReminder(reminder)
			if (!wasAcknowledged) {
				call.respond(HttpStatusCode.Conflict)
				return@post
			}

			call.respond(HttpStatusCode.OK)
		}

		// Get all reminders for the user
		get("reminder") {
			val reminderId = call.parameters["reminderId"]
			if (reminderId == null) {
				call.respond(
					HttpStatusCode.BadRequest,
					ErrorMessage(
						message = "No reminder ID provided"
					)
				)
				return@get
			}

			val reminder = reminderDataSource.getReminderById(reminderId)
			if (reminder == null) {
				call.respond(
					HttpStatusCode.BadRequest,
					ErrorMessage(
						message = "Reminder not found"
					)
				)
				return@get
			}

			if (reminder.userId != call.userId) {
				call.respond(HttpStatusCode.Forbidden)
				return@get
			}

			val reminderDTOResponse = ReminderDTOResponse(
				title = reminder.title,
				description = reminder.description,
				time = reminder.time,
				remindAt = reminder.remindAt,
				id = reminder._id
			)

			call.respond(HttpStatusCode.OK, reminderDTOResponse)
		}

		// Delete a reminder
		delete("reminder") {
			val reminderId = call.parameters["reminderId"]
			if (reminderId == null) {
				call.respond(
					HttpStatusCode.BadRequest,
					ErrorMessage(
						message = "No reminder ID provided"
					)
				)
				return@delete
			}

			val wasAcknowledged = reminderDataSource.deleteReminder(reminderId, call.userId)
			if (!wasAcknowledged) {
				call.respond(HttpStatusCode.Conflict)
				return@delete
			}

			call.respond(HttpStatusCode.OK)
		}

		// Update a reminder
		put("reminder") {
			val request = call.receiveNullable<UpdateReminderRequest>() ?: kotlin.run {
				call.respond(HttpStatusCode.BadRequest)
				return@put
			}

			val reminderFromDb = reminderDataSource.getReminderById(request.id)
			if (reminderFromDb == null) {
				call.respond(
					HttpStatusCode.Conflict,
					ErrorMessage(message = "Reminder not found")
				)
				return@put
			}
			if (reminderFromDb.userId != call.userId) {
				call.respond(HttpStatusCode.Forbidden)
				return@put
			}

			val reminder = reminderFromDb.copy(
				title = request.title,
				description = request.description,
				_id = request.id,
				time = request.time,
				remindAt = request.remindAt
			)
			val wasAcknowledged = reminderDataSource.updateReminderById(request.id, reminder)
			if (!wasAcknowledged) {
				call.respond(HttpStatusCode.Conflict)
				return@put
			}

			call.respond(HttpStatusCode.OK)
		}
	}
}
