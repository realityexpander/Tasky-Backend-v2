package com.realityexpander.util

import com.realityexpander.domain.security.token.TokenClaim
import com.realityexpander.domain.security.token.TokenConfig
import com.realityexpander.domain.util.EnvironmentProvider
import com.realityexpander.fakes.TestEnvironmentProvider
import com.realityexpander.fakes.TokenServiceFake
import io.ktor.client.request.*
import org.koin.java.KoinJavaComponent.inject
import kotlin.time.Duration.Companion.days

fun HttpRequestBuilder.createFullyAuthenticatedToken(userId: String): String {
    val testEnvironment by inject<EnvironmentProvider>(TestEnvironmentProvider::class.java)

    val token = TokenServiceFake().generate(
        config = TokenConfig(
            issuer = testEnvironment.jwtIssuer,
            audience = testEnvironment.jwtAudience,
            expiresIn = System.currentTimeMillis() + 365.days.inWholeMilliseconds,
            secret = testEnvironment.jwtSecret
        ),
        TokenClaim("userId", userId)
    )

    // Add the token to the request headers
    header("Authorization", "Bearer $token")
    header("x-api-key", userId)

    return token
}

fun HttpRequestBuilder.apiKeyAuth(userId: String) {
    header("x-api-key", userId)
}