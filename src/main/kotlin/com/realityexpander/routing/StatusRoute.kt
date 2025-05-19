package com.realityexpander.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.status() {
    get("/status") {
        call.respond(HttpStatusCode.OK)
    }
}