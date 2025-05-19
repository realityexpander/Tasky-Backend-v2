package com.realityexpander.routes

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.auth0.jwt.JWT
import com.realityexpander.domain.util.EnvironmentProvider
import com.realityexpander.fakes.UserDataSourceFake
import com.realityexpander.routing.requests.AuthRequest
import com.realityexpander.routing.requests.CreateApiKeyRequest
import com.realityexpander.routing.requests.RegisterRequest
import com.realityexpander.routing.responses.AuthResponse
import com.realityexpander.routing.responses.CreateApiKeyResponse
import com.realityexpander.util.*
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.test.runTest
import org.koin.ktor.ext.inject
import org.koin.test.inject
import java.util.*
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


fun ApplicationTestBuilder.setupClient(): HttpClient {
	return createClient {
		install(ContentNegotiation) {
			json()
		}
	}
}


class AuthRoutesTest : RootTest() {

	private val users by inject<UserDataSourceFake>()
	val environmentVariables: EnvironmentProvider by inject()

	@Test
	fun `Test register and login`() = testApplication {
		val client = setupClient()
		setupTestApplication()

		runTest {
			val registerRequest = RegisterRequest(
				fullName = "Chris Athanas",
				email = "new@test.com",
				password = "Test12345"
			)
			val registerResponse = client.post("/register") {
				apiKeyAuth("user1")
				contentType(ContentType.Application.Json)
				setBody(registerRequest)
			}

			assertThat(registerResponse.status).isEqualTo(HttpStatusCode.OK)

			val userFromDb = users.getUserByEmail("new@test.com")
			assertThat(userFromDb).isNotNull()

			val loginRequest = AuthRequest(
				email = "new@test.com",
				password = "Test12345"
			)
			val loginResponse = client.post("/login") {
				apiKeyAuth("user1")
				contentType(ContentType.Application.Json)
				setBody(loginRequest)
			}

			assertThat(registerResponse.status).isEqualTo(HttpStatusCode.OK)

			val body = loginResponse.getBody<AuthResponse>()

			assertThat(body.fullName).isEqualTo("Chris Athanas")
			assertThat(body.userId).isEqualTo(userFromDb!!._id)

			val tokenClaimUserId = JWT.decode(body.accessToken).getClaim("userId").asString()
			assertThat(tokenClaimUserId).isEqualTo(body.userId)
		}
	}

	@Test
	fun `Test register with email that exists, responds Conflict`() = testApplication {
		val client = setupClient()
		setupTestApplication()

		runTest {
			val registerRequest = RegisterRequest(
				fullName = "Chris Athanas",
				email = testUsers.random().email,
				password = "Test12345"
			)
			val registerResponse = client.post("/register") {
				apiKeyAuth("user1")
				contentType(ContentType.Application.Json)
				setBody(registerRequest)
			}

			assertThat(registerResponse.status).isEqualTo(HttpStatusCode.Conflict)
		}
	}

	@Test
	fun `Test login with invalid credentials, responds Unauthorized`() = testApplication {
		val client = setupClient()
		setupTestApplication()

		runTest {
			val user = testUsers.random()
			val loginRequest = AuthRequest(
				email = user.email,
				password = UUID.randomUUID().toString()  // invalid password
			)
			val response = client.post("/login") {
				apiKeyAuth("user1")
				contentType(ContentType.Application.Json)
				setBody(loginRequest)
			}

			assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
		}
	}

	@Test
	fun `Test logout and get event with same token, responds Unauthorized`() = testApplication {

		setupTestApplication()

		runTest {
			var token = ""
			client.get("/logout") {
				token = createFullyAuthenticatedToken("user1")
			}

			val response = client.get("/event") {
				parameter("eventId", testEvents.find { it.host == "user1" }?._id)
				header("Authorization", "Bearer $token")
			}

			assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
		}
	}

	@OptIn(ExperimentalTime::class)
	@Test
	fun `Test apiKey auth()`() = testApplication {
		setupTestApplication()

		val client = setupClient()
		val apiKeyRequest = CreateApiKeyRequest(
			email = "test1@test.com",
			validFrom = Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds()).toString()
		)
		val response = client.post("/apiKey") {
			basicAuth(environmentVariables.createApiKeyUser, environmentVariables.createApiKeyPassword)
			contentType(ContentType.Application.Json)
			setBody(apiKeyRequest)
		}

		assertThat(response.status).isEqualTo(HttpStatusCode.OK)
		val body = response.getBody<CreateApiKeyResponse>()
		assertThat(body.apiKey).isNotNull()
		assertThat(body.apiKey.length == 16, "ApiKey should be 16 characters long").isTrue()
	}
}