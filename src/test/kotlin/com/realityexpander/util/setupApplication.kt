package com.realityexpander.util

import com.realityexpander.domain.security.token.TokenConfig
import com.realityexpander.fakes.TestEnvironmentProvider.jwtAudience
import com.realityexpander.fakes.TestEnvironmentProvider.jwtIssuer
import com.realityexpander.fakes.TestEnvironmentProvider.jwtSecret
import com.realityexpander.plugins.configureRouting
import com.realityexpander.plugins.configureSecurity
import com.realityexpander.plugins.configureSerialization
import io.ktor.server.testing.TestApplicationBuilder
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

val testTokenConfig = TokenConfig(
    issuer = jwtIssuer,
    audience = jwtAudience,
//    expiresIn = 1.hours.inWholeMilliseconds,
    expiresIn = 10.seconds.inWholeMilliseconds,
    secret = jwtSecret
)

fun TestApplicationBuilder.setupTestApplication() {
    application {
        configureSecurity(testTokenConfig)
        configureRouting(testTokenConfig)
        configureSerialization()
    }
}