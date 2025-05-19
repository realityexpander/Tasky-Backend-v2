package com.realityexpander.routing.util

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.auth.AuthenticationProvider
//import io.ktor.server.auth.Principal // // CDA FIX - Ktor 3 fix for `Principal` -> Any
import io.ktor.server.request.header
import io.ktor.server.response.respond

// Replaces library: implementation("dev.forst:ktor-api-key:2.2.4") // (No longer maintained)
/**
 * Represents an API Key authentication provider.
 */
class ApiKeyAuthenticationProvider internal constructor(
    configuration: Configuration
) : AuthenticationProvider(configuration) {
    private val headerName: String = configuration.headerName
    private val validateFunction: ApiKeyValidateFunction = configuration.validateFunction
    private val challengeFunction: ApiKeyChallengeFunction = configuration.challengeFunction
    private val authScheme = configuration.authScheme

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val apiKey: Any = context.call.request.header(headerName) ?: ""
        val principal: Any = validateFunction(context.call, apiKey)

        val validateFailedCause = when {
            apiKey.toString().isEmpty() -> AuthenticationFailedCause.NoCredentials
            principal.toString().isEmpty() -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if(validateFailedCause != null) {
            context.challenge(
                authScheme,
                validateFailedCause
            ) { challenge, call ->
                // Indicate that the authentication failed
                challengeFunction(call)

                challenge.complete()
            }
        }
        if (principal.toString().isNotBlank()) {
            context.principal(principal)
        }
    }
    /**
     * Api key auth configuration.
     */
    class Configuration internal constructor(name: String?) : Config(name) {

        internal lateinit var validateFunction: ApiKeyValidateFunction

        internal var challengeFunction: ApiKeyChallengeFunction = { call ->
            call.respond(HttpStatusCode.Unauthorized)
        }

        /**
         * Name of the scheme used when `challenge` fails, see [AuthenticationContext.challenge].
         */
        var authScheme: String = "apiKey"

        /**
         * Name of the header that will be used as a source for the api key.
         */
        var headerName: String = "X-Api-Key"

        /**
         * Sets a validation function that will check the API key retrieved from [headerName] instance and return [Principal],
         * or null if credential does not correspond to an authenticated principal.
         */
        fun validate(body: suspend ApplicationCall.(Any) -> Any) {
            validateFunction = body
        }

        /**
         * A response to send back if authentication failed.
         */
        fun challenge(body: ApiKeyChallengeFunction) {
            challengeFunction = body
        }
    }
}

/**
 * Installs the `API Key` authentication mechanism.
 */
fun AuthenticationConfig.apiKey(
    name: String? = null,
    configure: ApiKeyAuthenticationProvider.Configuration.() -> Unit
) {
    val provider = ApiKeyAuthenticationProvider(
        ApiKeyAuthenticationProvider.Configuration(name)
            .apply(configure)
    )
    register(provider)
}

/**
 * Alias for function signature that is invoked when verifying header.
 */
typealias ApiKeyValidateFunction = suspend ApplicationCall.(Any) -> Any

/**
 * Alias for function signature that is called when authentication fails.
 */
typealias ApiKeyChallengeFunction = suspend (ApplicationCall) -> Unit