package com.realityexpander.routing.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * HumanDuration - Utility class converting Long to human-readable localized-time-zone date/time strings.
 *
 * Add by right-clicking a variable, choose "View As"->"Create renderer" in debugger "Create Renderer"
 *   - Use this string:
 *     `new HumanDuration(this).toTimeDurationStr();`
 *
 * @author Chris Athanas (realityexpanderdev@gmail.com)
 * @since 0.1
 * @param duration The duration in milliseconds to be converted.
 */
class HumanDuration constructor(
	private val duration: Long,
) {
	companion object {
		val second = Duration.ofSeconds(1).toMillis()
		val minute = Duration.ofMinutes(1).toMillis()
		val hour = Duration.ofHours(1).toMillis()
		val day = Duration.ofDays(1).toMillis()
		val week = Duration.ofDays(7).toMillis()
		val month = Duration.ofDays(30).toMillis()
		val year = Duration.ofDays(365).toMillis()
	}

	// Add suffix to the string if the value is not 1
	private fun String.addSuffix(value: Long): String {
		return if (value == 1L) this else this + "s"
	}

	fun toTimeDurationStr(): String {
		val seconds = duration / second
		val minutes = duration / minute
		val hours = duration / hour
		val days = duration / day
		val weeks = duration / week
		val months = duration / month
		val years = duration / year

		return when {
			seconds < 60 -> "$seconds second".addSuffix(seconds)
			minutes < 60 -> "$minutes minute".addSuffix(minutes) +
					" ${seconds % 60} second".addSuffix(seconds % 60)
			hours < 24 -> "$hours hour".addSuffix(hours) +
					" ${minutes % 60} minute".addSuffix(minutes % 60)
			days < 7 -> "$days day".addSuffix(days) +
					" ${hours % 24} hour".addSuffix(hours % 24)
			weeks < 4 -> "$weeks week".addSuffix(weeks) +
					" ${days % 7} day".addSuffix(days % 7)
			months < 12 -> "$months month".addSuffix(months) +
					" ${days % 30} day".addSuffix(days % 30)
			else -> "$years year".addSuffix(years) +
					" ${months % 12} month".addSuffix(months % 12)
		}
	}
}

fun main() {
	println(HumanDuration(1.seconds.inWholeMilliseconds).toTimeDurationStr())
	println(HumanDuration(15.seconds.inWholeMilliseconds).toTimeDurationStr())
	println(HumanDuration(1.minutes.inWholeMilliseconds + 1.seconds.inWholeMilliseconds).toTimeDurationStr())
	println(HumanDuration(15.minutes.inWholeMilliseconds + 2.seconds.inWholeMilliseconds).toTimeDurationStr())
	println(HumanDuration(1.hours.inWholeMilliseconds + 1.minutes.inWholeMilliseconds).toTimeDurationStr())
	println(HumanDuration(15.hours.inWholeMilliseconds + 6.minutes.inWholeMilliseconds).toTimeDurationStr())
	println(HumanDuration(1.days.inWholeMilliseconds + 1.hours.inWholeMilliseconds).toTimeDurationStr())
	println(HumanDuration(15.days.inWholeMilliseconds + 2.hours.inWholeMilliseconds).toTimeDurationStr())
	println(HumanDuration(7.days.inWholeMilliseconds + 1.days.inWholeMilliseconds).toTimeDurationStr())
	println(HumanDuration(14.days.inWholeMilliseconds + 3.days.inWholeMilliseconds).toTimeDurationStr())
	println(HumanDuration(30.days.inWholeMilliseconds + 1.days.inWholeMilliseconds).toTimeDurationStr())
	println(HumanDuration(90.days.inWholeMilliseconds + 7.days.inWholeMilliseconds).toTimeDurationStr())
}