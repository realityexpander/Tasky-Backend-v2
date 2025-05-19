package com.realityexpander.util

import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

suspend inline fun <reified T> HttpResponse.getBody(): T {
    return Json.decodeFromString(bodyAsText())
}