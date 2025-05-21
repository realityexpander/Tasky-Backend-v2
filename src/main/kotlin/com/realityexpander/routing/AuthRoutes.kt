package com.realityexpander.routing

import com.realityexpander.data.models.User
import com.realityexpander.domain.agenda.KilledTokenDataSource
import com.realityexpander.domain.security.auth.ApiKeyDataSource
import com.realityexpander.domain.security.hashing.HashingService
import com.realityexpander.domain.security.hashing.SaltedHash
import com.realityexpander.domain.security.token.TokenClaim
import com.realityexpander.domain.security.token.TokenConfig
import com.realityexpander.domain.security.token.TokenService
import com.realityexpander.domain.user.UserDataSource
import com.realityexpander.domain.user.UserDataValidationService
import com.realityexpander.domain.util.EnvironmentProvider
import com.realityexpander.domain.util.ErrorMessage
import com.realityexpander.domain.util.generateRandomString
import com.realityexpander.inject
import com.realityexpander.log
import com.realityexpander.routing.principal.userId
import com.realityexpander.routing.requests.*
import com.realityexpander.routing.responses.AccessTokenResponse
import com.realityexpander.routing.responses.AuthResponse
import io.ktor.http.*
import io.ktor.server.application.log
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.annotation.KoinInternalApi
import kotlin.time.Duration.Companion.seconds

fun Route.register() {
    authenticate("apiKey") {
        post("register") {
            val request = call.receiveNullable<RegisterRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val hashingService: HashingService by inject()
            val userDataSource: UserDataSource by inject()
            val userDataValidationService: UserDataValidationService by inject()

            val validationResult = userDataValidationService.validateUserData(
                fullName = request.fullName,
                email = request.email,
                password = request.password
            )
            val errors =
                listOf(validationResult.fullNameError, validationResult.emailError, validationResult.passwordError)
            val hasError = errors.any { it != null }

            if (hasError) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorMessage(
                        message = errors.first { it != null }
                    )
                )
                return@post
            }

            // Check if user already exists
            val doesUserExist = userDataSource.getUserByEmail(request.email.trim().lowercase()) != null
            if (doesUserExist) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorMessage(
                        message = "A user with that email already exists."
                    )
                )
            }

            val refreshToken = generateRandomString()
            val saltedHash = hashingService.generateSaltedHash(request.password)
            val user = User(
                email = request.email.trim().lowercase(),
                fullName = request.fullName,
                hashedPassword = saltedHash.hash,
                salt = saltedHash.salt,
                refreshToken = refreshToken
            )
            userDataSource.insertUser(user)

            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.login(tokenConfig: TokenConfig) {
    authenticate("apiKey") {
        post("login") {
            val environmentVariables: EnvironmentProvider by inject()
            val request = call.receiveNullable<AuthRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val hashingService: HashingService by inject()
            val userDataSource: UserDataSource by inject()
            val tokenService: TokenService by inject()

            val user = userDataSource.getUserByEmail(request.email.trim().lowercase())
            if (user == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorMessage(
                        message = "The email or password is incorrect"
                    )
                )
                return@post
            }

            val isValidPassword = hashingService.verify(
                value = request.password,
                saltedHash = SaltedHash(
                    hash = user.hashedPassword,
                    salt = user.salt
                )
            )
            if (!isValidPassword) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorMessage(
                        message = "The email or password is incorrect"
                    )
                )
                return@post
            }

            val config = if(request.email == environmentVariables.createApiKeyUser) {
                tokenConfig.copy(
                    expiresIn = 10.seconds.inWholeMilliseconds
                )
            } else tokenConfig
            val accessToken = tokenService.generate(
                config = config,
                TokenClaim(
                    name = "userId",
                    value = user._id
                )
            )

            call.respond(
                status = HttpStatusCode.OK,
                message = AuthResponse(
                    accessToken = accessToken,
                    refreshToken = user.refreshToken,
                    userId = user._id,
                    fullName = user.fullName,
                    accessTokenExpirationTimestamp = System.currentTimeMillis() + config.expiresIn
                )
            )
        }
    }
}

fun Route.authenticate() {
    authenticate("jwt") {
        get("authenticate") {
            log.info("Authenticating: ${call.userId}")
            call.respond(HttpStatusCode.OK)
        }
    }
}

@OptIn(KoinInternalApi::class)
fun Route.requestAccessToken(
    config: TokenConfig,
) {
    authenticate("apiKey") {
        // Request new access token using refresh token
        post("accessToken") {
            val environmentVariables: EnvironmentProvider by inject()
            val userDataSource: UserDataSource by inject()
            val tokenService: TokenService by inject()
            val body = call.receiveNullable<AccessTokenRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "No refresh token body attached")
                return@post
            }

            val isValidRefreshToken = userDataSource.checkRefreshTokenForUser(
                userId = body.userId,
                refreshToken = body.refreshToken
            )
            if (!isValidRefreshToken) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorMessage("The provided refresh token does not match the user ID.")
                )
                return@post
            }

            val email = userDataSource.getUserById(body.userId)?.email
            val tokenConfig = if(email == environmentVariables.createApiKeyUser) {
                val debugTime = 10.seconds.inWholeMilliseconds
                application.log.debug("Refresh from debug ${environmentVariables.createApiKeyUser}, expiry in ${debugTime.seconds}")
                config.copy(expiresIn = debugTime)
            } else
                config

           val accessToken = tokenService.generate(
                config = tokenConfig,
                TokenClaim(
                    name = "userId",
                    value = body.userId
                )
            )

            call.respond(
                HttpStatusCode.OK,
                AccessTokenResponse(
                    accessToken = accessToken,
                    expirationTimestamp = System.currentTimeMillis() + config.expiresIn
                )
            )
        }

        // Save Killed access token to DB
        post("killToken") {
            val killedTokenDataSource: KilledTokenDataSource by inject()
            val body = call.receiveNullable<KillAccessTokenRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest, "No kill access token body attached")
                return@post
            }
            val token = body.accessToken.removePrefix("Bearer").trim()

            try {
                killedTokenDataSource.insertKilledToken(token)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.message ?: "Unknown error")
            }
        }
    }
}

fun Route.logout() {
    val killedTokenDataSource: KilledTokenDataSource by inject()

    authenticate("jwt") {
        get("logout") {
            call.request.header("Authorization")?.let { fullToken ->
                val token = fullToken.removePrefix("Bearer").trim()

                killedTokenDataSource.insertKilledToken(token)

                call.respond(HttpStatusCode.OK)
            } ?: kotlin.run {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}

fun Route.createApiKey() {
    val apiKeyDataSource: ApiKeyDataSource by inject()

    authenticate("basic") { // Caller must use the `createApiKeyUser` from applicationSecrets.conf to create an API key
        // Create API key
        post("apiKey") {
            val request = call.receiveNullable<CreateApiKeyRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            // Check if email already exists (& associated key)
            val user = apiKeyDataSource.getKeyByEmail(request.email)
            if (user != null) {
                log.info("An API key already exists for this user, refreshing key...")
            }

            val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            val apiKey = (1..16).map {
                charset.random()
            }.joinToString("")

            apiKeyDataSource.createKey(
                key = apiKey,
                email = request.email,
                validFrom = request.validFrom
            )

            call.respond(
                HttpStatusCode.OK,
                mapOf("apiKey" to apiKey)
            )
        }
    }
}

