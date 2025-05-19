package com.realityexpander.routing.responses

import kotlinx.serialization.Serializable

@Serializable
data class PhotoDTO(
    val key: String,
    val url: String
)

