package com.realityexpander.domain.util

import kotlinx.serialization.Serializable

@Serializable
data class ErrorMessage(
    val message: String? = null
)
