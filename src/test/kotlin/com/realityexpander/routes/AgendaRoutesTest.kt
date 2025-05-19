package com.realityexpander.routes

import assertk.assertThat
import assertk.assertions.hasSize
import com.realityexpander.routing.responses.AgendaDTOResponse
import com.realityexpander.util.RootTest
import com.realityexpander.util.createFullyAuthenticatedToken
import com.realityexpander.util.getBody
import com.realityexpander.util.setupTestApplication
import io.ktor.client.request.*
import io.ktor.server.testing.*
import kotlinx.coroutines.test.runTest
import java.time.ZonedDateTime
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalUnit
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class AgendaRoutesTest : RootTest() {

	@OptIn(ExperimentalTime::class)
	@Test
	fun `Test agenda for day`() = testApplication {

		setupTestApplication()
		runTest {

			val client = setupClient()
			val response = client.get("/agenda") {
				//parameter("time", 1700159550000)  // 2023-11-16T12:33:10.000Z
				parameter("time", ZonedDateTime.parse("2023-11-16T12:33:10-06:00[America/Chicago]").toInstant().toEpochMilli())
				createFullyAuthenticatedToken("user1")
			}

			val body = response.getBody<AgendaDTOResponse>()

			assertThat(body.events).hasSize(1)
			assertThat(body.tasks).hasSize(1)
			assertThat(body.reminders).hasSize(1)
		}
	}
}

//@OptIn(ExperimentalTime::class)
//fun main() {
////	println(ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(1700159590000), ZonedDateTime.now().zone))
//	println(ZonedDateTime.parse("2023-11-16T12:33:10-06:00[America/Chicago]").toInstant().toEpochMilli())
//}