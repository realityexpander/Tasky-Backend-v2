package com.realityexpander.domain.util

fun generateRandomString(length: Int = 128): String {
    val characterSet = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { characterSet.random() }
        .joinToString("")
}