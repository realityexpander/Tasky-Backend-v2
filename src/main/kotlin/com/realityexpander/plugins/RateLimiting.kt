package com.realityexpander.plugins

import dev.forst.ktor.ratelimiting.RateLimiting
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import java.time.Duration

fun Application.configureRateLimiting() {
    install(RateLimiting) {
        registerLimit(
            limit = 200,
            window = Duration.ofMinutes(1)
        ) {
            val apiKeyHeader = request.header("x-api-key")
            apiKeyHeader ?: request.origin.localHost
        }
        excludeRequestWhen {
            this.request.uri.contains("/status")
        }
    }
}