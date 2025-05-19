package com.realityexpander.domain.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object TimeUtil {

    fun fromISOTime(time: String): LocalDateTime {
        return LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
    }
}