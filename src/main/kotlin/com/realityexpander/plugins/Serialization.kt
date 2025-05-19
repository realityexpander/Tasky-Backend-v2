package com.realityexpander.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            json = Json {
                ignoreUnknownKeys = true
//                ignoreType<HttpStatusCode>() // prevents warnings at runtime // CDA REMOVE?
                isLenient = true
                prettyPrint = true
                encodeDefaults = false
            }
        )
    }
}
