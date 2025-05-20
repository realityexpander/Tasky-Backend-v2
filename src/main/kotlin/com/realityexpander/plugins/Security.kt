package com.realityexpander.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.realityexpander.domain.agenda.KilledTokenDataSource
import com.realityexpander.domain.security.auth.ApiKeyDataSource
import com.realityexpander.domain.security.token.TokenConfig
import com.realityexpander.domain.util.EnvironmentProvider
import com.realityexpander.domain.util.ErrorMessage
import com.realityexpander.routing.util.apiKey
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject


fun Application.configureSecurity(tokenConfig: TokenConfig) {
	val apiKeyDataSource: ApiKeyDataSource by inject()
	val killedTokenDataSource: KilledTokenDataSource by inject()
	val environmentVariables: EnvironmentProvider by inject()

	// Setup authentication strategies
	authentication {
		basic("basic") { // Used only to create ApiKeys (x-api-key)

			validate { credentials ->
				if (credentials.name == environmentVariables.createApiKeyUser
					&& credentials.password == environmentVariables.createApiKeyPassword
				) {
					UserIdPrincipal(environmentVariables.createApiKeyUser)
				} else
					null
			}
		}

		apiKey(name = "apiKey") {
			challenge { call ->
				// Handle missing or invalid API key error response
				call.respond(
					HttpStatusCode.Unauthorized,
					ErrorMessage(
						message = "No valid API key provided, ${call.authentication.allFailures}"
					)
				)
			}

			validate { key ->
				if (apiKeyDataSource.isValidKey(key as String))
					key
				else
					"" // Invalid API key
			}
		}

		jwt(name = "jwt") {
			var errorReasons = ""

			verifier(
				JWT
					.require(Algorithm.HMAC256(tokenConfig.secret))
					.withAudience(tokenConfig.audience)
					.withIssuer(tokenConfig.issuer)
					.build()
			)

			validate { jWTCredential ->
				errorReasons = ""
				val apikey = request.header("x-api-key")
				val accessToken = request.header("Authorization")?.takeLastWhile { it != ' ' }

				// Check for JWT errors
				if (!jWTCredential.payload.audience.contains(tokenConfig.audience))
					errorReasons += "Audience is invalid, "
				if (jWTCredential.payload.getClaim("userId") == null)
					errorReasons += "User ID is invalid / missing, "
				if (apikey == null) errorReasons += "API key is missing, " else {
					if (!apiKeyDataSource.isValidKey(apikey))
						errorReasons += "API key is invalid, "
				}
				if (accessToken == null) errorReasons += "Token is missing, " else {
					// Check if the token is killed or revoked
					if (killedTokenDataSource.isKilled(accessToken))
						errorReasons += "Token has been revoked, "
				}
				if (errorReasons.endsWith(", "))
					errorReasons = errorReasons.dropLast(2) // trim trailing comma

				// No errors, return JWTPrincipal
				if (errorReasons.isEmpty()) {
					JWTPrincipal(jWTCredential.payload)
				} else null
			}

			challenge { _, _ ->
				call.respond(
					HttpStatusCode.Unauthorized,
					ErrorMessage("Invalid or missing token or API key, $errorReasons")
				)
			}
		}
	}
}
