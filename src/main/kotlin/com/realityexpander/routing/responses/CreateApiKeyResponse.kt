package com.realityexpander.routing.responses

import kotlinx.serialization.Serializable

@Serializable
data class CreateApiKeyResponse(
	val apiKey: String = ""
)