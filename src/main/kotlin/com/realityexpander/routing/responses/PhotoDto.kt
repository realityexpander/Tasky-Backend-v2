package com.realityexpander.routing.responses

import kotlinx.serialization.Serializable

@Serializable
data class PhotoDto(
    val key: String,
    val url: String
)

