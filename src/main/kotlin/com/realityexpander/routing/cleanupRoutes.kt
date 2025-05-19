package com.realityexpander.routing

import com.realityexpander.domain.cleanup.CleanupService
import com.realityexpander.domain.util.ErrorMessage
import com.realityexpander.inject
import com.realityexpander.routing.requests.CleanupRequest
import com.realityexpander.routing.responses.CleanupResponse
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.cleanupRoutes() {
	val cleanupService: CleanupService by inject()

	authenticate("basic") {
		// Delete old entries before a certain date
		post("cleanup") {
			try {
				val request = call.receiveNullable<CleanupRequest>()
					?: kotlin.run {
						call.respond(HttpStatusCode.BadRequest)
						return@post
					}
				val deletedCount =
					cleanupService.cleanupOldEntries(request.cleanUpBefore)

				call.respond(HttpStatusCode.OK, CleanupResponse(deletedCount))
			} catch (e: Exception) {
				call.respond(
					HttpStatusCode.BadRequest,
					ErrorMessage("Invalid request format")
				)
				return@post
			}
		}
	}
}