package com.realityexpander.plugins

import com.realityexpander.domain.agenda.KilledTokenDataSource
import com.realityexpander.domain.security.token.TokenConfig
import com.realityexpander.domain.user.UserDataSource
import com.realityexpander.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
	tokenConfig: TokenConfig,
) {
	routing {
		register()
		login(tokenConfig)
		authenticate()
		logout()
		createApiKey()
		requestAccessToken(tokenConfig)

		agenda()

		event()
		task()
		reminder()

		cleanupRoutes()
		status() // Health check endpoint

		get("/") {
			call.respond(HttpStatusCode.OK)
		}
	}
}
