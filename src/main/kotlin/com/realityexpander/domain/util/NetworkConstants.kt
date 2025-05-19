package com.realityexpander.domain.util

object NetworkConstants {
    const val IS_PRODUCTION = false
    const val PORT = 8080
    val BASE_URL = if(IS_PRODUCTION) "http://145.14.158.77:$PORT" else "http://192.168.0.166:$PORT"
}